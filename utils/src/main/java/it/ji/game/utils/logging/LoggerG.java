package it.ji.game.utils.logging;

public class LoggerG {
    private static LoggerG instance = null;
    private String _message;

    private LoggerG() {
    }
    public  static  LoggerG setMessage(String message){
        if (instance == null) {
            instance = new LoggerG();
        }
        instance._message = message;
        return instance;
    }
    public LoggerG system(){
        if (_message == null) {
            System.out.println();
            return this;
        }
        System.out.println("[LOGGING WITH LOGGER]"+_message);
        return this;
    }

    public LoggerG db(){
        throw new UnsupportedOperationException("Questo metodo non è ancora implementato.");
    }
}
