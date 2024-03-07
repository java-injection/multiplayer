package it.ji.game.utils.logging.defaults;

import it.ji.game.utils.logging.Level;
import it.ji.game.utils.logging.LogMethod;

public class SystemLogger implements LogMethod {
    @Override
    public void log(String message, Level level) {
        System.out.println("SystemLogger: " + message + " " + level);
    }
}
