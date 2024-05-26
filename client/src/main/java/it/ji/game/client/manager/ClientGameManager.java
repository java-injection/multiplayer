package it.ji.game.client.manager;


import it.ji.game.client.exceptions.NameAlreadyInUse;
import it.ji.game.client.exceptions.ServerNotFoundException;
import it.ji.game.client.gui.ClientListener;
import it.ji.game.client.gui.Direction;
import it.ji.game.utils.logging.LoggerG;
import it.ji.game.utils.logic.PlayerType;
import it.ji.game.client.gui.SingleCellPanel;
import it.ji.game.utils.logic.Coordinates;
import it.ji.game.utils.logic.Player;
import it.ji.game.utils.redis.RedisManager;
import it.ji.game.utils.redis.RedisMessage;
import it.ji.game.utils.redis.RedisMessageListener;
import it.ji.game.utils.settings.Settings;
import it.ji.game.utils.settings.Status;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientGameManager implements RedisMessageListener {
    private static ClientGameManager instance = null;
    private String serverId;
    private Map<Long, Coordinates> bulletsId = new HashMap<>();
    private SingleCellPanel[][] localBoard = new SingleCellPanel[Settings.getInstance().getHeight()][Settings.getInstance().getWitdh()];
    private Map<Player, Coordinates> playerPositions = new HashMap<>();
    private List<ClientListener> clientListeners = new CopyOnWriteArrayList<>();
    private Player selfPlayer;
    private Coordinates lastCoordinates;

    private boolean serverAlive = false;

    public static final String SERVER_STATUS = "server-status";

    private Boolean clientAccpted = false;

    private ClientGameManager() {

        RedisManager.getInstance().subscribe(this,
                "login.response.player",
                "login.status.accepted",
                "game.start",
                "game.init",
                "game.move.client",
                "game.move.client.refused",
                "game.move.client.accepted",
                "game.turret.client",
                "game.turret.client.refused",
                "game.turret.client.accepted",
                "game.turret.accepted",
                "game.turret.declined",
                "game.hit",
                "game.projectile",
                "game.projectile.moved",
                "game.bullet.remove",
                "server.imalive",
                "game.end"
        );
    }
    public void addPlayer(Player selfPlayer) {
        playerPositions.put(selfPlayer, null);

    }

    public void addClientListener(ClientListener listener) {
        clientListeners.add(listener);
    }

    public Boolean isClientAccpted() {
        return clientAccpted;
    }

    public static ClientGameManager getInstance() {
        if (instance == null) {
            instance = new ClientGameManager();
        }
        return instance;
    }

    public void serverIsAlive(boolean alive, Optional<String> serverId){

        for (ClientListener clientListener : clientListeners) {
            clientListener.serverIsAlive(alive, serverId);
        }
    }

    public Map<Player, Coordinates> getPlayerPositions() {
        return playerPositions;
    }

    public Player getPlayerFromType(PlayerType type) {
        return playerPositions
                .keySet()
                .stream()
                .filter(player -> player.getType() == type)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Player not found"));
    }

    public void setPlayerPositions(Map<Player, Coordinates> playerPositions) {
        this.playerPositions = playerPositions;
    }

    public Coordinates getLastCoordinates() {
        return lastCoordinates;
    }

    public void setLocalBoard(SingleCellPanel[][] localBoard) {
        this.localBoard = localBoard;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerId() {
        return serverId;
    }

    private boolean canStart() {
        //print contents of the map and everything that could set can start to false
        LoggerG.setMessage("[DEBUG] playerPositions: " + playerPositions).system().system();
        LoggerG.setMessage("[DEBUG] serverId: " + serverId).system().system();
        LoggerG.setMessage("[DEBUG] playerPositions.size(): " + playerPositions.size()).system().system();
        return playerPositions.size() == 2 &&
                serverId != null &&
                !serverId.isBlank() &&
                !serverId.isEmpty() &&
                playerPositions.keySet().stream().allMatch(player -> player.getUsername() != null && !player.getUsername().isBlank() && !player.getUsername().isEmpty());
    }

    public void requestToStartClient() throws NameAlreadyInUse {
        try {
            if (serverId == null || serverId.isBlank() || serverId.isEmpty()) {
                return;
            }
            if (isServerWaiting(serverId)) {
                //trigger im alive
                isNameInUse();
            }
        } catch (NameAlreadyInUse e) {
            throw new NameAlreadyInUse(e.getMessage());
        } catch (ServerNotFoundException e) {
            serverIsAlive(false,null);
            throw new RuntimeException(e);
        }
    }

    public void startClient() throws ServerNotFoundException {
        playerPositions.keySet().stream().filter(player -> player.getType() == PlayerType.SELF).findFirst().ifPresentOrElse(player -> {
            RedisManager.getInstance().publish("login", serverId + ":" + player.getUsername());
        }, () -> {
            throw new IllegalArgumentException("Server is not waiting for players");
        });
        LoggerG.setMessage("Waiting for the server to start the game ..").system().system();
    }

    private void isNameInUse() throws NameAlreadyInUse {
        RedisManager.getInstance().publish("login.request.player", serverId);
    }


    public boolean isServerWaiting(String serverId) throws ServerNotFoundException {
        return RedisManager.getInstance().hget(Settings.getInstance().getGameName(), serverId)
                .map(status -> status.equals(String.valueOf(Status.WAITING)))
                .orElseThrow(() -> new ServerNotFoundException("Server not found"));
    }

    public Player getSelfPlayer() {
        return this.selfPlayer;
    }
    public Player getEnemyPlayer() {
        return playerPositions.keySet().stream()
                .filter(player -> player.getType() == PlayerType.ENEMY)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Enemy player not found"));
    }

    private void initPositions(RedisMessage message) {
        LoggerG.setMessage("[DEBUG] handling message in channel: <game.init>").system().system();
        String channelMessage = message.message();
        String[] split = channelMessage.split(":");
        String initMessageServerId = split[0];
        LoggerG.setMessage("[DEBUG] initMessageServerId: [" + initMessageServerId+"]").system().system();
        String initMessageUsername = split[1];
        LoggerG.setMessage("[DEBUG] initMessageUsername: [" + initMessageUsername+"]").system().system();
        String initMessagePosition = split[2];
        String[] splitCoordinates = initMessagePosition.split(",");
        LoggerG.setMessage("[DEBUG] splitCoordinates: [" + splitCoordinates[0] + "] and [" + splitCoordinates[1] + "]").system().system();
        Coordinates xy = new Coordinates(Integer.parseInt(splitCoordinates[0]), Integer.parseInt(splitCoordinates[1]));
        if (!initMessageServerId.equals(serverId)){
            //todo definire costante per ServerID does not match
            LoggerG.setMessage("[DEBUG] ServerId does not match").system().system();
            return;
        }
        Player playerFromType = getPlayerFromType(PlayerType.SELF);
        if (playerFromType.getUsername().equals(initMessageUsername)) {
            setLocalBoardCoordinates(xy, PlayerType.SELF);
        } else {
            setLocalBoardCoordinates(xy, PlayerType.ENEMY);
        }
        LoggerG.setMessage("[DEBUG] Server initialized game for serverId: " + serverId).system().system();

    }

    public Coordinates getCoordinatesFromPlayer(Player player) {
        return playerPositions.get(player);
    }
    @Override
    public void onMessage(RedisMessage message) {
        if (message == null || message.message() == null || message.channel() == null){
            LoggerG.setMessage("[DEBUG] message is null or message.message() is null or message.channel() is null").system().system();
            return;
        }
        if (selfPlayer == null || serverId == null) {
            LoggerG.setMessage("[DEBUG] selfPlayer is null").system().system();
            return;
        }
        LoggerG.setMessage("Received message: [" + message.message() + "] from channel: " + message.channel() + " serverId: " + serverId).system().system();
        if (message.channel().equals("login.response.player")) {
            String[] serverMessage = message.message().split(":");
            LoggerG.setMessage("[debug] "+message.message()).system().system();
            if (serverMessage[0].equals(serverId)) {
                try {
                    if (serverMessage.length <2 || !serverMessage[1].equals(getSelfPlayer().getUsername())){
                        ClientGameManager.getInstance().addPlayer(selfPlayer);
                        LoggerG.setMessage("[DEBUG] STO STARTANDO IL CLIENT").system().system();
                        startClient();
                    }else{
                        LoggerG.setMessage("name already in use").system().system();
                        for (ClientListener clientListener : clientListeners) {
                            clientListener.userRejected(serverId, getSelfPlayer().getUsername());
                        }
                    }
                } catch (ServerNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        if (message.channel().equals("login.status.accepted")) {
            this.clientAccpted = true;
            LoggerG.setMessage("[DEBUG] handling message in channel: <login.status.accepted>").system().system();
            String[] split = message.message().split(":");
            LoggerG.setMessage("[DEBUG] split: [" + split[0] + "] and [" + split[1] + "]").system().system();
            String messageServerId = split[0];
            String messageUsername = split[1];
            if (messageServerId.equals(serverId) && messageUsername.equals(getSelfPlayer().getUsername())) {
                LoggerG.setMessage("[DEBUG] Server accepted user: " + messageUsername + " serverId: " + messageServerId).system().system();
                clientListeners.forEach(listener -> listener.userAccepted(messageServerId, messageUsername));
            }
        }
        if (message.channel().equals("game.start")) {
            LoggerG.setMessage("[DEBUG] handling message in channel: <game.start>").system().system();
            LoggerG.setMessage("[DEBUG] message: " + message.message()).system().system();
            String[] split = message.message().split(":");
            String messageServerId = split[0];
            String messagePlayer1 = split[1];
            String messagePlayer2 = split[2];
            if (messageServerId.equals(serverId)) {
                LoggerG.setMessage("[DEBUG] Server started game for serverId: " + serverId).system().system();
                playerPositions.entrySet().stream().findFirst().ifPresent((entry) -> {
                    if (entry.getKey().getUsername().equals(messagePlayer1)) {
                        LoggerG.setMessage("[DEBUG] IL NEMICO è " + messagePlayer2).system().system();
                        playerPositions.put(new Player(messagePlayer2, PlayerType.ENEMY), null);
                    } else if (entry.getKey().getUsername().equals(messagePlayer2)) {
                        LoggerG.setMessage("[DEBUG] IL NEMICO è " + messagePlayer1).system().system();
                        playerPositions.put(new Player(messagePlayer1, PlayerType.ENEMY), null);
                    }
                });
                if (!canStart()) {
                    throw new IllegalArgumentException("serverId and username cannot be null");
                }
                clientListeners.forEach(listener -> listener.gameStarted(serverId));
            }
            LoggerG.setMessage("[DEBUG] handling message in channel: <game.start>"+message.message()).system().system();
        }
        if (message.channel().equals("game.init")) {

            initPositions(message);
        }
        if (message.channel().equals("game.move.client.accepted")){
            LoggerG.setMessage("[DEBUG] handling message in channel: <game.move.client>").system().system();
            String[] split = message.message().split(":");
            String messageServerId = split[0];
            String messageUsername = split[1];
            String messagePosition = split[2];
            if (messageUsername.equals(getSelfPlayer().getUsername())) {
                updateLocalBoardByUsername(new Coordinates(Integer.parseInt(messagePosition.split(",")[0]), Integer.parseInt(messagePosition.split(",")[1])), getSelfPlayer());
                LoggerG.setMessage("[DEBUG] IGNORING SELF PLAYER: " + messageUsername + " to position: " + messagePosition).system().system();
                return;
            }
            String[] splitCoordinates = messagePosition.split(",");
            Coordinates xy = new Coordinates(Integer.parseInt(splitCoordinates[0]), Integer.parseInt(splitCoordinates[1]));
            if (!messageServerId.equals(serverId)){
                LoggerG.setMessage("[DEBUG] ServerId does not match").system().system();
                return;
            }
            LoggerG.setMessage("[DEBUG] Server moved player: " + messageUsername + " to position: " + xy).system().system();
            for (ClientListener clientListener : clientListeners) {
                clientListener.positionChanged(messageUsername, xy);
            }
        }
        if (message.channel().equals("game.turret.client.accepted")) {
            LoggerG.setMessage("[DEBUG] handling message in channel: <game.turret.client.accepted>").system().system();
            String[] split = message.message().split(":");
            String messageServerId = split[0];
            String messageUsername = split[1];
            String messagePosition = split[2];
            String[] splitCoordinates = messagePosition.split(",");
            Coordinates xy = new Coordinates(Integer.parseInt(splitCoordinates[0]), Integer.parseInt(splitCoordinates[1]));
            if (!messageServerId.equals(serverId)){
                LoggerG.setMessage("[DEBUG] ServerId does not match").system().system();
                return;
            }
            LoggerG.setMessage("[DEBUG] Server placed turret for player: " + messageUsername + " at position: " + xy).system().system();
            //get the player from the username
            if (messageUsername.matches(getSelfPlayer().getUsername())) {
                localBoard[xy.x()][xy.y()].setBackground(Color.BLUE);
                clientListeners.forEach(listener -> listener.turretPlaced(getSelfPlayer(), xy));
            } else {
                localBoard[xy.x()][xy.y()].setBackground(Color.BLACK);
                clientListeners.forEach(listener -> listener.turretPlaced(getEnemyPlayer(), xy));
            }
        }
        if (message.channel().equals("game.hit")) {
            LoggerG.setMessage("[DEBUG] handling message in channel: <game.hit>").system().system();
            String[] split = message.message().split(":");
            String messageServerId = split[0];
            String messageDamage = split[1];
            String messageUsername = split[2];
            if (!messageServerId.equals(serverId)){
                LoggerG.setMessage("[DEBUG] ServerId does not match").system().system();
                return;
            }
            LoggerG.setMessage("[DEBUG] Server hit player: " + messageUsername).system().system();
            if (messageUsername.equals(getSelfPlayer().getUsername())) {
                getSelfPlayer().hit(Integer.parseInt(messageDamage));
                clientListeners.forEach(listener -> listener.healthChanged(PlayerType.SELF));
            } else {
                getEnemyPlayer().hit(Integer.parseInt(messageDamage));
                clientListeners.forEach(listener -> listener.healthChanged(PlayerType.ENEMY));
            }
        }
        if (message.channel().equals("game.projectile")){
            channelProjectileMovedOrCreated(message.message());
        }
        if (message.channel().equals("server.imalive")){
            LoggerG.setMessage("[DEBUG] handling message in channel: <imalive>").system().system();
            String[] split = message.message().split(":");

            boolean alive = split[0].equals("true");
            if (alive) {
                Optional<String> optionalServerId = Optional.of(split[1]);
                serverIsAlive(alive, optionalServerId);
            }
        }
        if (message.channel().equals("game.bullet.remove")){
            channelProjectileRemoved(message.message());
        }
        if (message.channel().equals("game.end")){
            LoggerG.setMessage("[DEBUG] handling message in channel: <game.end>").system().system();
            String[] split = message.message().split(":");
            String messageServerId = split[0];
            String messageWinnerPlayer = split[1];
            if (!messageServerId.equals(serverId)){
                LoggerG.setMessage("[DEBUG] ServerId does not match").system().system();
                return;
            }
            if (messageWinnerPlayer.equals(getSelfPlayer().getUsername())) {
                LoggerG.setMessage("[DEBUG] Server ended, winner: " + messageWinnerPlayer).system().system();
                clientListeners.forEach(listener -> listener.gameEnded(selfPlayer.getUsername()));
            }
            LoggerG.setMessage("[DEBUG] Server ended game for serverId: " + serverId).system().system();
            clientListeners.forEach(listener -> listener.gameEnded(serverId));
        }

    }

    private void channelProjectileRemoved(String message) {
        LoggerG.setMessage("[DEBUG] handling message in channel: <game.bullet.remove>").system().system();
        String[] split = message.split(":");
        if (split.length == 2) {
            String messageServerID = split[0];
            String messageBulletID = split[1];
            if (!messageServerID.equals(serverId)){
                LoggerG.setMessage("[DEBUG] ServerId does not match").system().system();
                return;
            }
            LoggerG.setMessage("[DEBUG] Server DELETED projectile with id: " + messageBulletID).system().system();
            Coordinates coordinates = bulletsId.get(Long.parseLong(messageBulletID));
            localBoard[coordinates.x()][coordinates.y()].setBackground(Color.WHITE);
            return;
        }
        String messageServerID = split[0];
        if (!messageServerID.equals(serverId)){
            LoggerG.setMessage("[DEBUG] ServerId does not match").system().system();
            return;
        }
        String messageBulletId = split[1];
        String messageCoords = split[2];
        messageCoords = messageCoords.replace("(", "");
        messageCoords = messageCoords.replace(").system();", "");
        String[] splitCoords = messageCoords.split(",");
        Coordinates messageCoordinates = new Coordinates(Integer.parseInt(splitCoords[0]), Integer.parseInt(splitCoords[1]));
        bulletsId.remove(Long.parseLong(messageBulletId));
        localBoard[messageCoordinates.x()][messageCoordinates.y()].setBackground(Color.WHITE);
        LoggerG.setMessage("[DEBUG] Server removed projectile at position: " + messageCoordinates).system().system();
    }

    private void channelProjectileMovedOrCreated(String message){
        String[] split = message.split(":");
        String messageServerID = split[0];
        if (!messageServerID.equals(serverId)){
            LoggerG.setMessage("[DEBUG] ServerId does not match").system().system();
            return;
        }
        String messageBulletId = split[1];
        String messageCoords = split[2];
        messageCoords = messageCoords.replace("(", "");
        messageCoords = messageCoords.replace(").system();", "");
        String[] splitCoords = messageCoords.split(",");
        if (bulletsId.get(Long.parseLong(messageBulletId)) == null) {
            LoggerG.setMessage("[DEBUG] Server created projectile at position: " + splitCoords[0] + " " + splitCoords[1]).system().system();
            bulletsId.put(Long.parseLong(messageBulletId),new Coordinates(Integer.parseInt(splitCoords[0]), Integer.parseInt(splitCoords[1])));
        }
        else {
            LoggerG.setMessage("[DEBUG] Server moved projectile at position: " + splitCoords[0] + " " + splitCoords[1]).system().system();
            Coordinates previousCoordinates = bulletsId.get(Long.parseLong(messageBulletId));
            localBoard[previousCoordinates.x()][previousCoordinates.y()].setBackground(Color.WHITE);
            bulletsId.put(Long.parseLong(messageBulletId),new Coordinates(Integer.parseInt(splitCoords[0]), Integer.parseInt(splitCoords[1])));
        }

        Coordinates xy = new Coordinates(Integer.parseInt(splitCoords[0]), Integer.parseInt(splitCoords[1]));
        LoggerG.setMessage("[DEBUG] Server moved projectile to position: " + xy).system().system();
        localBoard[xy.x()][xy.y()].setBackground(Color.YELLOW);
    }
    public void updateLocalBoardByUsername(Coordinates coordinates, Player player) {
        LoggerG.setMessage("[DEBUG] updating local board for player: " + player.getUsername() + " at position: " + coordinates).system().system();
        Coordinates playerCoordinates = playerPositions.get(player);
        if (player.getUsername().equals(getSelfPlayer().getUsername())) {
            try {
                setLocalBoardCoordinates(coordinates, PlayerType.SELF);
            }catch (ArrayIndexOutOfBoundsException e) {
                LoggerG.setMessage("[DEBUG] ArrayIndexOutOfBoundsException: " + e.getMessage()).system().system();
                playerPositions.put(player, playerCoordinates);
            }
        } else if (player.getUsername().equals(getEnemyPlayer().getUsername())) {
            try {
                setLocalBoardCoordinates(coordinates, PlayerType.ENEMY);
            }catch (ArrayIndexOutOfBoundsException e) {
                LoggerG.setMessage("[DEBUG] ArrayIndexOutOfBoundsException: " + e.getMessage()).system().system();
                playerPositions.put(player, playerCoordinates);
            }
        }
    }

    private void setLocalBoardCoordinates(Coordinates coordinates, PlayerType playerType) throws ArrayIndexOutOfBoundsException{
        Player player = playerPositions.keySet().stream().filter(p -> p.getType() == playerType).findFirst().orElseThrow(() -> new IllegalArgumentException("Player not found"));
        Coordinates previousCoordinates = playerPositions.get(player);
        if (playerType == PlayerType.SELF) {
            localBoard[coordinates.x()][coordinates.y()].setBackground(Color.GREEN);
            this.lastCoordinates = previousCoordinates;
        } else {
            localBoard[coordinates.x()][coordinates.y()].setBackground(Color.RED);
        }

        playerPositions.put(player, coordinates);
        if (previousCoordinates != null) {
            localBoard[previousCoordinates.x()][previousCoordinates.y()].setBackground(Color.WHITE);
        }
    }

    public void removeClientListener(ClientListener clientListener) {
        clientListeners.remove(clientListener);
    }

    public void requestToUpdatePosition(Coordinates coordinates, Player player) {
        if (coordinates.x()<0 || coordinates.y()<0 || coordinates.x()>=Settings.getInstance().getHeight() || coordinates.y()>=Settings.getInstance().getWitdh()){
            LoggerG.setMessage("[DEBUG] Coordinates out of bounds").system().system();
            return;
        }
        RedisManager.getInstance().publish("game.move.server", serverId + ":" + player.getUsername() + ":" + coordinates.x() + "," + coordinates.y());
    }

    private Direction getDirectionFromCoordinates(Coordinates coordinates) {
        if (lastCoordinates == null) {
            return Direction.NONE;
        }
        if (coordinates.x() == lastCoordinates.x() && coordinates.y() == lastCoordinates.y()) {
            return Direction.NONE;
        }
        if (coordinates.x() == lastCoordinates.x() && coordinates.y() > lastCoordinates.y()) {
            return Direction.RIGHT;
        }
        if (coordinates.x() == lastCoordinates.x()) {
            return Direction.LEFT;
        }
        if (coordinates.x() > lastCoordinates.x() && coordinates.y() == lastCoordinates.y()) {
            return Direction.DOWN;
        }
        if (coordinates.x() < lastCoordinates.x() && coordinates.y() == lastCoordinates.y()) {
            return Direction.UP;
        }
        return Direction.NONE;
    }
    public void requestToPlaceTurret(Coordinates coordinates) {
        LoggerG.setMessage("[DEBUG] requesting to place turret at coordinates: " + coordinates).system().system();
        //todo implementare il controllo per vedere se il giocatore ha già un turret
        /*Optional<Items> items = playerItems.stream().filter(item -> item instanceof Turret).findFirst();
        if (items.isEmpty()) {
            LoggerG.setMessage("[DEBUG] Turret not found").system().system();
            return;
        }*/
        RedisManager.getInstance().publish("game.turret.server",
                serverId+":"+selfPlayer.getUsername()+":"+coordinates.x() + "," + coordinates.y());
    }

    public void setSelfPlayer(Player player) {
        this.selfPlayer = player;
    }

    public void checkServer() {
        LoggerG.setMessage("[DEBUG] checking server").system().system();
        final Optional<String> general = RedisManager.getInstance().hget(SERVER_STATUS, "GENERAL");
        //check if the server is alive
        if (general.isEmpty()) {
            serverIsAlive(false,Optional.empty());
            LoggerG.setMessage("[ERROR] Server is not alive").system().system();
            return ;
        }
        if(general.get().equals("ALIVE")){
            LoggerG.setMessage("[DEBUG] Server is alive").system().system();
            serverAlive = true;
            Map<String, String> matrice = RedisManager.getInstance().hgetAll("MATRICE");
            for (Map.Entry<String, String> stringStringEntry : matrice.entrySet()) {
                if (stringStringEntry.getValue().equals("WAITING")){
                    serverIsAlive(true, Optional.of(stringStringEntry.getKey()));
                    return;
                }
            }
        }
        serverIsAlive(false, Optional.empty());
    }

    public synchronized boolean isServerAlive() {
        return serverAlive;
    }

    public void reset() {
        this.serverId = null;
        this.bulletsId = new HashMap<>();
        this.localBoard = new SingleCellPanel[Settings.getInstance().getHeight()][Settings.getInstance().getWitdh()];
        this.playerPositions = new HashMap<>();
        this.clientListeners = new CopyOnWriteArrayList<>();
        this.selfPlayer = null;
        this.lastCoordinates = null;
        this.serverAlive = false;
        this.clientAccpted = false;
    }
}