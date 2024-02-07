package it.ji.manager;

import it.ji.Player;
import it.ji.manager.events.RedisMessageListener;
import it.ji.manager.logic.Status;

public class ServerGameManager implements RedisMessageListener {
    private static ServerGameManager instance = null;
    public static final String GAME_NAME = "MATRICE";
    private String serverId;
    private Player player1;
    private Player player2;
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
                if (canStart() || elapsed > 60) {
                    System.out.println("both players ready");
                    break;
                }
            }
            System.out.println(" ************************ Server started ************************ ");
            RedisManager.getInstance().kill();

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @Override
    public void onMessage(RedisMessage message) {
        if (message.channel().equals("login")){
            if (player1 == null){
                player1 = new Player(message.message());
                System.out.println("Player 1 logged in: "+player1.name());
            }else if (player2 == null){
                player2 = new Player(message.message());
                System.out.println("Player 2 logged in: "+player2.name());
            }else{
                System.out.println("Server full");
            }
        }
    }
}
