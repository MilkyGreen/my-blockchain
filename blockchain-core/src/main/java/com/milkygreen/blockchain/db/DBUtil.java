package com.milkygreen.blockchain.db;

import com.milkygreen.blockchain.core.Block;
import com.milkygreen.blockchain.core.Transaction;
import com.milkygreen.blockchain.core.TransactionOutput;
import com.milkygreen.blockchain.wallet.Account;

import java.util.*;

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

    /**
     * 未花费输出,代表一个账户的「余额」
     * 地址-transactionOutput
     */
    public static Map<String, Set<TransactionOutput>> UTXO = new HashMap<>();

    /**
     * 未确认交易
     * 区块链网络中发生的交易，还没有被打包进任何一个区块，暂时放在这里，等矿工来取
     */
    public static Set<Transaction> unConfirmTransactionPool = new HashSet<>();

    /**
     * 地址-账号
     */
    public static Map<String, Account> accountDB = new HashMap<>();
}
