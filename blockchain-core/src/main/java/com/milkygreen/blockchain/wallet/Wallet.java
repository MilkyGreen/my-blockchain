package com.milkygreen.blockchain.wallet;

import java.util.List;
import java.util.Set;

/**
 *  钱包。
 *  提供查询、转账、新增账户、删除账户等功能。
 */
public class Wallet {

    /**
     * 账户集合
     */
    private Set<Account> accounts;

    /**
     * 新增账户
     * @param account
     */
    public void addAccount(Account account){
        accounts.add(account);
    }

    /**
     * 删除账户
     * @param address
     */
    public void removeAccount(String address){
        Account target = null;
        for (Account account : accounts) {
            if(account.getAddress().equals(address)){
                target = account;
            }
        }
        accounts.remove(target);
    }

    /**
     * 给指定账户付款
     * @param amount 金额
     * @param payee 收款地址
     * 1、第一步从自己的钱包中查询是否有足够的「未话费输出」用来支付
     * 2、凑出的金额是否大于支付金额，多的话需要给自己找零
     * 3、构建交易，放到未确认交易池中，等待被挖
     */
    public void pay(long amount,String payee){

    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }
}
