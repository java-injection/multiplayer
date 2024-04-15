package it.ji.game.utils.logic.objects;

import java.util.List;

public class TurretManager {
    private static TurretManager instance = null;
    private List<TurretListener> turretListeners;
    private TurretManager() {
    }

    public static TurretManager getInstance() {
        if (instance == null) {
            instance = new TurretManager();
        }
        return instance;
    }

    public void addTurretListener(TurretListener turret) {
        turretListeners.add(turret);
    }

    public void removeTurretListener(TurretListener turret) {
        turretListeners.remove(turret);
    }

    public void notifyBulletMoved(int x, int y, int damage) {
        for (TurretListener turret : turretListeners) {
            turret.onBulletMoved(x, y, damage);
        }
    }

    public void notifyBulletRemoved(int x, int y) {
        for (TurretListener turret : turretListeners) {
            turret.onBulletRemoved(x, y);
        }
    }
}
