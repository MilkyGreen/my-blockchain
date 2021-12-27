package com.milkygreen.blockchain.wallet;

import java.util.Objects;

/**
 * 账号，由公钥和私钥组成，公钥相当于账户、私钥相当于密码，代表了区块链世界中的一个唯一的地址或账户，区块链中的所有财产、信息都
 * 归属于某一个账户。同时由于各个账户的状态是公开的，除非拥有秘钥，否则谁也无法强制将财产从一个账户中转移。
 * 同时该账户由用户自己生成，并不需要借助于第三方或者现实世界，因此具有匿名性。
 */
public class Account {

    /**
     * 私钥字符串形式。
     * 账户主要由公私钥构成。简单的理解，私钥是一个随机的、很大的数字，由可靠的随机算法生成。
     * 有了私钥之后，通过椭圆随机算法，生成公钥。
     * 也就是说私钥可以推倒出公钥，但是公钥是无法推导出私钥的。
     * 公私钥可以互相解密彼此加密的信息：私钥加密-公钥解密、公钥加密-私钥解密
     */
    private String privateKey;

    /**
     * 公钥字符串
     */
    private String publicKey;

    /**
     * 账户地址
     * 账户地址本质上时公钥，但是公钥的字符形式太长，因此对公钥进行两次hash之后，再base58编码得到地址，更利于人眼阅读和发送。
     * 区块链上的资产会以地址关联保存。
     */
    private String address;

    public Account(String privateKey, String publicKey, String address) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(privateKey, account.privateKey) &&
                Objects.equals(publicKey, account.publicKey) &&
                Objects.equals(address, account.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(privateKey, publicKey, address);
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
