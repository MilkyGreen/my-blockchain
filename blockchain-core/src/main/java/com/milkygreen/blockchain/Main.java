package com.milkygreen.blockchain;

import com.milkygreen.blockchain.core.Block;
import com.milkygreen.blockchain.core.Blockchain;
import com.milkygreen.blockchain.core.Miner;
import com.milkygreen.blockchain.db.DBUtil;
import com.milkygreen.blockchain.util.CryptoUtil;
import com.milkygreen.blockchain.wallet.Account;
import com.milkygreen.blockchain.wallet.Wallet;

/**
 * 模拟测试
 */
public class Main {

    public static void main(String[] args) {
        Blockchain blockchain = new Blockchain();
        Wallet wallet1 = new Wallet();
        Block genesisBlock = blockchain.createGenesisBlock(wallet1);
        blockchain.addBlock(genesisBlock);
        System.out.println("init成功！wallet1当前余额有=" + wallet1.getBalance());

        Wallet wallet2 = new Wallet();
        Account account = CryptoUtil.randomAccount();
        wallet2.addAccount(account);

        wallet1.pay(10, account.getAddress());

        System.out.println("当前未确认交易数量：" + DBUtil.unConfirmTransactionPool.size());

        Miner miner1 = new Miner(wallet1, blockchain);
        Thread miner1Thread = new Thread(miner1);
        miner1Thread.start();
        System.out.println("矿工1开始挖矿...");
        while (true) {
            long balance = wallet2.getBalance();
            if (balance == 10) {
                System.out.println("钱包2收到了转账！余额=" + balance);
                System.out.println("钱包1余额=" + wallet1.getBalance());
                break;
            }
        }
        miner1Thread.interrupt();
        System.out.println("矿工1暂停挖矿...");

        Miner miner2 = new Miner(wallet2, blockchain);
        Thread miner2Thread = new Thread(miner2);
        miner2Thread.start();
        System.out.println("矿工2开始挖矿...");

        wallet1.pay(50, account.getAddress());

        while (true) {
            long balance = wallet2.getBalance();
            if (balance == 110) {
                System.out.println("钱包2收到了转账！余额=" + balance);
                System.out.println("钱包1余额=" + wallet1.getBalance());
                break;
            }
        }
        miner2Thread.interrupt();
    }
}
