package it.ji.game.client;

import it.ji.game.client.exceptions.ServerNotFoundException;
import it.ji.game.client.manager.ClientGameManager;
import it.ji.game.utils.settings.Settings;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Starting client...");
        System.out.println("insert server id: ");
        String serverId = scanner.nextLine();
        System.out.println("insert username: ");
        String username = scanner.nextLine();
        try {
            ClientGameManager.getInstance().startClient(serverId, username);
        } catch (ServerNotFoundException e) {
            e.printStackTrace();
        }
    }

}
