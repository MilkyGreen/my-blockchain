package com.milkygreen.blockchain.core;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 *     区块，一个区块包含了一系列交易
 */
public class Block implements Serializable {

    /**
     * 整个区块的hash值，标识一个唯一的区块。
     * 区块的hash值必须符合一定的条件才会被认为合法，所以这个值是需要反复计算直到符合条件，就是所谓的「挖矿」。
     * 挖出区块的人，由于贡献了自己的计算资源，会得到一定的代币奖励。
     *
     */
    private String hash;

    /**
     * 上个区块的hash值。
     * 区块链类似一个区块组成的单链表，当前区块记录着上一个区块的hash，相当于指针。
     */
    private String preHash;

    /**
     * 区块的高度。
     * 即当前区块是区块链上的第几个区块。
     * 从1开始计算
     */
    private long height;

    /**
     * 当前区块链上包含的交易信息。
     * 当一个交易发生之后，不会立刻生效，只有被成功打包到一个区块，并且添加到区块链上之后，才能确认交易生效了。
     * 生产区块的时候，需要从未所有未确认的交易中挑选一个定数量的交易作为自己的数据。
     * 一个不包含任何交易的区块是没有意义的。
     */
    private List<Transaction> transactions;

    /**
     * 创建时间
     * 区块生成的时间
     */
    private long timestamp;

    /**
     * 随机字符串
     * 由于需要挖出正确的区块hash，所以每次都需要随机一个字符串参与hash计算，使得计算结果不同，直到算出一个符合条件的hash
     */
    private long nonce;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreHash() {
        return preHash;
    }

    public void setPreHash(String preHash) {
        this.preHash = preHash;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
}
