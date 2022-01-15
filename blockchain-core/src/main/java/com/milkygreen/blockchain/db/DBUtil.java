package com.milkygreen.blockchain.db;

import com.milkygreen.blockchain.core.Block;
import com.milkygreen.blockchain.core.Transaction;
import com.milkygreen.blockchain.core.TransactionOutput;
import com.milkygreen.blockchain.wallet.Account;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 持久化工具，用于保存区块链上的所以数据(目前仅保存在内存中，后续可以用一些kv数据库实现本地保存)
 */
public class DBUtil {

    /**
     * 区块链的最新高度
     */
    public static long blockchainHeight = -1;

    /**
     * hash-block 结构
     */
    public static Map<String, Block> hashBlockDB = new ConcurrentHashMap<>();

    /**
     * block的height-hash 结构
     */
    public static Map<Long, String> HeightHashBlockDB = new ConcurrentHashMap<>();

    /**
     * hash-transaction结构
     */
    public static Map<String, Transaction> hashTransactionDB = new ConcurrentHashMap<>();

    /**
     * 未花费输出,代表一个账户的「余额」
     * 地址-transactionOutput
     */
    public static final Map<String, Set<TransactionOutput>> UTXO = new ConcurrentHashMap<>();

    /**
     * 未确认交易
     * 区块链网络中发生的交易，还没有被打包进任何一个区块，暂时放在这里，等矿工来取
     */
    public static Map<String, Transaction> unConfirmTransactionPool = new ConcurrentHashMap<>();


    public synchronized static Map<String, Set<TransactionOutput>> getUTXO(){
        return UTXO;
    }

    /**
     * 获取最新区块
     *
     * @return 区块
     */
    public static Block getTailBlock() {
        String hash = HeightHashBlockDB.get(blockchainHeight);
        if(hash == null){
            return null;
        }
        Block block = hashBlockDB.get(hash);
        return block;
    }


}
