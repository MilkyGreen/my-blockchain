package com.milkygreen.blockchain.core;

import com.milkygreen.blockchain.db.DBUtil;
import com.milkygreen.blockchain.util.ByteUtil;
import com.milkygreen.blockchain.util.CryptoUtil;
import com.milkygreen.blockchain.util.TransactionUtil;
import com.milkygreen.blockchain.wallet.Account;
import com.milkygreen.blockchain.wallet.Wallet;

import java.math.BigInteger;
import java.util.*;

/**
 * 矿工
 * 挖矿是指将还没有被打包进区块的、已经经过验证的交易，打包到一个block中，并且计算出正确的hash值。
 * 挖出一个正确的block之后，应当立刻广播到整个区块链网络，其他节点验证无误之后便会接受这个block。
 * 挖出区块的节点，可以给自己发放50个币作为奖励，一并放到block里面。
 */
public class Miner implements Runnable {

    /**
     * 奖励金额。
     * 为何要给矿工奖励？因为交易是区块链的核心，加入到了区块链中的交易才会被承认。节点多的区块链网络中每时每刻都会有交易产生，它们都亟待被打包到区块链中获得确认。
     * 矿工需要耗费很多计算资源来打包这些交易，因此需要给与奖励来激励更多矿工参与挖矿，否则没人愿意挖矿，这个经济体系就运行不起来。
     * 在真正的区块链网络中，这个值应该是会自动调整的，因为币不能无限超发，会引起通货膨胀。
     * 例如在比特币网络中，发币到一定数量之后挖矿的奖励会变成从交易中抽取手续费。
     */
    public final static long incentives = 50;

    /**
     * 挖矿难度。
     * 这个字段规定了block的hash值合法规则：hash值必须小于difficulty，也就是必须小于某个数。
     * 矿工必须不断随机出nonce值来参与hash运算，直到满足要求，这样的block才会被别人承认。
     * 为何要设置挖矿难度？有了难度之后，篡改的成本变的很高。由于block包含了前一个block的hash值，修改一个block的内容就
     * 必须把后面的block都重新计算一遍。假设没有任何难度，一个人想把某个时间点之后block数据全改掉是比较容易的，
     * 他自己可以疯狂的生成block然后接到某个合法block后面，长度比合法的链还要长，那么他有可能就成了主链，别人就要接受他的链。
     * 有了挖矿难度，想要上面这样操作会慢很多，非法的链长度很难超过合法的链。这样就保证了区块链的信息一经记录就几乎无法修改。
     * <p>
     * 在完善的区块链系统中，难度是会动态调整的：挖矿的人多，难度就提高。挖矿的人少，难度就降低。确保block生产的速度比较稳定。
     */
    public final static String difficulty = "00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

    /**
     * 矿工是否在工作
     */
    public static boolean isActive = true;

    /**
     * 一个block中的交易数量限制，确保一个block不会太大
     */
    public static long transactionLimit = 1000;

    /**
     * 钱包实例，挖矿的奖励要放到这里
     */
    private Wallet wallet;

    /**
     * 区块链实例，在这个区块链上挖矿
     */
    private Blockchain blockchain;

    public Blockchain getBlockchain() {
        return blockchain;
    }

    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    @Override
    public void run() {
        while (true) {
            if (isActive) {
                // 获取当前的最新区块
                Block tailBlock = DBUtil.getTailBlock();
                String preHash = tailBlock.getHash();
                long preHeight = tailBlock.getHeight();
                // 收集未确认交易
                List<Transaction> transactions = collectUnConfirmTransactions();
                if(transactions == null || transactions.size() == 0){
                    continue;
                }
                Account account = CryptoUtil.randomAccount();
                // 加入给自己的激励
                Transaction incentivesTransaction = wallet.genIncentives(account);
                transactions.add(incentivesTransaction);
                // 构建block对象，计算hash
                Block block = new Block();
                block.setPreHash(preHash);
                block.setHeight(preHeight+1);
                block.setTransactions(transactions);
                block.setTimestamp(System.currentTimeMillis());
                block.setMerkleTree(TransactionUtil.genMerkleTree(transactions));
                block.setType(Block.NORMAL_BLOCK);
                block.setNonce(ByteUtil.bytesToUint64(ByteUtil.random32Bytes()));
                String hash = Block.calculateHash(block);
                block.setHash(hash);
                // 不停的随机nonce并计算hash，直到符合difficulty要求。
                while(new BigInteger(difficulty,16).compareTo(new BigInteger(block.getHash(),16)) <= 0){
                    block.setNonce(ByteUtil.bytesToUint64(ByteUtil.random32Bytes()));
                    hash = Block.calculateHash(block);
                    block.setHash(hash);
                }
                // 挖矿成功，将奖励放入钱包，尽快广播block!
                wallet.addAccount(account);
                System.out.println("挖矿成功！");
                this.sendBlock(block);
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将挖到的block发送出去
     * @param block 区块
     */
    public void sendBlock(Block block){
        this.blockchain.addBlock(block);
    }

    /**
     * 获取一批未确认交易列表
     *
     * @return List<Transaction>
     */
    public List<Transaction> collectUnConfirmTransactions() {
        Map<String, Transaction> unConfirmTransactionPool = DBUtil.unConfirmTransactionPool;
        if (unConfirmTransactionPool.size() > 0) {
            return null;
        }
        // 要按时间顺序处理交易
        List<Transaction> transactions = new ArrayList<>(unConfirmTransactionPool.values());
        transactions.sort((Transaction o1, Transaction o2) -> o1.getTimestamp() > o2.getTimestamp() ? 1 : -1);
        List<Transaction> ret = new ArrayList<>();
        for (int i = 0; i < transactionLimit && i < transactions.size(); i++) {
            ret.add(transactions.get(i));
        }
        return ret;
    }
}
