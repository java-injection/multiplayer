package it.ji.game.logging;

@FunctionalInterface
public interface LogMethod {
    void log(String message, Level level);
}
