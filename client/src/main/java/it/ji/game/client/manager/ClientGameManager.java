package it.ji.game.client.manager;


import it.ji.game.client.exceptions.ServerNotFoundException;
import it.ji.game.client.gui.ClientListener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientGameManager implements RedisMessageListener {
    private static ClientGameManager instance = null;
    private String serverId;
    private SingleCellPanel[][] localBoard = new SingleCellPanel[Settings.getInstance().getHeight()][Settings.getInstance().getWitdh()];
    private Map<Player, Coordinates> playerPositions = new HashMap<>();
    private List<ClientListener> clientListeners = new CopyOnWriteArrayList<>();

    private ClientGameManager() {
        RedisManager.getInstance().subscribe(this,"login.status.accepted", "game.start", "game.init","game.move.client");
    }
    public void addPlayer(Player selfPlayer) {
        playerPositions.put(selfPlayer, null);
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
                .filter(player -> player.type() == type)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Player not found"));
    }
    public void setPlayerPositions(Map<Player, Coordinates> playerPositions) {
        this.playerPositions = playerPositions;
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
                playerPositions.keySet().stream().allMatch(player -> player.username() != null && !player.username().isBlank() && !player.username().isEmpty());
    }
    public void startClient() throws ServerNotFoundException {
        if (isServerWaiting(serverId)) {

            playerPositions.keySet().stream().filter(player -> player.type()== PlayerType.SELF).findFirst().ifPresentOrElse(player -> {
                RedisManager.getInstance().publish("login", serverId + ":" + player.username());
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
                .filter(player -> player.type() == PlayerType.SELF)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Self player not found"));
    }
    public Player getEnemyPlayer() {
        return playerPositions.keySet().stream()
                .filter(player -> player.type() == PlayerType.ENEMY)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Enemy player not found"));
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
            if (messageServerId.equals(serverId) && messageUsername.equals(getSelfPlayer().username())) {
                System.out.println("[DEBUG] Server accepted user: " + messageUsername + " serverId: " + messageServerId);
//                synchronized (clientListeners) {
                    clientListeners.forEach(listener -> listener.userAccepted(messageServerId, messageUsername));
//                }
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
                    if (entry.getKey().username().equals(messagePlayer1)) {
                        playerPositions.put(new Player(messagePlayer2, PlayerType.ENEMY), null);
                    } else if (entry.getKey().username().equals(messagePlayer2)) {
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
        if (message.channel().equals("game.move.client")){
            System.out.println("[DEBUG] handling message in channel: <game.move.client>");
            String[] split = message.message().split(":");
            String messageServerId = split[0];
            String messageUsername = split[1];
            String messagePosition = split[2];
            if (messageUsername.equals(getSelfPlayer().username())) {
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
        if (playerFromType.username().equals(initMessageUsername)) {
            setLocalBoardCoordinates(xy, PlayerType.SELF);
        } else {
            setLocalBoardCoordinates(xy, PlayerType.ENEMY);
        }
        System.out.println("[DEBUG] Server initialized game for serverId: " + serverId);

    }
    private void setLocalBoardCoordinates(Coordinates coordinates, PlayerType playerType) throws ArrayIndexOutOfBoundsException{
       Player player = playerPositions.keySet().stream().filter(p -> p.type() == playerType).findFirst().orElseThrow(() -> new IllegalArgumentException("Player not found"));
       Coordinates previousCoordinates = playerPositions.get(player);
        if (playerType == PlayerType.SELF) {
            localBoard[coordinates.x()][coordinates.y()].setBackground(Color.GREEN);
        } else {
            localBoard[coordinates.x()][coordinates.y()].setBackground(Color.RED);
        }
        playerPositions.put(player, coordinates);
        if (previousCoordinates != null) {
            localBoard[previousCoordinates.x()][previousCoordinates.y()].setBackground(Color.WHITE);
        }
        if (playerType == PlayerType.ENEMY) {
            return;
        }
        RedisManager.getInstance().publish("game.move.server", serverId + ":" + player.username() + ":" + coordinates.x() + "," + coordinates.y());
    }

    public void removeClientListener(ClientListener clientListener) {
        clientListeners.remove(clientListener);
    }

    public void updateLocalBoardByUsername(Coordinates coordinates, Player player) {
        System.out.println("[DEBUG] updating local board for player: " + player.username() + " at position: " + coordinates);
        Coordinates playerCoordinates = playerPositions.get(player);
            if (player.username().equals(getSelfPlayer().username())) {
                try {
                    setLocalBoardCoordinates(coordinates, PlayerType.SELF);
                }catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("[DEBUG] ArrayIndexOutOfBoundsException: " + e.getMessage());
                   playerPositions.put(player, playerCoordinates);
                }
            } else if (player.username().equals(getEnemyPlayer().username())) {
                try {
                    setLocalBoardCoordinates(coordinates, PlayerType.ENEMY);
                }catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("[DEBUG] ArrayIndexOutOfBoundsException: " + e.getMessage());
                    playerPositions.put(player, playerCoordinates);
                }
            }
        }
    }