package it.ji.game.client.manager;


import it.ji.game.client.exceptions.ServerNotFoundException;
import it.ji.game.client.gui.ClientListener;
import it.ji.game.utils.logic.Coordinates;
import it.ji.game.utils.logic.Player;
import it.ji.game.utils.redis.RedisManager;
import it.ji.game.utils.redis.RedisMessage;
import it.ji.game.utils.redis.RedisMessageListener;
import it.ji.game.utils.settings.Settings;
import it.ji.game.utils.settings.Status;
import it.ji.game.utils.utilities.Utilities;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ClientGameManager implements RedisMessageListener {
    private static ClientGameManager instance = null;
    private String serverId;
    private Integer[][] localBoard;

    private Player selfPlayer;
    private Player enemyPlayer;
    private List<ClientListener> clientListeners = Collections.synchronizedList(new LinkedList<>());

    private ClientGameManager() {
        RedisManager.getInstance().subscribe(this,"login.status.accepted", "game.start", "game.init");
    }
    public void setSelfPlayer(Player selfPlayer) {
        this.selfPlayer = selfPlayer;
    }
    public void setEnemyPlayer(Player enemyPlayer) {
        this.enemyPlayer = enemyPlayer;
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

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerId() {
        return serverId;
    }

    public void startClient(String serverId, String username) throws ServerNotFoundException {
        if (serverId == null || username == null || serverId.isBlank() || username.isBlank() || serverId.isEmpty() || username.isEmpty() ) {
            throw new IllegalArgumentException("serverId and username cannot be null");
        }
        if (isServerWaiting(serverId)) {
            RedisManager.getInstance().publish("login", serverId + ":" + username);
            System.out.println("Waiting for the server to start the game ..");
        }
        else {
            throw new IllegalArgumentException("Server is not waiting for players");
        }
    }
    public boolean isServerWaiting(String serverId) throws ServerNotFoundException {
        return RedisManager.getInstance().hget(Settings.getInstance().getGameName(), serverId)
                .map(status -> status.equals(String.valueOf(Status.WAITING)))
                .orElseThrow(() -> new ServerNotFoundException("Server not found"));
    }

    public String getSelfPlayer() {
        return selfPlayer.username();
    }

    @Override
    public void onMessage(RedisMessage message) {
        System.out.println("Received message: [" + message.message() + "] from channel: " + message.channel() + " serverId: " + serverId);
        if (message.channel().equals("login.status.accepted")) {
            if (this.selfPlayer == null) {
                System.out.println("[DEBUG] selfPlayer is null");
                return;
            }
            System.out.println("[DEBUG] handling message in channel: <login.status.accepted>");
            String[] split = message.message().split(":");
            System.out.println("[DEBUG] split: [" + split[0] + "] and [" + split[1] + "]");
            String messageServerId = split[0];
            String messageUsername = split[1];
            if (messageServerId.equals(serverId) && messageUsername.equals(selfPlayer.username())) {
                System.out.println("[DEBUG] Server accepted user: " + messageUsername + " serverId: " + messageServerId);
                synchronized (clientListeners) {
                    clientListeners.forEach(listener -> listener.userAccepted(messageServerId, messageUsername));
                }
            }
        }
        if (message.channel().equals("game.start")) {
            System.out.println("[DEBUG] handling message in channel: <game.start>");
            String messageServerId = message.message();
            if (messageServerId.equals(serverId)) {
                System.out.println("[DEBUG] Server started game for serverId: " + serverId);
                synchronized (clientListeners) {
                    clientListeners.forEach(listener -> listener.gameStarted(serverId));
                }
            }
        }
        if (message.channel().equals("game.init")) {
            initPositions(message);
        }
    }

    private void initPositions(RedisMessage message) {
        System.out.println("[DEBUG] handling message in channel: <game.init>");
        String channelMessage = message.message();
        String[] split = channelMessage.split(":");
        String initMessageServerId = split[0];
        String initMessageUsername = split[1];
        String initMessagePosition = split[2];
        String[] splitCoordinates = initMessagePosition.split(",");
        Coordinates xy = new Coordinates(Integer.parseInt(splitCoordinates[0]), Integer.parseInt(splitCoordinates[1]));
        if (!initMessageServerId.equals(serverId)){
            return;
        }
        System.out.println("[DEBUG] Server initialized game for serverId: " + serverId);

    }


}