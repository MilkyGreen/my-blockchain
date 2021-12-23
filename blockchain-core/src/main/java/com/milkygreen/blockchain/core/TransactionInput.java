package com.milkygreen.blockchain.core;

import java.io.Serializable;
import java.util.List;

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
    private List<TransactionOutput> outputs;

    /**
     * 此输出的金额
     * 可以由所有的output的金额相加得来
     */
    private double amount;

    /**
     * 付款人的账号
     */
    private String payer;


    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<TransactionOutput> outputs) {
        this.outputs = outputs;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPayer() {
        return payer;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }
}
