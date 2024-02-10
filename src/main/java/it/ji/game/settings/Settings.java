package it.ji.game.settings;

public class Settings {
    private static Settings instance = null;

    private Settings() {
    }

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }
    public int getWitdh(){
        return 10;
    }
    public int getHeight(){
        return 10;
    }
}
