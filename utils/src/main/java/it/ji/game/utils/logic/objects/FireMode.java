package it.ji.game.utils.logic.objects;

import it.ji.game.utils.logic.Player;

@FunctionalInterface
public interface FireMode {
    void fire( int x, int y, int turretType);
}
