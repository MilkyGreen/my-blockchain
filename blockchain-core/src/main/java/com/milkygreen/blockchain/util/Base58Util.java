package com.milkygreen.blockchain.util;

import org.bitcoinj.core.Base58;

/**
 * Base58工具类
 *
 */
public class Base58Util {

    /**
     * Base58编码
     */
    public static String encode(byte[] input) {
        return Base58.encode(input);
    }

    /**
     * Base58解码
     */
    public static byte[] decode(String input) {
        return Base58.decode(input);
    }
}