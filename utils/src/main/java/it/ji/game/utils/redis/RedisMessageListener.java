package it.ji.game.utils.redis;


@FunctionalInterface
public interface RedisMessageListener {
    void onMessage(RedisMessage message);
}
