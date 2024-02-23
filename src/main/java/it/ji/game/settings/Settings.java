package it.ji.game.settings;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

public class Settings {
    private static Settings instance = null;
    private EnumMap<SettingsKey, String> settings = new EnumMap<>(SettingsKey.class);

    private String path = "settings.properties";
    private Settings() {
        loadSettingsFromFile();
    }

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    private void loadSettingsFromFile(){
        Properties properties = new Properties();
        try(InputStream is = new FileInputStream(path)){
            properties.load(is);
            for (String key : properties.stringPropertyNames()) {
                settings.put(SettingsKey.fromString(key), properties.getProperty(key));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void printSetting(){
        for (SettingsKey key : settings.keySet()) {
            System.out.println(key.getKey() + " : " + settings.get(key));
        }
    }
    public int getWitdh(){
        return 10;
    }
    public int getHeight(){
        return 10;
    }
}
