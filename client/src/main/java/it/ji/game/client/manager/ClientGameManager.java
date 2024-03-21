package it.ji.game.client.manager;


import it.ji.game.client.exceptions.ServerNotFoundException;
import it.ji.game.client.gui.ClientListener;
import it.ji.game.utils.logic.Player;
import it.ji.game.utils.redis.RedisManager;
import it.ji.game.utils.redis.RedisMessage;
import it.ji.game.utils.redis.RedisMessageListener;
import it.ji.game.utils.settings.Settings;
import it.ji.game.utils.settings.Status;

import java.util.LinkedList;

public class ClientGameManager implements RedisMessageListener {
    private static ClientGameManager instance = null;
    private String serverId;
    private Integer[][] localBoard;

    private Player selfPlayer;
    private Player enemyPlayer;
    private LinkedList<ClientListener> clientListeners = new LinkedList<>();

    private ClientGameManager() {
        RedisManager.getInstance().subscribe("login.status.accepted", this);
    }
    public void setSelfPlayer(Player selfPlayer) {
        this.selfPlayer = selfPlayer;
    }
    public void setEnemyPlayer(Player enemyPlayer) {
        this.enemyPlayer = enemyPlayer;
    }

    public void addClientListner(ClientListener listner){
        clientListeners.add(listner);
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
            System.out.println("[DEBUG] handling messag in channel: <login.status.accepted>");
            String[] split = message.message().split(":");
            System.out.println("[DEBUG] split: [" + split[0] + "] and [" + split[1] + "]");
            if (split[0].equals(serverId)) {
                serverId = split[0];
                String username = split[1];
                System.out.println("[DEBUG] Server accepted user: " + username + " serverId: " + serverId);
                clientListeners.forEach(listener -> listener.userAccepted(serverId,username));
            }
        }
    }
}
