package it.ji.manager;

import redis.clients.jedis.Jedis;

import java.awt.geom.Rectangle2D;

public class RedisManager {
    private static RedisManager instance = null;
    private Jedis jedis = null;
    private RedisManager() {
        jedis = new Jedis("http://grandeminchia.org:200");
    }

    public static RedisManager getInstance() {
        if (instance == null) {
            instance = new RedisManager();
        }
        return instance;
    }
    public void put(String key, String value){
        jedis.set(key, value);
    }


}
