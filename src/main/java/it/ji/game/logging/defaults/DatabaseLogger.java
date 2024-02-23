package it.ji.game.logging.defaults;

import it.ji.game.logging.Level;
import it.ji.game.logging.LogMethod;

public class DatabaseLogger implements LogMethod {
    private String connectionString;
    private String username;
    private String password;
    @Override
    public void log(String message, Level level) {
        System.out.println("DatabaseLogger: " + message + " " + level);
    }
}
