package it.ji.manager;

import it.ji.Player;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GameManager {
    private ExecutorService executorService;
    private Player player1;
    private Player player2;
    private String serverId;
    private static GameManager instance = null;


    private GameManager() {
        executorService = Executors.newFixedThreadPool(2);
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    public boolean canStart(){
        return player1 != null && player2 != null;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public void startServer(){
        serverId = Utils.generateServerId();
        System.out.println("Server Id: "+serverId);
        RedisManager.getInstance().put(serverId, "waiting");
    }

    public String getServerId() {
        return serverId;
    }

    public Future<Void> waitingPlayers(){
        return executorService.submit(()->{
            int elapsed = 0;
            while (true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                System.out.print("waiting for players " + (elapsed++) + " seconds\r");
                if (canStart()){
                    System.out.println("both players ready");
                    break;
                }
            }
            return null;
        });
    }

    public void joinServer(String serverId, Player player2) {
        if(RedisManager.getInstance().get(serverId) == null){
            System.out.println("Server not found");
            return;
        }
        if(RedisManager.getInstance().get(serverId).equals("waiting")){
            RedisManager.getInstance().put(serverId, "playing");
            this.player2 = player2;
            System.out.println("Player " + player2.name() + " joined the server");
        }else{
            System.out.println("Server is already playing");
        }
    }
}
