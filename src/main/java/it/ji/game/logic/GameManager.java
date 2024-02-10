package it.ji.game.logic;

import it.ji.game.communications.Player;

public class GameManager {
    private Player player1;
    private Player player2;
    private static GameManager instance = null;
    private int[][] board = null;



    private GameManager() {
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
    public Player getPlayer1() {
        return player1;
    }
    public Player getPlayer2() {
        return player2;
    }
}
