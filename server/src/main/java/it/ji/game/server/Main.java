package it.ji.game.server;

import it.ji.game.server.manager.ServerGameManager;

public class Main {

    public static void main(String[] args) {
        System.out.println("[JI Game Server] Starting...");
        ServerGameManager.getInstance().startServer();
        System.out.println("\r[JI Game Server] Started ... OK");
    }
}
