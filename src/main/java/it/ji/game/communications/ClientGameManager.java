package it.ji.game.communications;

import it.ji.game.logic.Status;
import it.ji.game.redis.RedisManager;
import it.ji.game.exceptions.ServerNotFoundException;

public class ClientGameManager {
    private static ClientGameManager instance = null;
    private String serverId;
    private ClientGameManager() {
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
        return RedisManager.getInstance().hget(ServerGameManager.GAME_NAME, serverId)
                .map(status -> status.equals(String.valueOf(Status.WAITING)))
                .orElseThrow(() -> new ServerNotFoundException("Server not found"));
    }
}
