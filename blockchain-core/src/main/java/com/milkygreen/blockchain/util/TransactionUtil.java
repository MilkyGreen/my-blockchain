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
     * @param transaction
     * @return
     */
    public static String calculateHash(Transaction transaction){
        String msg = transaction.getPayee() + transaction.getAmount() + transaction.getTimestamp() + transaction.getNonce();
        return ByteUtil.bytesToHexString(CryptoUtil.doubleDigest(ByteUtil.stringToUtf8Bytes(msg)));
    }

    /**
     * 给交易签名
     * @param privateKey
     * @return
     */
    public static String signature(String privateKey, TransactionInput transactionInput){
        String data = transactionInput.getTransactionHash() + "-" + JsonUtil.toJson(transactionInput.getUnspentOutput());
        return CryptoUtil.signature(privateKey,ByteUtil.hexStringToBytes(data));
    }

    /**
     * 验证交易的签名
     * @param pubicKey
     * @param signature
     * @return
     */
    public static boolean validateSignature(String pubicKey,String signature,TransactionInput transactionInput){
        String data = transactionInput.getTransactionHash() + "-" + JsonUtil.toJson(transactionInput.getUnspentOutput());
        return CryptoUtil.verifySignature(pubicKey,ByteUtil.hexStringToBytes(data),ByteUtil.hexStringToBytes(signature));
    }

    public static String genMerkleTree(List<Transaction> list){
        List<byte[]> bytesList = new ArrayList<>();
        list.forEach(transaction -> bytesList.add(ByteUtil.hexStringToBytes(transaction.getHash())));
        byte[] bytes = CryptoUtil.calculateMerkleTreeRoot(bytesList);
        return ByteUtil.bytesToHexString(bytes);
    }
}
