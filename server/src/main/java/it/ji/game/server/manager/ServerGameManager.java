package it.ji.game.server.manager;

import it.ji.game.server.Utils;
import it.ji.game.utils.logic.Coordinates;
import it.ji.game.utils.logic.Player;
import it.ji.game.utils.logic.PlayerType;
import it.ji.game.utils.logic.objects.Turret;
import it.ji.game.utils.logic.objects.TurretListener;
import it.ji.game.utils.logic.objects.TurretManager;
import it.ji.game.utils.logic.objects.TurretTypes;
import it.ji.game.utils.redis.RedisManager;
import it.ji.game.utils.redis.RedisMessage;
import it.ji.game.utils.redis.RedisMessageListener;
import it.ji.game.utils.settings.Settings;
import it.ji.game.utils.settings.Status;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerGameManager implements RedisMessageListener, TurretListener {

    public final static int EMPTY = 0;
    public final static int PLAYER_1 = 1;
    public final static int PLAYER_2 = 2;
    public final static int TURRET_PLAYER_1 = 3;
    public final static int TURRET_PLAYER_2 = 4;
    private static final int PROJECTILE = 5;
    private static ServerGameManager instance = null;
    public static final String GAME_NAME = "MATRICE";
    public static final String SERVER_STATUS = "server-status";

    private Map<Long, Coordinates> bulletsId = new HashMap<>();
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
        TurretManager.getInstance().addTurretListener(this);
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

    public void startServer() {
        try {
            System.out.println("[GameServer] Starting server...");
            System.out.println("[GameServer] Cleaning up Redis instance ...");
            RedisManager.getInstance().delete(GAME_NAME);
            RedisManager.getInstance().delete(SERVER_STATUS);
            System.out.println("[GameServer] Redis instance cleaned up ... OK");
            serverId = Utils.generateServerId();
            System.out.println("[GameServer] Server Id: " + serverId);
            RedisManager.getInstance().hset(GAME_NAME, serverId, String.valueOf(Status.WAITING));
            System.out.println("[DEBUG] Subscribing...");
            /*RedisManager.getInstance().subscribe(
                    message -> {
                        System.out.println("<<<<<<<<[ERROR] Received message: " + message.message() + " from channel: " + message.channel());
                    },"login"
            );*/

            //create a waiting thread that listen for players to login through the redis publish/subscribe system
            RedisManager.getInstance().subscribe(this,
                    "login",
                    "game.move.server",
                    "game.turret.server",
                    "game.item",
                    "login.request.player"
            );
            //create a thread that wait for the game to start and write elapsed time each second until the game starts

            int elapsed = 0;
            imalive();
            System.out.println("waiting for players");
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.print("waiting for players " + (elapsed++) + " seconds\r");
                if (canStart()) {
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
            RedisManager.getInstance().hset(GAME_NAME, "GENERAL", "ALIVE");

        } catch (Exception e) {
            e.printStackTrace();
            imdead();
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
        localBoard[p1RandomInitCoords.x()][p1RandomInitCoords.y()] = PLAYER_1;
        localBoard[p2RandomInitCoords.x()][p2RandomInitCoords.y()] = PLAYER_2;

        RedisManager.getInstance().publish("game.init", serverId+":"+player1.getUsername()+":"+p1RandomInitCoords.x()+","+p1RandomInitCoords.y());
        RedisManager.getInstance().publish("game.init", serverId+":"+player2.getUsername()+":"+p2RandomInitCoords.x()+","+p2RandomInitCoords.y());
    }
    public void setPlayerMovement(Player player, Coordinates coordinates){
        if (player.equals(player1)) {
            localBoard[coordinates.x()][coordinates.y()] = PLAYER_1;
        }
        if (player.equals(player2)) {
            localBoard[coordinates.x()][coordinates.y()] = PLAYER_2;
        }
    }

    //public a true value on message on game.imalive channel
    public void imalive(){
        RedisManager.getInstance().publish("game.imalive", "true");

        //thread that refresh each 5 second the server status
        Thread t = new Thread(this::refreshStatus);
        t.start();
    }

    //refresh the server status
    public void refreshStatus(){
        while (true){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            RedisManager.getInstance().hset(SERVER_STATUS, "GENERAL", "ALIVE", 5);
        }
    }

    public void imdead(){
        RedisManager.getInstance().delete(SERVER_STATUS);
        RedisManager.getInstance().publish("game.imalive", "false");
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
        RedisManager.getInstance().publish("game.start", serverId+":"+player1.getUsername()+":"+player2.getUsername());
        setInitialPositions();
        printBoard();
    }

    @Override
    public void onMessage(RedisMessage message) {
        if (message.channel().equals("login.request.player")){
            if (player1 == null) {
                RedisManager.getInstance().publish("login.response.player", serverId);
            }else {
                RedisManager.getInstance().publish("login.response.player", serverId+":"+player1.getUsername());
            }
        }
        if (message.channel().equals("login")){
            if (player1 == null){
                player1 = new Player(message.message().split(":")[1].trim(), PlayerType.SERVER);
                System.out.println("Player 1 logged in: "+player1.getUsername());
                RedisManager.getInstance().publish("login.status.accepted", serverId+":"+player1.getUsername());
            }else if (player2 == null){
                player2 = new Player(message.message().split(":")[1].trim(), PlayerType.SERVER);
                System.out.println("Player 2 logged in: "+player2.getUsername());
                RedisManager.getInstance().publish("login.status.accepted", serverId+":"+player2.getUsername());
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
                if (messageusername.matches(player1.getUsername())){
                    localBoard[player1Coordinates.x()][player1Coordinates.y()] = 0;
                    localBoard[x][y] =PLAYER_1;
                    player1Coordinates = new Coordinates(x,y);
                }else if (messageusername.matches(player2.getUsername())){
                    localBoard[player2Coordinates.x()][player2Coordinates.y()] = 0;
                    localBoard[x][y] = PLAYER_2;
                    player2Coordinates = new Coordinates(x,y);
                }
                System.out.println("Player "+messageusername+" moved to "+x+","+y);
                RedisManager.getInstance().publish("game.move.client.accepted", serverId+":"+messageusername+":"+x+","+y);
                System.out.println(localBoard);
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
                Turret turret = null;
                if (messageusername.matches(player1.getUsername())){
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
                    turret = new Turret(player1, serverId, TurretTypes.BASIC_TURRET, "BASIC_TURRET", deltaPlusCurrentCoordinates, 1);
                    localBoard[deltaPlusCurrentCoordinates.x()][deltaPlusCurrentCoordinates.y()] = TURRET_PLAYER_1;
                }else if (messageusername.matches(player2.getUsername())){
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
                     turret = new Turret(player2, serverId, TurretTypes.BASIC_TURRET, "BASIC_TURRET", deltaPlusCurrentCoordinates, 1);
                    localBoard[deltaPlusCurrentCoordinates.x()][deltaPlusCurrentCoordinates.y()] = TURRET_PLAYER_2;
                }
                startTurretEvent(turret);
                System.out.println("Player "+messageusername+" placed turret at "+deltaX+","+deltaY);
                RedisManager.getInstance().publish("game.turret.client.accepted", serverId+":"+messageusername+":"+deltaPlusCurrentCoordinates.x()+","+deltaPlusCurrentCoordinates.y());
            }
            printBoard();
        }
    }

    private void startTurretEvent(Turret turret) {
        executorService.submit(() -> {
            try {
                turret.use();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
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
                System.out.print(localBoard[i][j] + "  ");
            }
            System.out.println();
        }
    }

    @Override
    public void onBulletMoved(long id, int x, int y, int damage) {
        System.out.println("[DEBUG] bullet: "+id+" is trying to move to: "+x+","+y);
        if (bulletsId.get(id) == null){
            bulletsId.put(id, new Coordinates(x,y));
        }
        Coordinates coordinates = bulletsId.get(id);
        System.out.println("[DEBUG] id and Position before update: " +id+" coords "+bulletsId.get(id).x()+","+bulletsId.get(id).y());
        localBoard[coordinates.x()][coordinates.y()] = 0;

        System.out.println("Bullet moved to: "+x+","+y);
        int cell = localBoard[x][y];
        if (cell == PLAYER_1 || cell == PLAYER_2){
            System.out.println("Player hit");
            if (cell == PLAYER_1){
                player1.hit(damage);
                System.out.println("Player 1 hit for: "+damage+" damage health: "+player1.getHP());
                RedisManager.getInstance().publish("game.hit", serverId+":"+damage+":"+player1.getUsername());


                if (player1.isDead()){
                    System.out.println("Player 1 dead");
                    RedisManager.getInstance().publish("game.end", serverId+":"+player2.getUsername());
                    shutDownServer();
                }
            }else if (cell == PLAYER_2){
                player2.hit(damage);
                System.out.println("Player 2 hit for: "+damage+" damage health: "+player2.getHP());
                RedisManager.getInstance().publish("game.hit", serverId+":"+damage+":"+player2.getUsername());

                if (player2.isDead()){
                    System.out.println("Player 2 dead");
                    RedisManager.getInstance().publish("game.end", serverId+":"+player1.getUsername());
                    shutDownServer();
                }
            }
            TurretManager.getInstance().notifyBulletDeleted(id);
        } else if (cell == PROJECTILE) {
            long bulletIDfromCoords1 = bulletsId.entrySet().stream().filter(entry -> entry.getValue().equals(new Coordinates(x, y))).findFirst().get().getKey();
            long bulletIDfromCoords = TurretManager.getInstance().getBulletIDfromCoords(x,y);
            System.out.println("[DIFFERENZA] "+bulletIDfromCoords+" "+bulletIDfromCoords1);
            System.out.println("[Projectile hit] bullet id: "+id+"collided with bullet id: "+bulletIDfromCoords1+" at: "+x+","+y);
            TurretManager.getInstance().notifyBulletDeleted(id);
            System.out.println("Bullet ID from id: "+bulletIDfromCoords1);
            TurretManager.getInstance().notifyBulletDeleted(bulletIDfromCoords1);
            localBoard[x][y] = 0;
            localBoard[coordinates.x()][coordinates.y()] = 0;
        } else if (cell != 0) {
            System.out.println(localBoard[x][y] + " hit");
            TurretManager.getInstance().removeBulletFromMap(id);
            bulletsId.remove(id);
        } else {
            localBoard[x][y] = PROJECTILE;
            bulletsId.put(id, new Coordinates(x,y));
            RedisManager.getInstance().publish("game.projectile", serverId + ":" + id + ":" + x + "," + y);
            TurretManager.getInstance().updateBulletCoordinates(id, new Coordinates(x, y));
        }

    }

    @Override
    public void onBulletRemoved(long id,int x, int y) {
        System.out.println("Bullet removed from: "+x+","+y);
        localBoard[x][y] = 0;
        RedisManager.getInstance().publish("game.bullet.remove", serverId+":"+id+":"+x+","+y);
    }

    @Override
    public void onBulletDeleted(long id) {
        System.out.println("[DEBUG] Bullet deleted: "+id);
        bulletsId.remove(id);
        RedisManager.getInstance().publish("game.bullet.remove", serverId+":"+id);
    }
}
