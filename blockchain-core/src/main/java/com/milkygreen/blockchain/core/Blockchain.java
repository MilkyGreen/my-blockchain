package com.milkygreen.blockchain.core;

import com.milkygreen.blockchain.db.DBUtil;
import com.milkygreen.blockchain.util.TransactionUtil;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yunmeng.li
 * @version 1.0.0
 * 区块链，提供对这个链上数据的所有操作方法
 */
public class Blockchain {

    /**
     * 新增一个区块到区块链尾部
     * 一个区块需要通过一系列校验才能被接受。如:区块本身的hash值合法、区块上的交易数据结构正确、区块不包含重复的交易等等
     * @param block 区块
     */
    public void addBlock(Block block){
        // 判断接收到的区块是否是本地最新高度加一
        Block preBlock = getTailBlock();
        if(!preBlock.getHash().equals(block.getPreHash()) || preBlock.getHeight()+1 != block.getHeight()){
            System.out.println("接收到非法的区块！");
            return;
        }
        // 校验区块hash
        String hash = Block.calculateHash(block);
        if(!hash.equals(block.getHash())){
            System.out.println("区块的hash值不正确！");
            return;
        }
        if(new BigInteger(Miner.difficulty,16).compareTo(new BigInteger(block.getHash(),16)) <= 0){
            System.out.println("区块的hash值不符合difficulty要求！");
            return;
        }
        List<Transaction> transactions = block.getTransactions();
        String merkleTree = TransactionUtil.genMerkleTree(transactions);
        if(!merkleTree.equals(block.getMerkleTree())){
            System.out.println("区块的merkleTree值不正确！");
            return;
        }
        // 校验每笔交易是否合法、有重复花费
        if(!checkTransactions(transactions)){
            return;
        }

        // 校验通过之后，数据存入本地
        this.saveBlock(block);
        System.out.println("新区块已接受！");
    }

    /**
     * 将区块保存到本地区块链上
     * @param block Block
     * @return
     */
    private boolean saveBlock(Block block){

        return true;
    }

    /**
     * 校验每笔交易的正确性：input是否正确、签名是否正确、是否有重复的未花费输出
     * @param transactions
     * @return
     */
    private boolean checkTransactions(List<Transaction> transactions){
        Set<TransactionOutput> outputSet = new HashSet<>();

        for (Transaction transaction : transactions) {

            String hash = TransactionUtil.calculateHash(transaction);
            if(!hash.equals(transaction.getHash())){
                System.out.println("交易的hash值非法！");
                return false;
            }

            List<TransactionInput> inputs = transaction.getInputs();
            long inputAmount = 0;
            for (TransactionInput input : inputs) {
                TransactionOutput unspentOutput = input.getUnspentOutput();
                if(outputSet.contains(unspentOutput)){
                    System.out.println("交易中含有重复的未花费输出！");
                    return false;
                }
                outputSet.add(unspentOutput);
                inputAmount += unspentOutput.getAmount();
                if(!checkTransactionInput(input)){
                    return false;
                }
            }

            List<TransactionOutput> outputs = transaction.getOutputs();
            int outputAmount = 0;
            for (TransactionOutput output : outputs) {
                outputAmount += output.getAmount();
            }

            if(inputAmount != outputAmount || inputAmount != transaction.getAmount() || outputAmount != transaction.getAmount()){
                System.out.println("交易的金额不匹配！");
                return false;
            }
        }
        return true;
    }

    /**
     * 校验交易的TransactionInput是否合法。
     * 首先校验所使用的未花费输出是否存在
     * 其次校验交易签名是否正确，即是否是UTXO持有者发起的交易
     * @param input
     * @return
     */
    public boolean checkTransactionInput(TransactionInput input){
        TransactionOutput unspentOutput = input.getUnspentOutput();
        Set<TransactionOutput> transactionOutputs = DBUtil.UTXO.get(unspentOutput.getAccount());
        if(transactionOutputs.contains(unspentOutput)){
            String signature = input.getSignature();
            if(!TransactionUtil.validateSignature(input.getPublicKey(),signature,input)){
                System.out.println("交易的input签名未校验通过！");
                return false;
            }
            return true;
        }else{
            System.out.println("试图花费不存在的UTXO！");
            return false;
        }
    }

    /**
     * 根据hash查询一个区块
     * @param hash 区块hash
     * @return 区块
     */
    public Block getBlockByHash(String hash){

        return null;
    }

    /**
     * 根据区块高度查询区块
     * @param height 区块高度
     * @return 区块
     */
    public Block getBlockByHeight(long height){

        return null;
    }

    /**
     * 根据交易hash查询交易
     * @param hash 交易hash
     * @return 交易
     */
    public Transaction getTransactionByHash(String hash){

        return null;
    }

    /**
     * 获取当前链上的最新区块
     * @return 区块
     */
    public Block getTailBlock(){
        return DBUtil.getTailBlock();
    }


}
