package it.ji.game.client.gui;

import it.ji.game.utils.logic.Coordinates;
import it.ji.game.utils.logic.Player;
import it.ji.game.utils.logic.PlayerType;

import java.util.Optional;

public interface ClientListener {
    void userAccepted(String serverId , String username);

    void userRejected(String serverId , String username);
    void gameStarted(String serverId);

    void gameEnded(String serverId);
    void positionChanged(String username, Coordinates coordinates );

    void turretPlaced(Player player, Coordinates xy);

    void bulletMoved(Coordinates xy);

    void serverIsAlive(boolean isAlive, Optional<String> serverId);

    void healthChanged(PlayerType playerType);
}
