package it.ji;

import it.ji.manager.RedisManager;
import it.ji.manager.ServerGameManager;

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
            System.out.println("Enter the server id");
            Scanner scanner = new Scanner(System.in);
            String serverId = scanner.nextLine();
            RedisManager.getInstance().publish("hello", "I'm a player");
            System.out.println("Waiting for the server to start the game ..");

        }
        if(args.length > 1) {
            System.out.println("Second argument: " + args[1]);
        }
    }
}