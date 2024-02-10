package it.ji.game.events;

import it.ji.game.redis.RedisMessage;

@FunctionalInterface
public interface RedisMessageListener {
    void onMessage(RedisMessage message);
}
