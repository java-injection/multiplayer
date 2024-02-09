package it.ji.manager;

import it.ji.manager.events.RedisMessageListener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisManager {
    private static RedisManager instance = null;
    private Jedis jedisBroker = null;
    private Jedis jedisRW = null;

    private List<RedisMessageListener> listeners = new ArrayList<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private RedisManager() {
        jedisBroker = new Jedis("http://217.160.155.226:19003");
        jedisRW = new Jedis("http://217.160.155.226:19003");
    }

    public static RedisManager getInstance() {
        if (instance == null) {
            instance = new RedisManager();
        }
        return instance;
    }
    public void put(String key, String value){
        jedisRW.set(key, value);
    }

    public String get(String key){
        return jedisRW.get(key);
    }

    public void hset(String key, String field, String value){
        jedisRW.hset(key, field, value);
    }

    public Optional<String> hget(String key, String field){
        return Optional.ofNullable(jedisRW.hget(key, field));
    }

    public void publish(String channel, String message){
        jedisBroker.publish(channel, message);
    }

    public void subscribe(String channel, RedisMessageListener listener){
        listeners.add(listener);
        executorService.submit(() -> {
            try {
                jedisBroker.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {

                        listeners.forEach(listener -> listener.onMessage(new RedisMessage(channel, message)));
                    }
                }, channel);
                System.out.println("Subscribed to channel: " + channel);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //expose a method to kill the executor service
    public void kill(){
        System.out.println("Killing the executor service");
        executorService.shutdown();
        System.out.println("Executor service killed");
    }
    public void hdelete(String key, String field){
        System.out.println("Deleting field: " + field + " from key: " + key);
        jedisRW.hdel(key, field);
    }

    public void shutdown() {
        jedisBroker.shutdown();
        jedisRW.shutdown();
        jedisBroker.close();
        jedisRW.close();
    }
}
