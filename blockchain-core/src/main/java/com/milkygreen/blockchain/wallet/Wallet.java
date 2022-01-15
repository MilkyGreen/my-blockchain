package com.milkygreen.blockchain.wallet;

import com.milkygreen.blockchain.core.Miner;
import com.milkygreen.blockchain.core.Transaction;
import com.milkygreen.blockchain.core.TransactionInput;
import com.milkygreen.blockchain.core.TransactionOutput;
import com.milkygreen.blockchain.db.DBUtil;
import com.milkygreen.blockchain.util.ByteUtil;
import com.milkygreen.blockchain.util.CryptoUtil;
import com.milkygreen.blockchain.util.TransactionUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  钱包。
 *  管理本地的账号。可以添加、删除账户，发起转账等。
 */
public class Wallet {

    /**
     * 保存本节钱包的地址-账号（实际应该保存在本地文件中，目前仅内存保存）
     */
    public Map<String, Account> accountDB = new ConcurrentHashMap<>();

    /**
     * 新增账户
     * @param account
     */
    public void addAccount(Account account){
        accountDB.put(account.getAddress(),account);
    }

    /**
     * 删除账户
     * @param address
     */
    public void removeAccount(String address){
        accountDB.remove(address);
    }

    /**
     * 给指定账户付款
     * @param amount 金额
     * @param payee 收款地址
     * 1、第一步从自己的钱包中查询是否有足够的「未花费输出」用来支付
     * 2、凑出的金额是否大于支付金额，多的话需要给自己找零
     * 3、构建交易，放到未确认交易池中，等待被挖
     */
    public void pay(long amount,String payee){
        long sum = 0;
        List<TransactionOutput> payeeOutputs = new ArrayList<>();
        // 找出子节点所有账户
        Collection<Account> accounts = accountDB.values();
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
                Account accountObj = accountDB.get(account);
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
                accountDB.put(changeAccount.getAddress(),changeAccount);
                TransactionOutput changeOutput = new TransactionOutput();
                changeOutput.setAccount(changeAccount.getAddress());
                changeOutput.setAmount(sum - amount);
                changeOutput.setIndex(1);
                changeOutput.setTransactionHash(transactionHash);
                outputs.add(changeOutput);
            }
            transaction.setOutputs(outputs);
            transaction.setType(Transaction.TRANSACTION_TYPE_NORMAL);
            DBUtil.unConfirmTransactionPool.put(transaction.getHash(),transaction);
        }
    }

    /**
     * 查询所有余额
     * @return
     */
    public long getBalance(){
        int balance = 0;
        Collection<Account> accounts = accountDB.values();
        for (Account account : accounts) {
            synchronized (DBUtil.UTXO){
                Set<TransactionOutput> transactionOutputs = DBUtil.UTXO.get(account.getAddress());
                if(transactionOutputs != null){
                    Iterator<TransactionOutput> iterator = transactionOutputs.iterator();
                    while (iterator.hasNext()){
                        balance += iterator.next().getAmount();
                    }
                }
            }
        }
        return balance;
    }

    /**
     * 构建挖矿激励交易
     * 挖出block的节点会得到50块作为奖励。这50块可以认为是系统发放的，但是去中心化的区块链中并没有「系统」这一方，
     * 大家都是平等的，于是挖到矿的节点构建一个特殊的交易，没有输入，只有给自己50块的输出。其他节点接收到之后会先验证确实你挖出block了，
     * 才会承认这笔奖励交易。
     * @return 奖励的交易
     */
    public Transaction genIncentives(Account account){
        Transaction transaction = new Transaction();
        transaction.setType(Transaction.TRANSACTION_TYPE_INCENTIVE);
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setAmount(Miner.incentives);
        transaction.setPayee(account.getAddress());
        transaction.setNonce(ByteUtil.bytesToUint64(ByteUtil.random32Bytes()));
        String hash = TransactionUtil.calculateHash(transaction);
        transaction.setHash(hash);
        TransactionOutput transactionOutput = new TransactionOutput();
        transactionOutput.setTransactionHash(hash);
        transactionOutput.setIndex(0);
        transactionOutput.setAmount(Miner.incentives);
        transactionOutput.setAccount(account.getAddress());
        List<TransactionOutput> outputs = new ArrayList<>();
        outputs.add(transactionOutput);
        transaction.setOutputs(outputs);
        return transaction;
    }
}
