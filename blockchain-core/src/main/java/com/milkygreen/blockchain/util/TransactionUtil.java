package com.milkygreen.blockchain.util;

import com.milkygreen.blockchain.core.Transaction;

/**
 * 交易相关工具类
 */
public class TransactionUtil {

    /**
     * 计算交易的hash值
     * @param transaction
     * @return
     */
    public static String calculateHash(Transaction transaction){
        String msg = transaction.getPayee() + transaction.getAmount() + JsonUtil.toJson(transaction.getInputs()) +
                JsonUtil.toJson(transaction.getOutputs());
        return ByteUtil.bytesToHexString(CryptoUtil.doubleDigest(ByteUtil.stringToUtf8Bytes(msg)));
    }

    /**
     * 给交易签名
     * @param privateKey
     * @param hash
     * @return
     */
    public static String signature(String privateKey,String hash){
        return CryptoUtil.signature(privateKey,ByteUtil.hexStringToBytes(hash));
    }

    /**
     * 验证交易的签名
     * @param pubicKey
     * @param signature
     * @param hash
     * @return
     */
    public static boolean validateSignature(String pubicKey,String signature,String hash){
        return CryptoUtil.verifySignature(pubicKey,ByteUtil.hexStringToBytes(hash),ByteUtil.hexStringToBytes(signature));
    }
}
