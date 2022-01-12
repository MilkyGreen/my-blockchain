package com.milkygreen.blockchain.core;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author yunmeng.li
 * @version 1.0.0
 */
public class TransactionOutput implements Serializable {

    /**
     * 交易hash
     * 该输入隶属于哪个交易
     */
    private String transactionHash;

    /**
     * 金额
     */
    private long amount;

    /**
     * 收款人的账号（地址）
     */
    private String account;

    /**
     * 在交易中输出列表中的索引
     * Transaction中有个输出列表，此字段代表改输出是第几个输出
     */
    private int index;

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionOutput that = (TransactionOutput) o;
        return amount == that.amount &&
                index == that.index &&
                Objects.equals(transactionHash, that.transactionHash) &&
                Objects.equals(account, that.account);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionHash, amount, account, index);
    }
}
