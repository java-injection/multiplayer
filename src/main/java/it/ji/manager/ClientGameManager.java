package it.ji.manager;

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
}
