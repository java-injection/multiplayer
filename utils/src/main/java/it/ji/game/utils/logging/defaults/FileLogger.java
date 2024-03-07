package it.ji.game.utils.logging.defaults;


import it.ji.game.utils.logging.Level;
import it.ji.game.utils.logging.LogMethod;

public class FileLogger implements LogMethod {

    @Override
    public void log(String message, Level level) {
        System.out.println("FileLogger: " + message + " " + level);
    }
}
