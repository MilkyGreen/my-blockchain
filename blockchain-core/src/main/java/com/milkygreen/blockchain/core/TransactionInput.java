package com.milkygreen.blockchain.core;

import java.io.Serializable;

/**
 * 交易输入
 */
public class TransactionInput implements Serializable {

    /**
     * 交易hash
     * 该输入隶属于哪个交易
     */
    private String transactionHash;

    /**
     * 组成该交易输入的输出
     * 输入的金额不是凭空而来，而是从之前的交易生成的输出得来的。
     * 你想给别人转账，提前是必须别人先给你转过帐，而且你还没花出去。
     */
    private TransactionOutput unspentOutput;

    /**
     * 数字签名
     * 用来验证这笔转账确实是payer发起的，而不是别人伪造的。
     *
     */
    private String signature;

    /**
     * 付款人公钥，用来让别人对签名进行验证
     */
    private String publicKey;

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public TransactionOutput getUnspentOutput() {
        return unspentOutput;
    }

    public void setUnspentOutput(TransactionOutput unspentOutput) {
        this.unspentOutput = unspentOutput;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
