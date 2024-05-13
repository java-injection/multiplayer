package it.ji.game.utils.logging;

@FunctionalInterface
public interface LogMethod {
    void log(String message, Level level);
}
