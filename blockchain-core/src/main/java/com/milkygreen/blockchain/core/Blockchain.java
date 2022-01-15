package com.milkygreen.blockchain.core;

import com.milkygreen.blockchain.db.DBUtil;
import com.milkygreen.blockchain.util.ByteUtil;
import com.milkygreen.blockchain.util.CryptoUtil;
import com.milkygreen.blockchain.util.TransactionUtil;
import com.milkygreen.blockchain.wallet.Account;
import com.milkygreen.blockchain.wallet.Wallet;

import java.math.BigInteger;
import java.util.*;

/**
 * 区块链
 * 区块以链表的形式连接起来，构成区块链。
 * 可以理解为一个append only的分布式日志数据库，记录的是一笔笔的交易。新的区块产生之后会被追加到区块链的尾部，作为区块链的一部分。
 * 每一个节点（peer）都保存了一份区块链的完整数据，大家通过相同的方法来管理各自的区块链，最终达到数据一致。
 * （在大部分完善的区块链系统中，并不是所有节点都保存完整的区块链数据，一些轻量节点只保存部分，一些钱包节点则只负责交易，数据交给完整节点去校验）
 */
public class Blockchain {

    /**
     * 新增一个区块到区块链尾部
     * 一个区块需要通过一系列校验才能被接受。如:区块本身的hash值合法、区块上的交易数据结构正确、区块不包含重复的交易等等
     *
     * @param block 区块
     */
    public void addBlock(Block block) {
        // 判断接收到的区块是否是本地最新高度加一
        Block preBlock = getTailBlock();
        if(preBlock != null){
            if (!preBlock.getHash().equals(block.getPreHash()) || preBlock.getHeight() + 1 != block.getHeight()) {
                System.out.println("接收到非法的区块！");
                return;
            }
        }

        // 校验区块hash
        String hash = Block.calculateHash(block);
        if (!hash.equals(block.getHash())) {
            System.out.println("区块的hash值不正确！");
            return;
        }
        if (new BigInteger(Miner.difficulty, 16).compareTo(new BigInteger(block.getHash(), 16)) <= 0) {
            System.out.println("区块的hash值不符合difficulty要求！");
            return;
        }
        List<Transaction> transactions = block.getTransactions();
        String merkleTree = TransactionUtil.genMerkleTree(transactions);
        if (!merkleTree.equals(block.getMerkleTree())) {
            System.out.println("区块的merkleTree值不正确！");
            return;
        }
        // 校验每笔交易是否合法、有重复花费
        if (!checkTransactions(transactions)) {
            return;
        }

        // 校验通过之后，数据存入本地
        this.saveBlock(block);
        System.out.println("新区块已接受！");
    }

    /**
     * 将区块保存到本地区块链上
     *
     * @param block Block
     * @return
     */
    private void saveBlock(Block block) {
        // 保存区块高度
        DBUtil.blockchainHeight = block.getHeight();
        // 保存hash-区块
        DBUtil.hashBlockDB.put(block.getHash(), block);
        DBUtil.HeightHashBlockDB.put(block.getHeight(), block.getHash());
        // 保存交易数据
        List<Transaction> transactions = block.getTransactions();
        for (Transaction transaction : transactions) {
            // 从未确认交易池中删除
            Map<String, Transaction> unConfirmTransactionPool = DBUtil.unConfirmTransactionPool;
            unConfirmTransactionPool.remove(transaction.getHash());

            // 保存交易本身
            DBUtil.hashTransactionDB.put(transaction.getHash(), transaction);

            if(transaction.getType() == Transaction.TRANSACTION_TYPE_NORMAL){
                List<TransactionInput> inputs = transaction.getInputs();
                for (TransactionInput transactionInput : inputs) {
                    // 将input中的UTXO，从付款人账户中删除
                    TransactionOutput unspentOutput = transactionInput.getUnspentOutput();
                    Set<TransactionOutput> transactionOutputs = DBUtil.UTXO.get(unspentOutput.getAccount());
                    transactionOutputs.remove(unspentOutput);
                    DBUtil.UTXO.put(unspentOutput.getAccount(), transactionOutputs);
                }
            }

            // 将新的output放入到相应收款人的账户中
            List<TransactionOutput> outputs = transaction.getOutputs();
            for (TransactionOutput transactionOutput : outputs) {
                synchronized (DBUtil.UTXO){
                    Set<TransactionOutput> transactionOutputs = DBUtil.UTXO.getOrDefault(transactionOutput.getAccount(),new HashSet<>());
                    transactionOutputs.add(transactionOutput);
                    DBUtil.UTXO.put(transactionOutput.getAccount(), transactionOutputs);
                }
            }
        }
        System.out.println("区块入库成功！");
    }

    /**
     * 校验每笔交易的正确性：input是否正确、签名是否正确、是否有重复的未花费输出
     *
     * @param transactions
     * @return
     */
    private boolean checkTransactions(List<Transaction> transactions) {
        Set<TransactionOutput> outputSet = new HashSet<>();

        for (Transaction transaction : transactions) {

            String hash = TransactionUtil.calculateHash(transaction);
            if (!hash.equals(transaction.getHash())) {
                System.out.println("交易的hash值非法！");
                return false;
            }

            List<TransactionInput> inputs = transaction.getInputs();
            long inputAmount = 0;
            // 如果是普通交易，需要验证Input的来源。
            if(transaction.getType() == Transaction.TRANSACTION_TYPE_NORMAL ){
                for (TransactionInput input : inputs) {
                    if (!checkTransactionInput(input)) {
                        return false;
                    }
                    TransactionOutput unspentOutput = input.getUnspentOutput();
                    if (outputSet.contains(unspentOutput)) {
                        System.out.println("交易中含有重复的未花费输出！");
                        return false;
                    }
                    outputSet.add(unspentOutput);
                    inputAmount += unspentOutput.getAmount();
                }
            }
            List<TransactionOutput> outputs = transaction.getOutputs();
            int outputAmount = 0;
            for (TransactionOutput output : outputs) {
                outputAmount += output.getAmount();
            }
            if(transaction.getType() == Transaction.TRANSACTION_TYPE_NORMAL){
                if (inputAmount != outputAmount) {
                    System.out.println("交易的输入金额不匹配！");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 校验交易的TransactionInput是否合法。
     * 首先校验所使用的未花费输出是否存在
     * 其次校验交易签名是否正确，即是否是UTXO持有者发起的交易
     *
     * @param input
     * @return
     */
    public boolean checkTransactionInput(TransactionInput input) {
        TransactionOutput unspentOutput = input.getUnspentOutput();
        Set<TransactionOutput> transactionOutputs = DBUtil.UTXO.get(unspentOutput.getAccount());
        if (transactionOutputs.contains(unspentOutput)) {
            String signature = input.getSignature();
            if (!TransactionUtil.validateSignature(input.getPublicKey(), signature, input)) {
                System.out.println("交易的input签名未校验通过！");
                return false;
            }
            return true;
        } else {
            System.out.println("试图花费不存在的UTXO！");
            return false;
        }
    }

    /**
     * 构建创世区块
     * 什么是创世区块？区块是由交易构成的，必须有交易产生之后矿工才能构建区块，交易需要有输入，输入需要有输出...
     * 一个区块链系统第一次运行的时候，是没有任何人有余额的,没有余额无法交易，无法交易就无法挖矿，无法挖矿就没有奖励（无法新增货币）
     * 因此必须在一开始初始化出一些货币出来，以便可以发起后续的交易。这些初始的货币也是以交易的形式发放，该交易所在的区块叫创世区块，是区块链中的第一个区块。
     * @param wallet 创建节点的钱包，用于发放激励
     */
    public Block createGenesisBlock(Wallet wallet){
        // 创建一个挖矿激励交易给初始节点
        Account account = CryptoUtil.randomAccount();
        Transaction transaction = wallet.genIncentives(account);

        String merkleTree = TransactionUtil.genMerkleTree(Collections.singletonList(transaction));
        Block block = new Block();
        block.setMerkleTree(merkleTree);
        block.setHeight(0);
        block.setTimestamp(System.currentTimeMillis());
        block.setTransactions(Collections.singletonList(transaction));
        block.setType(Block.GENESIS_BLOCK);
        block.setNonce(ByteUtil.bytesToUint64(ByteUtil.random32Bytes()));
        String hash = Block.calculateHash(block);
        block.setHash(hash);
        // 创世区块依然需要符合hash难度要求（在动态调整难度的区块链系统中，初始的时候难度应该是很小的）
        while(new BigInteger(Miner.difficulty,16).compareTo(new BigInteger(block.getHash(),16)) <= 0){
            block.setNonce(ByteUtil.bytesToUint64(ByteUtil.random32Bytes()));
            hash = Block.calculateHash(block);
            block.setHash(hash);
        }
        wallet.addAccount(account);
        System.out.println("创世区块构建成功！");
        return block;
    }

    /**
     * 根据hash查询一个区块
     *
     * @param hash 区块hash
     * @return 区块
     */
    public Block getBlockByHash(String hash) {
        return DBUtil.hashBlockDB.get(hash);
    }

    /**
     * 根据区块高度查询区块
     *
     * @param height 区块高度
     * @return 区块
     */
    public Block getBlockByHeight(long height) {
        return DBUtil.hashBlockDB.get(DBUtil.HeightHashBlockDB.get(height));
    }

    /**
     * 根据交易hash查询交易
     *
     * @param hash 交易hash
     * @return 交易
     */
    public Transaction getTransactionByHash(String hash) {
        return DBUtil.hashTransactionDB.get(hash);
    }

    /**
     * 获取当前链上的最新区块
     *
     * @return 区块
     */
    public Block getTailBlock() {
        return DBUtil.getTailBlock();
    }

}
