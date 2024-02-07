package it.ji.manager;

import it.ji.manager.events.RedisMessageListener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RedisManager {
    private static RedisManager instance = null;
    private Jedis jedis = null;
    private List<RedisMessageListener> listeners = new ArrayList<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private RedisManager() {
        jedis = new Jedis("http://217.160.155.226:19003");
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

    public String get(String key){
        return jedis.get(key);
    }

    public void hset(String key, String field, String value){
        jedis.hset(key, field, value);
    }

    public String hget(String key, String field){
        return jedis.hget(key, field);
    }

    public void publish(String channel, String message){
        jedis.publish(channel, message);
    }

    public void subscribe(String channel, RedisMessageListener listener){
        listeners.add(listener);
        executorService.submit(() -> {
            try {
                jedis.subscribe(new JedisPubSub() {
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

    public static void main(String[] args) {
        RedisManager.getInstance().put("PROVA-GET","acqua");
        //test hset
        RedisManager.getInstance().hset("prova-hset2", "temperature", "20");
        RedisManager.getInstance().hset("prova-hset2", "temperature", "30");
        RedisManager.getInstance().hset("prova-hset2", "time", "11:21");
        final String temperature = RedisManager.getInstance().hget("prova-hset2", "temperature");
        System.out.println("Temperature: " + temperature);
    }


}
