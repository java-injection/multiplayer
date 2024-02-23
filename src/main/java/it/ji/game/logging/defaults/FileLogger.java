package it.ji.game.logging.defaults;

import it.ji.game.logging.Level;
import it.ji.game.logging.LogMethod;

public class FileLogger implements LogMethod {

    @Override
    public void log(String message, Level level) {
        System.out.println("FileLogger: " + message + " " + level);
    }
}
