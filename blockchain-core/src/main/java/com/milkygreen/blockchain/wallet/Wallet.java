package com.milkygreen.blockchain.wallet;

import com.milkygreen.blockchain.core.Transaction;
import com.milkygreen.blockchain.core.TransactionInput;
import com.milkygreen.blockchain.core.TransactionOutput;
import com.milkygreen.blockchain.db.DBUtil;
import com.milkygreen.blockchain.util.ByteUtil;
import com.milkygreen.blockchain.util.CryptoUtil;
import com.milkygreen.blockchain.util.JsonUtil;
import com.milkygreen.blockchain.util.TransactionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *  钱包。
 *  提供查询、转账、新增账户、删除账户等功能。
 */
public class Wallet {

    /**
     * 账户集合
     */
    private Set<Account> accounts;

    /**
     * 新增账户
     * @param account
     */
    public void addAccount(Account account){
        accounts.add(account);
    }

    /**
     * 删除账户
     * @param address
     */
    public void removeAccount(String address){
        Account target = null;
        for (Account account : accounts) {
            if(account.getAddress().equals(address)){
                target = account;
            }
        }
        accounts.remove(target);
    }

    /**
     * 给指定账户付款
     * @param amount 金额
     * @param payee 收款地址
     * 1、第一步从自己的钱包中查询是否有足够的「未话费输出」用来支付
     * 2、凑出的金额是否大于支付金额，多的话需要给自己找零
     * 3、构建交易，放到未确认交易池中，等待被挖
     */
    public Transaction pay(long amount,String payee){
        long sum = 0;
        List<TransactionOutput> payeeOutputs = new ArrayList<>();
        out : for (Account account : accounts) {
            // 从自己的账户中，找未花费输出，看看是否能凑出要支付的金额
            Set<TransactionOutput> transactionOutputs = DBUtil.UTXO.get(account.getAddress());
            if(transactionOutputs != null){
                for (TransactionOutput output : transactionOutputs) {
                    sum += output.getAmount();
                    payeeOutputs.add(output);
                    if(sum >= amount){
                        break out;
                    }
                }
            }
        }
        if(sum < amount){
            throw new RuntimeException("没有足够的余额用于支付");
        }else{
            // 构建交易
            Transaction transaction = new Transaction();
            transaction.setTimestamp(System.currentTimeMillis());
            transaction.setPayee(payee);
            transaction.setAmount(amount);
            transaction.setNonce(ByteUtil.bytesToUint64(ByteUtil.random32Bytes()));
            String transactionHash = TransactionUtil.calculateHash(transaction);
            transaction.setHash(transactionHash);
            // 构建交易的input列表
            List<TransactionInput> inputs = new ArrayList<>();
            for (TransactionOutput payeeOutput : payeeOutputs) {
                // input是由一个未花费的output得来的，可以认为是对output的一个包装
                TransactionInput transactionInput = new TransactionInput();
                transactionInput.setUnspentOutput(payeeOutput);
                transactionInput.setTransactionHash(transactionHash);
                // 未花费输出(output)的地址，就是现在这个付款人的地址。
                // 根据地址找到私钥，给input签名，这样别的节点拿到这个交易后，可以用公钥对签名进行验证
                // 证明这个output确实是付款人自己发起的，因为只有付款人才有正确的私钥
                String account = payeeOutput.getAccount();
                Account accountObj = DBUtil.accountDB.get(account);
                transactionInput.setPublicKey(accountObj.getPublicKey());
                String signature = TransactionUtil.signature(accountObj.getPrivateKey(),transactionInput);
                transactionInput.setSignature(signature);
                inputs.add(transactionInput);
            }
            transaction.setInputs(inputs);

            // 构建交易输出列表
            List<TransactionOutput> outputs = new ArrayList<>();
            // 先构建给收款人的输出
            TransactionOutput transactionOutput = new TransactionOutput();
            transactionOutput.setAccount(payee);
            transactionOutput.setAmount(amount);
            transactionOutput.setIndex(0);
            transactionOutput.setTransactionHash(transactionHash);
            outputs.add(transactionOutput);
            if(sum > amount){
                // 需要给自己找零。
                // 可以想象output是不可分割的，不能只花一个output的一分部金额，要么不花，要么全花。
                // 如果自己有一个10块的output，但是只想给别人支付1块钱，就需要生成两个新的output，一个1块的给对方，一个9块的给自己
                // 这里新生成了一个账户用来接收找零。创建账户是没有成本的，这样可以更好的保证匿名性。
                Account changeAccount = CryptoUtil.randomAccount();
                DBUtil.accountDB.put(changeAccount.getAddress(),changeAccount);
                TransactionOutput changeOutput = new TransactionOutput();
                changeOutput.setAccount(changeAccount.getAddress());
                changeOutput.setAmount(sum - amount);
                changeOutput.setIndex(1);
                changeOutput.setTransactionHash(transactionHash);
                outputs.add(changeOutput);
            }
            transaction.setOutputs(outputs);
            return transaction;
        }
    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }
}
