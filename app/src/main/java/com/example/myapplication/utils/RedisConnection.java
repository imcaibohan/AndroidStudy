package com.example.myapplication.utils;

import redis.clients.jedis.Jedis;

public class RedisConnection {
    private static final String REDIS_HOST = "r-bp1lcd21nkr3zeqt8y"; // 你的Redis服务器地址
    private static final int REDIS_PORT = 6379; // 你的Redis服务器端口

    public static Jedis getInstance() {
        return new Jedis(REDIS_HOST, REDIS_PORT);
    }
}
