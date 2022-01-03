package com.milkygreen.blockchain.core;

/**
 * 矿工
 * 挖矿是指将还没有被打包进区块的、已经经过验证的交易，打包到一个block中，并且计算出正确的hash值。
 * 挖出一个正确的block之后，应当立刻广播到整个区块链网络，其他节点验证无误之后便会接受这个block。
 * 挖出区块的节点，可以给自己发放50个币作为奖励，一并放到block里面
 */
public class Miner implements Runnable{

    /**
     * 奖励金额。
     * 为何要给矿工奖励？因为交易是区块链的核心，节点多的区块链网络中每时每刻都会有交易产生，它们都亟待被打包到区块链中获得确认。
     * 矿工需要耗费很多计算资源来打包这些交易，因此需要给与奖励来激励更多矿工参与挖矿，否则没人愿意挖矿这个经济体系就运行不起来。
     * 在真正的区块链网络中，这个值应该是会自动调整的，因为币不能无限超发，会引起通货膨胀。
     * 例如在比特币网络中，发币到一定数量之后挖矿的奖励会变成从交易中抽取手续费。
     */
    public final static long incentives = 50;

    /**
     * 挖矿难度。
     * 这个字段规定了block的hash值合法规则：hash值的16进制字符串前difficulty位必须是0，也就是必须小于某个数。
     * 矿工必须不断随机出nonce值来参与hash运算，直到满足要求，这样的block才会被别人承认。
     * 为何要设置挖矿难度？有了难度之后，篡改的成本变的很高。由于block包含了前一个block的hash值，修改一个block的内容就
     * 必须把后面的block都重新计算一遍。假设没有任何难度，一个人想把某个时间点之后block数据全改掉是比较容易的，
     * 他自己可以疯狂的生成block然后接到某个合法block后面，长度比合法的链还要长，那么他就成了主链，别人就要接受他的链。
     * 有了挖矿难度，想要上面这样操作会慢很多，非法的链长度很难超过合法的链。这样就保证了区块链的信息一经记录就几乎无法修改。
     *
     * 在完善的区块链系统中，难度是会动态调整的：挖矿的人多，难度就提高。挖矿的人少，难度就降低。确保block生产的速度比较稳定。
     *
     *  */
    public final static long difficulty = 2;


    /**
     * 矿工是否在工作
     */
    public static boolean isActive = true;


    @Override
    public void run() {
        while (true){
            if(isActive){
                // 获取当前的最新区块

                // 收集未确认交易

                // 加入给自己的激励

                // 循环随机数，构建block对象，计算hash
                // 每次都要检查有没有接收到新的区块，如果有说明被别人先挖出来了，放弃这次挖矿


            }else{
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
