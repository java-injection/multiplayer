package it.ji.game.logging;

import it.ji.game.logging.defaults.DatabaseLogger;
import it.ji.game.logging.defaults.FileLogger;
import it.ji.game.logging.defaults.SystemLogger;

public class Logger {

    private static LogMethod systemLogMethod;
    private static LogMethod databaseLogMethod;
    private static LogMethod fileLogMethod;
    private static Logger _instance = null;
    private static Level _level;
    private static String _message;
    private static void init(){
        if(_instance == null){
            _instance = new Logger();
        }
    }

    private Logger(){
        systemLogMethod = new SystemLogger();
        databaseLogMethod = new DatabaseLogger();
        fileLogMethod = new FileLogger();
    }

    public static Logger info(String message){
        init();
        _message = message;
        _level = Level.INFO;
        return _instance;
    }

    public static Logger warning(String message){
        init();
        _message = message;
        _level = Level.WARNING;
        return _instance;
    }

    public static Logger debug(String message){
        init();
        _message = message;
        _level = Level.DEBUG;
        return _instance;
    }

    public static Logger error(String message){
        init();
        _message = message;
        _level = Level.ERROR;
        return _instance;
    }

    public static Logger fatal(String message){
        init();
        _message = message;
        _level = Level.FATAL;
        return _instance;
    }


    public static Logger system(){
        systemLogMethod.log(_message, _level);
        return _instance;
    }

    public static Logger db(){
        databaseLogMethod.log(_message, _level);
        return _instance;
    }

    public static Logger file(){
        fileLogMethod.log(_message, _level);
        return _instance;
    }

    //Logger.info("cia").system().file().db();

}
