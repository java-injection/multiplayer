package it.ji.game.server.manager;

import it.ji.game.server.Utils;
import it.ji.game.utils.logic.Coordinates;
import it.ji.game.utils.logic.Player;
import it.ji.game.utils.redis.RedisManager;
import it.ji.game.utils.redis.RedisMessage;
import it.ji.game.utils.redis.RedisMessageListener;
import it.ji.game.utils.settings.Settings;
import it.ji.game.utils.settings.Status;

import java.util.Map;

public class ServerGameManager implements RedisMessageListener {
    private static ServerGameManager instance = null;
    public static final String GAME_NAME = "MATRICE";
    private String serverId;
    private Integer[][] localBoard;
    private Player player1;
    private Player player2;
    private Map<String, Coordinates> playersToCoordinates;

    private ServerGameManager() {
    }

    public static ServerGameManager getInstance() {
        if (instance == null) {
            instance = new ServerGameManager();
        }
        return instance;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerId() {
        return serverId;
    }

    public boolean canStart(){
        return player1 != null && player2 != null;
    }

    public void startServer(){
        try {
            System.out.println("[GameServer] Starting server...");
            serverId = Utils.generateServerId();
            System.out.println("[GameServer] Server Id: " + serverId);
            RedisManager.getInstance().hset(GAME_NAME,serverId, String.valueOf(Status.WAITING));
            System.out.println("[DEBUG] Subscribing...");
            RedisManager.getInstance().subscribe("login",
                    message -> {
                        System.out.println("Received message: " + message.message() + " from channel: " + message.channel());
                    }
            );

            //create a waiting thread that listen for players to login through the redis publish/subscribe system
            RedisManager.getInstance().subscribe("login", this);
            //create a thread that wait for the game to start and write elapsed time each second until the game starts

            int elapsed = 0;
            System.out.println("waiting for players");
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.print("waiting for players " + (elapsed++) + " seconds\r");
                if (canStart()  ) {
                    System.out.println("both players ready");
                    break;
                }
                if (elapsed > 600) {
                    System.out.println("Timeout");
                    shutDownServer();
                    break;
                }
            }
            System.out.println(" ************************ Server started ************************ ");
            RedisManager.getInstance().kill();

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void shutDownServer(){
        RedisManager.getInstance().hdelete(GAME_NAME, serverId);
        RedisManager.getInstance().shutdown();
    }



    @Override
    public void onMessage(RedisMessage message) {

        if (message.channel().equals("login")){
            if (player1 == null){
                player1 = new Player(message.message());
                System.out.println("Player 1 logged in: "+player1.username());
                RedisManager.getInstance().publish("login/status/accepted", serverId+" : "+player1.username());
            }else if (player2 == null){
                player2 = new Player(message.message());
                System.out.println("Player 2 logged in: "+player2.username());
                RedisManager.getInstance().publish("login/status/accepted", serverId+" : "+player1.username());
            }else{
                System.out.println("Server full");
                RedisManager.getInstance().publish("login/status/rejected", serverId+" : "+message.message());
            }
        }
    }
    private void initBoard(){
        localBoard = new Integer[Settings.getInstance().getHeight()][Settings.getInstance().getWitdh()];

        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                localBoard[i][j] = 0;
            }
        }
    }
    public void printBoard(){

        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                System.out.print(localBoard[i][j] + " ");
            }
            System.out.println();
        }
    }
}
