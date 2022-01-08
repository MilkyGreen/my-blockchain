package com.milkygreen.blockchain.core;

import com.milkygreen.blockchain.db.DBUtil;

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
