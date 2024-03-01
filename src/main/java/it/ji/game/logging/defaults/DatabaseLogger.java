package it.ji.game.logging.defaults;

import it.ji.game.logging.Level;
import it.ji.game.logging.LogMethod;
import it.ji.game.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseLogger implements LogMethod {
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://152.228.218.211:230/logger?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = System.getenv("DB_PASSWORD");


    // Oggetto Connection per la connessione al database
    private Connection conn;

    // Metodo per la connessione al database
    private void connect() {
        try {
            // Caricamento del driver JDBC
            Class.forName(JDBC_DRIVER);

            // Connessione al database
            System.out.println("Connessione al database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connessione riuscita.");
        } catch (ClassNotFoundException e) {
            System.err.println("Errore nel caricamento del driver JDBC: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Errore durante la connessione al database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Metodo per la chiusura della connessione al database
    private void close() {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Connessione al database chiusa.");
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la chiusura della connessione al database: " + e.getMessage());
        }
    }
    //show tables
    private void showTables() {
        connect();
        try {
            java.sql.Statement stmt = conn.createStatement();
            String sql = "SHOW TABLES";
            stmt.executeQuery(sql);
            //print the result
            System.out.println("Tables in the database:");
            while (stmt.getResultSet().next()) {
                System.out.println(stmt.getResultSet().getString(1));
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la visualizzazione delle tabelle: " + e.getMessage());
        }
        close();
    }

    // Metodo per ottenere l'oggetto Connection
    private Connection getConnection() {
        return conn;
    }
    // insert method
    private void insert(String message, Level level) {
        // Connessione al database
        connect();

        // Inserimento del messaggio nel database
        try {
            // Creazione dello statement
            java.sql.Statement stmt = conn.createStatement();

            // Query per l'inserimento del messaggio nel database
            String sql = "INSERT INTO logger (message, livello, user_, timestamp_) VALUES ('" + message + "', '" + level + "', '" + Logger.getUser() + "', NOW())";
            stmt.executeUpdate(sql);
            System.out.println("Messaggio inserito nel database.");
        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento del messaggio nel database: " + e.getMessage());
        }

        // Chiusura della connessione al database
        close();
    }
    @Override
    public void log(String message, Level level) {
        insert(message, level);
    }

}
