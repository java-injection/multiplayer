package it.ji;

import it.ji.manager.GameManager;
import it.ji.manager.RedisManager;

public class Main {

    public static void main(String[] args) {
        System.out.println("Welcome to multiplayer");
        RedisManager.getInstance().put("Mammt", "è A PECORAAAAAA");
        GameManager.getInstance().setPlayer1(new Player("Felipe"));
        GameManager.getInstance().waitingPlayers();
        System.out.println("forsa");
    }
}