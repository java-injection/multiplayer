package it.ji.manager.events;

import it.ji.manager.RedisMessage;

@FunctionalInterface
public interface RedisMessageListener {
    void onMessage(RedisMessage message);
}
