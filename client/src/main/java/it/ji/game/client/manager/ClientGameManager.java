package it.ji.game.client.manager;


import it.ji.game.client.exceptions.ServerNotFoundException;
import it.ji.game.client.gui.ClientListener;
import it.ji.game.client.gui.Direction;
import it.ji.game.utils.logic.PlayerType;
import it.ji.game.client.gui.SingleCellPanel;
import it.ji.game.utils.logic.Coordinates;
import it.ji.game.utils.logic.Player;
import it.ji.game.utils.logic.objects.Items;
import it.ji.game.utils.logic.objects.Turret;
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
    private List<Items> playerItems = new LinkedList<>();
    private Player selfPlayer;
    private Coordinates lastCoordinates;
    private ClientGameManager() {

        RedisManager.getInstance().subscribe(this,
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
                "game.bullet.remove"
        );
    }
    public void addPlayer(Player selfPlayer) {
        playerPositions.put(selfPlayer, null);
        this.selfPlayer = selfPlayer;
        /*playerItems.add(new Turret(selfPlayer,  serverId));*/
    }

    public void addClientListener(ClientListener listener){
        clientListeners.add(listener);
    }
    public static ClientGameManager getInstance() {
        if (instance == null) {
            instance = new ClientGameManager();
        }
        return instance;
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

    private boolean canStart(){
        //print contnents of the map and everything that could set canstart to false
        System.out.println("[DEBUG] playerPositions: " + playerPositions);
        System.out.println("[DEBUG] serverId: " + serverId);
        System.out.println("[DEBUG] playerPositions.size(): " + playerPositions.size());
        return playerPositions.size() == 2 &&
                serverId !=  null &&
                !serverId.isBlank() &&
                !serverId.isEmpty() &&
                playerPositions.keySet().stream().allMatch(player -> player.getUsername() != null && !player.getUsername().isBlank() && !player.getUsername().isEmpty());
    }
    public void startClient() throws ServerNotFoundException {
        if (isServerWaiting(serverId)) {

            playerPositions.keySet().stream().filter(player -> player.getType()== PlayerType.SELF).findFirst().ifPresentOrElse(player -> {
                RedisManager.getInstance().publish("login", serverId + ":" + player.getUsername());
            }, () -> {
                throw new IllegalArgumentException("Server is not waiting for players");
            });
            System.out.println("Waiting for the server to start the game ..");
        } else {
            throw new ServerNotFoundException("Server not found");
        }
    }
    public boolean isServerWaiting(String serverId) throws ServerNotFoundException {
        return RedisManager.getInstance().hget(Settings.getInstance().getGameName(), serverId)
                .map(status -> status.equals(String.valueOf(Status.WAITING)))
                .orElseThrow(() -> new ServerNotFoundException("Server not found"));
    }

    public Player getSelfPlayer() {
        return playerPositions.keySet().stream()
                .filter(player -> player.getType() == PlayerType.SELF)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Self player not found"));
    }
    public Player getEnemyPlayer() {
        return playerPositions.keySet().stream()
                .filter(player -> player.getType() == PlayerType.ENEMY)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Enemy player not found"));
    }

    private void initPositions(RedisMessage message) {
        System.out.println("[DEBUG] handling message in channel: <game.init>");
        String channelMessage = message.message();
        String[] split = channelMessage.split(":");
        String initMessageServerId = split[0];
        System.out.println("[DEBUG] initMessageServerId: [" + initMessageServerId+"]");
        String initMessageUsername = split[1];
        System.out.println("[DEBUG] initMessageUsername: [" + initMessageUsername+"]");
        String initMessagePosition = split[2];
        String[] splitCoordinates = initMessagePosition.split(",");
        System.out.println("[DEBUG] splitCoordinates: [" + splitCoordinates[0] + "] and [" + splitCoordinates[1] + "]");
        Coordinates xy = new Coordinates(Integer.parseInt(splitCoordinates[0]), Integer.parseInt(splitCoordinates[1]));
        if (!initMessageServerId.equals(serverId)){
            System.out.println("[DEBUG] ServerId does not match");
            return;
        }
        Player playerFromType = getPlayerFromType(PlayerType.SELF);
        if (playerFromType.getUsername().equals(initMessageUsername)) {
            setLocalBoardCoordinates(xy, PlayerType.SELF);
        } else {
            setLocalBoardCoordinates(xy, PlayerType.ENEMY);
        }
        System.out.println("[DEBUG] Server initialized game for serverId: " + serverId);

    }
    public Coordinates getCoordinatesFromPlayer(Player player) {
        return playerPositions.get(player);
    }
    @Override
    public void onMessage(RedisMessage message) {
        if (message == null || message.message() == null || message.channel() == null){
            System.out.println("[DEBUG] message is null or message.message() is null or message.channel() is null");
            return;
        }

        System.out.println("Received message: [" + message.message() + "] from channel: " + message.channel() + " serverId: " + serverId);
        if (message.channel().equals("login.status.accepted")) {
            if (getSelfPlayer() == null) {
                System.out.println("[DEBUG] selfPlayer is null");
                return;
            }
            System.out.println("[DEBUG] handling message in channel: <login.status.accepted>");
            String[] split = message.message().split(":");
            System.out.println("[DEBUG] split: [" + split[0] + "] and [" + split[1] + "]");
            String messageServerId = split[0];
            String messageUsername = split[1];
            if (messageServerId.equals(serverId) && messageUsername.equals(getSelfPlayer().getUsername())) {
                System.out.println("[DEBUG] Server accepted user: " + messageUsername + " serverId: " + messageServerId);
                clientListeners.forEach(listener -> listener.userAccepted(messageServerId, messageUsername));
            }
        }
        if (message.channel().equals("game.start")) {

            System.out.println("[DEBUG] handling message in channel: <game.start>");
            String[] split = message.message().split(":");
            String messageServerId = split[0];
            String messagePlayer1 = split[1];
            String messagePlayer2 = split[2];
            if (messageServerId.equals(serverId)) {
                System.out.println("[DEBUG] Server started game for serverId: " + serverId);

                playerPositions.entrySet().stream().findFirst().ifPresent((entry) -> {
                    if (entry.getKey().getUsername().equals(messagePlayer1)) {
                        playerPositions.put(new Player(messagePlayer2, PlayerType.ENEMY), null);
                    } else if (entry.getKey().getUsername().equals(messagePlayer2)) {
                        playerPositions.put(new Player(messagePlayer1, PlayerType.ENEMY), null);
                    }
                });

                if (!canStart()) {
                    throw new IllegalArgumentException("serverId and username cannot be null");
                }
                clientListeners.forEach(listener -> listener.gameStarted(serverId));
            }
            System.out.println("[DEBUG] handling message in channel: <game.start>"+message.message());
        }
        if (message.channel().equals("game.init")) {

            initPositions(message);
        }
        if (message.channel().equals("game.move.client.refused")) { //todo levare
            System.out.println("[DEBUG] handling message in channel: <game.move.client.refused>");
            System.out.println("[DEBUG] message: " + message.message());
            }
        if (message.channel().equals("game.move.client.accepted")){
            System.out.println("[DEBUG] handling message in channel: <game.move.client>");
            String[] split = message.message().split(":");
            String messageServerId = split[0];
            String messageUsername = split[1];
            String messagePosition = split[2];
            if (messageUsername.equals(getSelfPlayer().getUsername())) {

                updateLocalBoardByUsername(new Coordinates(Integer.parseInt(messagePosition.split(",")[0]), Integer.parseInt(messagePosition.split(",")[1])), getSelfPlayer());
                System.out.println("[DEBUG] IGNORING SELF PLAYER: " + messageUsername + " to position: " + messagePosition);
                return;
            }
            String[] splitCoordinates = messagePosition.split(",");
            Coordinates xy = new Coordinates(Integer.parseInt(splitCoordinates[0]), Integer.parseInt(splitCoordinates[1]));
            if (!messageServerId.equals(serverId)){
                System.out.println("[DEBUG] ServerId does not match");
                return;
            }
            System.out.println("[DEBUG] Server moved player: " + messageUsername + " to position: " + xy);
            for (ClientListener clientListener : clientListeners) {
                clientListener.positionChanged(messageUsername, xy);
            }
        }
        if (message.channel().equals("game.turret.client.refused")) {
            System.out.println("[DEBUG] handling message in channel: <game.turret.client.refused>");
            System.out.println("[DEBUG] message: " + message.message());
        }
        if (message.channel().equals("game.turret.client.accepted")) {
            System.out.println("[DEBUG] handling message in channel: <game.turret.client.accepted>");
            String[] split = message.message().split(":");
            String messageServerId = split[0];
            String messageUsername = split[1];
            String messagePosition = split[2];
            String[] splitCoordinates = messagePosition.split(",");
            Coordinates xy = new Coordinates(Integer.parseInt(splitCoordinates[0]), Integer.parseInt(splitCoordinates[1]));
            if (!messageServerId.equals(serverId)){
                System.out.println("[DEBUG] ServerId does not match");
                return;
            }
            System.out.println("[DEBUG] Server placed turret for player: " + messageUsername + " at position: " + xy);
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
            System.out.println("[DEBUG] handling message in channel: <game.hit>");
            String[] split = message.message().split(":");
            String messageServerId = split[0];
            String messageDamage = split[1];
            String messageUsername = split[2];
            if (!messageServerId.equals(serverId)){
                System.out.println("[DEBUG] ServerId does not match");
                return;
            }
            System.out.println("[DEBUG] Server hit player: " + messageUsername);
            if (messageUsername.equals(getSelfPlayer().getUsername())) {
                getSelfPlayer().hit(Integer.parseInt(messageDamage));
            } else {
                getEnemyPlayer().hit(Integer.parseInt(messageDamage));
            }
        }
        if (message.channel().equals("game.projectile")){
            channelProjectileMovedOrCreated(message.message());
        }
        if (message.channel().equals("game.bullet.remove")){
            channelProjectileRemoved(message.message());
        }

    }

    private void channelProjectileRemoved(String message) {
        System.out.println("[DEBUG] handling message in channel: <game.bullet.remove>");
        String[] split = message.split(":");
        if (split.length == 2) {
            String messageServerID = split[0];
            String messageBulletID = split[1];
            if (!messageServerID.equals(serverId)){
                System.out.println("[DEBUG] ServerId does not match");
                return;
            }
            Coordinates coordinates = bulletsId.get(Long.parseLong(messageBulletID));
            localBoard[coordinates.x()][coordinates.y()].setBackground(Color.WHITE);
            return;
        }
        String messageServerID = split[0];
        if (!messageServerID.equals(serverId)){
            System.out.println("[DEBUG] ServerId does not match");
            return;
        }
        String messageBulletId = split[1];
        String messageCoords = split[2];
        messageCoords = messageCoords.replace("(", "");
        messageCoords = messageCoords.replace(")", "");
        String[] splitCoords = messageCoords.split(",");
        Coordinates messageCoordinates = new Coordinates(Integer.parseInt(splitCoords[0]), Integer.parseInt(splitCoords[1]));
        bulletsId.remove(Long.parseLong(messageBulletId));
        localBoard[messageCoordinates.x()][messageCoordinates.y()].setBackground(Color.WHITE);
        System.out.println("[DEBUG] Server removed projectile at position: " + messageCoordinates);
    }

    private void channelProjectileMovedOrCreated(String message){
        String[] split = message.split(":");
        String messageServerID = split[0];
        if (!messageServerID.equals(serverId)){
            System.out.println("[DEBUG] ServerId does not match");
            return;
        }
        String messageBulletId = split[1];
        String messageCoords = split[2];
        messageCoords = messageCoords.replace("(", "");
        messageCoords = messageCoords.replace(")", "");
        String[] splitCoords = messageCoords.split(",");
        if (bulletsId.get(Long.parseLong(messageBulletId)) == null) {
            System.out.println("[DEBUG] Server created projectile at position: " + splitCoords[0] + " " + splitCoords[1]);
            bulletsId.put(Long.parseLong(messageBulletId),new Coordinates(Integer.parseInt(splitCoords[0]), Integer.parseInt(splitCoords[1])));
        }
        else {
            System.out.println("[DEBUG] Server moved projectile at position: " + splitCoords[0] + " " + splitCoords[1]);
            Coordinates previousCoordinates = bulletsId.get(Long.parseLong(messageBulletId));
            localBoard[previousCoordinates.x()][previousCoordinates.y()].setBackground(Color.WHITE);
            bulletsId.put(Long.parseLong(messageBulletId),new Coordinates(Integer.parseInt(splitCoords[0]), Integer.parseInt(splitCoords[1])));
        }

        Coordinates xy = new Coordinates(Integer.parseInt(splitCoords[0]), Integer.parseInt(splitCoords[1]));
        System.out.println("[DEBUG] Server moved projectile to position: " + xy);
        localBoard[xy.x()][xy.y()].setBackground(Color.YELLOW);
    }
    public void updateLocalBoardByUsername(Coordinates coordinates, Player player) {
        System.out.println("[DEBUG] updating local board for player: " + player.getUsername() + " at position: " + coordinates);
        Coordinates playerCoordinates = playerPositions.get(player);
        if (player.getUsername().equals(getSelfPlayer().getUsername())) {
            try {
                setLocalBoardCoordinates(coordinates, PlayerType.SELF);
            }catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("[DEBUG] ArrayIndexOutOfBoundsException: " + e.getMessage());
                playerPositions.put(player, playerCoordinates);
            }
        } else if (player.getUsername().equals(getEnemyPlayer().getUsername())) {
            try {
                setLocalBoardCoordinates(coordinates, PlayerType.ENEMY);
            }catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("[DEBUG] ArrayIndexOutOfBoundsException: " + e.getMessage());
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
            System.out.println("[DEBUG] Coordinates out of bounds");
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
        if (coordinates.x() == lastCoordinates.x() && coordinates.y() < lastCoordinates.y()) {
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
        System.out.println("[DEBUG] requesting to place turret at coordinates: " + coordinates);
        //todo implementare il controllo per vedere se il giocatore ha già un turret
        /*Optional<Items> items = playerItems.stream().filter(item -> item instanceof Turret).findFirst();
        if (items.isEmpty()) {
            System.out.println("[DEBUG] Turret not found");
            return;
        }*/
        RedisManager.getInstance().publish("game.turret.server",
                 serverId+":"+selfPlayer.getUsername()+":"+coordinates.x() + "," + coordinates.y());
    }
}