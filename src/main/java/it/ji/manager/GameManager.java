package it.ji.manager;

import it.ji.Player;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GameManager {
    private ExecutorService executorService;
    private Player player1;
    private Player player2;
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

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public Future<Void> waitingPlayers(){
        return executorService.submit(()->{

            while (true){
                System.out.println("waiting players... ");
                if (canStart()){
                    System.out.println("both players ready");
                    break;
                }
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                }
            }
            return null;
        });
    }
}
