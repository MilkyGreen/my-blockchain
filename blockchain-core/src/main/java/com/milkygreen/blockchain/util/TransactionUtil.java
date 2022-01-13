package com.milkygreen.blockchain.util;

import com.milkygreen.blockchain.core.Transaction;
import com.milkygreen.blockchain.core.TransactionInput;

import java.util.ArrayList;
import java.util.List;

/**
 * 交易相关工具类
 */
public class TransactionUtil {

    /**
     * 计算交易的hash值
     * @param transaction 交易
     * @return 交易hash
     */
    public static String calculateHash(Transaction transaction){
        String msg = transaction.getPayee() + transaction.getAmount() + transaction.getTimestamp() + transaction.getNonce();
        return ByteUtil.bytesToHexString(CryptoUtil.doubleDigest(ByteUtil.stringToUtf8Bytes(msg)));
    }

    /**
     * 给交易签名
     * @param privateKey 私钥
     * @param transactionInput 交易输入
     * @return 签名
     */
    public static String signature(String privateKey, TransactionInput transactionInput){
        String data = transactionInput.getTransactionHash() + "-" + JsonUtil.toJson(transactionInput.getUnspentOutput());
        return CryptoUtil.signature(privateKey,ByteUtil.hexStringToBytes(data));
    }

    /**
     * 验证交易的签名
     * @param pubicKey 公钥
     * @param signature 签名
     * @param transactionInput 交易输入
     * @return 是否验证通过
     */
    public static boolean validateSignature(String pubicKey,String signature,TransactionInput transactionInput){
        String data = transactionInput.getTransactionHash() + "-" + JsonUtil.toJson(transactionInput.getUnspentOutput());
        return CryptoUtil.verifySignature(pubicKey,ByteUtil.hexStringToBytes(data),ByteUtil.hexStringToBytes(signature));
    }

    /**
     * 创建交易列表的默克尔树
     * @param list 交易列表
     * @return 默克尔树hash值
     */
    public static String genMerkleTree(List<Transaction> list){
        List<byte[]> bytesList = new ArrayList<>();
        list.forEach(transaction -> bytesList.add(ByteUtil.hexStringToBytes(transaction.getHash())));
        byte[] bytes = CryptoUtil.calculateMerkleTreeRoot(bytesList);
        return ByteUtil.bytesToHexString(bytes);
    }
}
