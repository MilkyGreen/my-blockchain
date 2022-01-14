package com.milkygreen.blockchain.core;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author yunmeng.li
 * @version 1.0.0
 * 交易。代表一个或多个账户给另一个账户的转账操作
 * 区块链不像银行系统那样有人专门记录每个人的余额，而是从每一笔转账记录中计算出某账号的余额。
 * 所有的交易记录都存在区块链上，每一个节点都可以查询到任何一个交易记录。
 */
public class Transaction implements Serializable {

    /**
     * 交易类型：挖矿激励
     */
    public final static int TRANSACTION_TYPE_INCENTIVE = 0;

    /**
     * 交易类型：普通交易
     */
    public final static int TRANSACTION_TYPE_NORMAL = 1;

    /**
     * 交易的hash值
     */
    private String hash;

    /**
     * 交易的输入。
     * 需要从转账发起人的「未花费输出」中扣除，作为输入的来源
     */
    private List<TransactionInput> inputs;

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

    /**
     * 收款方。
     * 一个交易应该只有一个收款方
     */
    private String payee;

    /**
     * 交易生成时间戳
     */
    private long timestamp;

    /**
     * 随机数，用来参与hash（并不像block一样需要达到某个目标，只是为了增加伪造的难度）
     */
    private long nonce;

    /**
     * 交易类型：
     * 0 挖矿奖励
     * 1 普通交易
     */
    private int type;

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<TransactionInput> inputs) {
        this.inputs = inputs;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<TransactionOutput> outputs) {
        this.outputs = outputs;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
