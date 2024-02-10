package it.ji.game;

import it.ji.game.communications.ClientGameManager;
import it.ji.game.communications.ServerGameManager;
import it.ji.game.exceptions.ServerNotFoundException;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        //switch between player and server mode using the first argument that can be --server or --player
        if (args.length == 0) {
            System.out.println("Usage: java -jar <jarfile> --server or java -jar <jarfile> --player");
            return;
        }
        if (args[0].equals("--server")) {
            System.out.println("WARNING: this is a server, it will start listening ..");
            ServerGameManager.getInstance().startServer();

        } else if (args[0].equals("--player")) {
            while (true) {
                System.out.println("Enter the server id");
                Scanner scanner = new Scanner(System.in);
                String serverId = scanner.nextLine();
                System.out.println("inserisci il tuo username");
                String username = scanner.nextLine();
                try {
                    ClientGameManager.getInstance().startClient(serverId, username);
                } catch (IllegalArgumentException | ServerNotFoundException e) {
                    System.out.println(e.getMessage());
                    continue;
                }
                System.out.println("Waiting for the server to start the game ..");
                break;
            }
        }
        if(args.length > 1) {
            System.out.println("Second argument: " + args[1]);
        }
    }
}