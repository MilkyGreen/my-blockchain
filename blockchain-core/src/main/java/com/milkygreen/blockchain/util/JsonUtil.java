package com.milkygreen.blockchain.util;

import com.google.gson.Gson;

/**
 */
public class JsonUtil {

    public static Gson gson = new Gson();

    public static String toJson(Object object){
        return gson.toJson(object);
    }

    public static <T> T parseJson(String json,Class<T> classTo){
        return gson.fromJson(json,classTo);
    }

}
