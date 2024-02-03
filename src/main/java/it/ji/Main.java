package it.ji;

import it.ji.manager.RedisManager;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to multiplayer");
        RedisManager.getInstance().put("Mammt", "è A PECORAAAAAA");
    }
}