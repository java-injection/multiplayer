package it.ji.manager;

@FunctionalInterface
public interface RedisMessageListener {
    void onMessage(RedisMessage message);
}
