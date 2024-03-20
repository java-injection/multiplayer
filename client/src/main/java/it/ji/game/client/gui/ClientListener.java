package it.ji.game.client.gui;

public interface ClientListener {
    void userAccepted(String serverId , String username);

    void userRejected(String serverId , String username);
    void gameStarted(String serverId);

    void gameEnded(String serverId);

}
