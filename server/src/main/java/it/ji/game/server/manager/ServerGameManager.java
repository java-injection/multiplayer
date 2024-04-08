package it.ji.game.server.manager;

import it.ji.game.server.Utils;
import it.ji.game.utils.logging.Logger;
import it.ji.game.utils.logic.Coordinates;
import it.ji.game.utils.logic.Player;
import it.ji.game.utils.logic.PlayerType;
import it.ji.game.utils.redis.RedisManager;
import it.ji.game.utils.redis.RedisMessage;
import it.ji.game.utils.redis.RedisMessageListener;
import it.ji.game.utils.settings.Settings;
import it.ji.game.utils.settings.Status;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerGameManager implements RedisMessageListener {
    public final static int TURRET_PLAYER_1 = 3;
    public final static int TURRET_PLAYER_2 = 4;
    private static ServerGameManager instance = null;
    public static final String GAME_NAME = "MATRICE";
    private String serverId;
    private Integer[][] localBoard;
    private Player player1;
    private Player player2;
    private Coordinates player1Coordinates;
    private Coordinates player2Coordinates;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();


    private ServerGameManager() {
        //todo implementare bene la local board

        System.out.println("[DEBUG] Height: " + Settings.getInstance().getHeight()+ " Width: " + Settings.getInstance().getWitdh() );
        localBoard = new Integer[Settings.getInstance().getHeight()][Settings.getInstance().getWitdh()];
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
            /*RedisManager.getInstance().subscribe(
                    message -> {
                        System.out.println("<<<<<<<<[ERROR] Received message: " + message.message() + " from channel: " + message.channel());
                    },"login"
            );*/

            //create a waiting thread that listen for players to login through the redis publish/subscribe system
            RedisManager.getInstance().subscribe(this,"login","game.move.server","game.turret.server");
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
                if (elapsed > 2000) {
                    System.out.println("Timeout");
                    shutDownServer();
                    break;
                }
            }
            System.out.println(" ************************ Server started ************************ ");
            startGame();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setInitialPositions() {
        Coordinates p1RandomInitCoords = getRandomCoordinates();
        Coordinates p2RandomInitCoords = getRandomCoordinates();

        while (p1RandomInitCoords.equals(p2RandomInitCoords)){
            p2RandomInitCoords = getRandomCoordinates();
        }
        player1Coordinates = p1RandomInitCoords;
        player2Coordinates = p2RandomInitCoords;
        localBoard[p1RandomInitCoords.x()][p1RandomInitCoords.y()] = 1;
        localBoard[p2RandomInitCoords.x()][p2RandomInitCoords.y()] = 2;

        RedisManager.getInstance().publish("game.init", serverId+":"+player1.username()+":"+p1RandomInitCoords.x()+","+p1RandomInitCoords.y());
        RedisManager.getInstance().publish("game.init", serverId+":"+player2.username()+":"+p2RandomInitCoords.x()+","+p2RandomInitCoords.y());
    }
    public void setPlayerMovement(Player player, Coordinates coordinates){
        if (player.equals(player1)) {
            localBoard[coordinates.x()][coordinates.y()] = 1;
        }
        if (player.equals(player2)) {
            localBoard[coordinates.x()][coordinates.y()] = 2;
        }
    }

    //todo spostare in utils
    public static Coordinates getRandomCoordinates(){
        return new Coordinates((int) (Math.random()*Settings.getInstance().getHeight()),(int) (Math.random()*Settings.getInstance().getWitdh()));
    }
    public void shutDownServer(){
        RedisManager.getInstance().hdelete(GAME_NAME, serverId);
        RedisManager.getInstance().shutdown();
    }


    public void startGame(){
        initBoard();
        System.out.println("Game started");
        RedisManager.getInstance().publish("game.start", serverId+":"+player1.username()+":"+player2.username());
        setInitialPositions();
        printBoard();
    }

    @Override
    public void onMessage(RedisMessage message) {

        if (message.channel().equals("login")){
            if (player1 == null){
                player1 = new Player(message.message().split(":")[1].trim(), PlayerType.SERVER);
                System.out.println("Player 1 logged in: "+player1.username());
                RedisManager.getInstance().publish("login.status.accepted", serverId+":"+player1.username());
            }else if (player2 == null){
                player2 = new Player(message.message().split(":")[1].trim(), PlayerType.SERVER);
                System.out.println("Player 2 logged in: "+player2.username());
                RedisManager.getInstance().publish("login.status.accepted", serverId+":"+player2.username());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }else{
                System.out.println("Server full");
                RedisManager.getInstance().publish("login.status.rejected", serverId+":"+message.message());
            }
        }
        if (message.channel().equals("game.move.server")){
            System.out.println("Received move: "+message.message());
            String[] split = message.message().split(":");
            String messageServerId = split[0];
            String messageusername = split[1];
            String[] messageCoords = split[2].split(",");
            int x = Integer.parseInt(messageCoords[0]);
            int y = Integer.parseInt(messageCoords[1]);
            if (x < 0 || x >= Settings.getInstance().getHeight() || y < 0 || y >= Settings.getInstance().getWitdh()){
                System.out.println("Invalid coordinates");
                RedisManager.getInstance().publish("game.move.client.refused", serverId+":"+messageusername+":"+x+","+y);
                return;
            }
            if (messageServerId.matches(serverId)){
                if (localBoard[x][y] != 0){
                    System.out.println("Cell already occupied");
                    RedisManager.getInstance().publish("game.move.client.refused", serverId+":"+messageusername+":"+x+","+y);
                    return;
                }
                if (messageusername.matches(player1.username())){
                    localBoard[player1Coordinates.x()][player1Coordinates.y()] = 0;
                    localBoard[x][y] = 1;
                    player1Coordinates = new Coordinates(x,y);
                }else if (messageusername.matches(player2.username())){
                    localBoard[player2Coordinates.x()][player2Coordinates.y()] = 0;
                    localBoard[x][y] = 2;
                    player2Coordinates = new Coordinates(x,y);
                }
                System.out.println("Player "+messageusername+" moved to "+x+","+y);
                RedisManager.getInstance().publish("game.move.client.accepted", serverId+":"+messageusername+":"+x+","+y);
            }
            printBoard();
        }
        if (message.channel().equals("game.turret.server")){
            System.out.println("Received turret: "+message.message());
            String[] split = message.message().split(":");
            String messageServerId = split[0];
            String messageusername = split[1];
            String[] messageCoords = split[2].split(",");
            int deltaX = Integer.parseInt(messageCoords[0]);
            int deltaY = Integer.parseInt(messageCoords[1]);
            if (messageServerId.matches(serverId)){
                Coordinates deltaPlusCurrentCoordinates = new Coordinates(0, 0);
                if (localBoard[deltaPlusCurrentCoordinates.x()][deltaPlusCurrentCoordinates.y()] != 0){
                    System.out.println("Cell already occupied");
                    RedisManager.getInstance().publish("game.turret.client.refused", serverId+":"+messageusername+":"+deltaX+","+deltaY);
                    return;
                }
                if (messageusername.matches(player1.username())){
                    deltaPlusCurrentCoordinates = new Coordinates(player1Coordinates.x()+deltaX, player1Coordinates.y()+deltaY);
                    if(isOutOfBounds(deltaPlusCurrentCoordinates)){
                        System.out.println("Invalid coordinates");
                        RedisManager.getInstance().publish("game.turret.client.refused", serverId+":"+messageusername+":"+deltaX+","+deltaY);
                        return;
                    }
                    if (isCellOccupied(deltaPlusCurrentCoordinates)){
                        System.out.println("Cell already occupied");
                        RedisManager.getInstance().publish("game.turret.client.refused", serverId+":"+messageusername+":"+deltaX+","+deltaY);
                        return;
                    }
                    localBoard[deltaPlusCurrentCoordinates.x()][deltaPlusCurrentCoordinates.y()] = TURRET_PLAYER_1;
                }else if (messageusername.matches(player2.username())){
                    deltaPlusCurrentCoordinates = new Coordinates(player2Coordinates.x()+deltaX, player2Coordinates.y()+deltaY);
                    if(isOutOfBounds(deltaPlusCurrentCoordinates)){
                        System.out.println("Invalid coordinates");
                        RedisManager.getInstance().publish("game.turret.client.refused", serverId+":"+messageusername+":"+deltaX+","+deltaY);
                        return;
                    }
                    if (isCellOccupied(deltaPlusCurrentCoordinates)){
                        System.out.println("Cell already occupied");
                        RedisManager.getInstance().publish("game.turret.client.refused", serverId+":"+messageusername+":"+deltaX+","+deltaY);
                        return;
                    }
                    localBoard[deltaPlusCurrentCoordinates.x()][deltaPlusCurrentCoordinates.y()] = TURRET_PLAYER_2;
                }
                System.out.println("Player "+messageusername+" placed turret at "+deltaX+","+deltaY);
                RedisManager.getInstance().publish("game.turret.client.accepted", serverId+":"+messageusername+":"+deltaPlusCurrentCoordinates.x()+","+deltaPlusCurrentCoordinates.y());
            }
            printBoard();
        }
    }

    public boolean isOutOfBounds(Coordinates coordinates){
        return coordinates.x() < 0 || coordinates.x() >= Settings.getInstance().getHeight() || coordinates.y() < 0 || coordinates.y() >= Settings.getInstance().getWitdh();
    }
    public boolean isCellOccupied(Coordinates coordinates){
        return localBoard[coordinates.x()][coordinates.y()] != 0;
    }
    private void initBoard(){
        localBoard = new Integer[Settings.getInstance().getHeight()][Settings.getInstance().getWitdh()];
        for (Integer[] integers : localBoard) {
            for (int j = 0; j < localBoard[0].length; j++) {
                integers[j] = 0;
            }
        }
    }
    public void printBoard(){
//todo: implement this method
        for (int i = 0; i < Settings.getInstance().getHeight(); i++){
            for (int j = 0; j < Settings.getInstance().getWitdh(); j++){
                System.out.print(localBoard[i][j] + " ");
            }
            System.out.println();
        }
    }
}
