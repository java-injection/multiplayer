package it.ji.game.logging.defaults;

import it.ji.game.logging.Level;
import it.ji.game.logging.LogMethod;

public class SystemLogger implements LogMethod {
    @Override
    public void log(String message, Level level) {
        System.out.println("SystemLogger: " + message + " " + level);
    }
}
