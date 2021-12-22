package com.milkygreen.blockchain.db;

import com.milkygreen.blockchain.core.Block;

import java.util.HashMap;
import java.util.Map;

/**
 * 持久化工具，用于保存区块链上的所以数据(目前仅保存在内存中，后续可以用一些kv数据库实现本地保存)
 *
 */
public class DBUtil {

    /**
     * hash-block 结构
     */
    public static Map<String, Block> hashBlockDB = new HashMap<>();

    /**
     * block的height-hash 结构
     */
    public static Map<Long, String> HeightHashBlockDB = new HashMap<>();

    /**
     * hash-transaction结构
     */
    public static Map<String, Block> hashTransactionDB = new HashMap<>();


}
