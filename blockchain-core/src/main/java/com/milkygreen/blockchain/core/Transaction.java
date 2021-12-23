package com.milkygreen.blockchain.core;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author yunmeng.li
 * @version 1.0.0
 * 交易。代表一个账户给另一个或多个账户的转账操作
 * 区块链不像银行系统那样有人专门记录每个人的余额，而是从每一笔转账记录中计算出某账号的余额。
 * 所有的交易记录都存在区块链上，每一个区块链节点可以查询到任何一个交易记录。
 */
public class Transaction implements Serializable {

    /**
     * 交易的hash值
     */
    private String hash;

    /**
     * 交易的输入。
     * 需要从转账发起人的「余额」中扣除，作为输入的来源
     */
    private TransactionInput input;

    /**
     * 交易的输出列表
     * 代表接收转账的账户。
     * 注意，这里面有可能包含付款方自己的账号，比如说付款方有一个5块和一个4块的入账（output），他要向a转6块钱，这时他
     * 不可以把5块和4块的入账合并起来的，因为区块链没有「余额」的概念，转账记录无法合并、拆分。他只能把自己的两个output全部
     * 拿出来组成一个Input，然后设置两个收款人，一个是a，金额是6块，另一个是自己，金额是3块。
     * 转给自己的这个output称作找零输出。
     */
    private List<TransactionOutput> outputs;

    /**
     * 交易金额
     * 等于输入或输出的金额
     */
    private long amount;

}
