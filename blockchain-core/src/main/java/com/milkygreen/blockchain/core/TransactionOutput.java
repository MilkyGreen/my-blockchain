package com.milkygreen.blockchain.core;

import java.io.Serializable;
import java.util.List;

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
    private double amount;

    /**
     * 收款人的账号（地址）
     */
    private String account;

    /**
     * 在交易中输出列表中的索引
     * Transaction中有个输出列表，此字段代表改输出是第几个输出
     */
    private int index;

}
