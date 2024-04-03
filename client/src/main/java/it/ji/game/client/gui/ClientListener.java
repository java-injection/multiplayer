package it.ji.game.client.gui;

import it.ji.game.utils.logic.Coordinates;

public interface ClientListener {
    void userAccepted(String serverId , String username);

    void userRejected(String serverId , String username);
    void gameStarted(String serverId);

    void gameEnded(String serverId);
    void positionChanged(String username, Direction direction );

}
