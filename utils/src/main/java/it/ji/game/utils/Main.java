package it.ji.game.utils;

import it.ji.game.utils.settings.Settings;

public class Main {

    public static void main(String[] args) {
        System.out.println("[CLIENT] Loading settings...");
        Settings.getInstance();
        System.out.println("[CLIENT] Settings loaded");
    }
}
