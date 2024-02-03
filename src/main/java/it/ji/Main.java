package it.ji;

import it.ji.manager.GameManager;
import it.ji.manager.RedisManager;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to multiplayer");
        System.out.println("1) New Server");
        System.out.println("2) Join Server");
        System.out.println("3) Exit");
        System.out.print("Choose an option: ");
        int option = scanner.nextInt();
        switch (option) {
            case 1:
                GameManager.getInstance().startServer();
                System.out.println("++++++++++++++++++++++++++++++");
                System.out.println("Creating a new Server with ID: " + GameManager.getInstance().getServerId());
                System.out.print("> Insert your player name: ");
                String playerName = scanner.next();
                GameManager.getInstance().setPlayer1(new Player(playerName));
                System.out.println("Player " + playerName + " is ready!");
                GameManager.getInstance().waitingPlayers();
                break;
            case 2:
                System.out.println("not ready yet!");
                break;
            case 3:
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option");
                break;
        }
        RedisManager.getInstance().put("test", "timestamp: " + System.currentTimeMillis());
        System.out.println("end of the program!");
    }
}