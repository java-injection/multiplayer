package it.ji.game.utils.logic.objects;

import it.ji.game.utils.logic.Coordinates;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TurretManager {
    private static TurretManager instance = null;
    private Map<Integer, Coordinates> bulletsId = new HashMap<>();
    private List<TurretListener> turretListeners = new LinkedList<>();
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
    public int insertNewBullet(Coordinates coordinates){
        int id = bulletsId.size()+1;
        bulletsId.put(id, coordinates);
        return id;
    }
    public void removeBullet(int id){
        bulletsId.remove(id);
    }
    public Coordinates getBulletCoordinates(int id){
        return bulletsId.get(id);
    }

    public void notifyBulletMoved(int id ,int x, int y, int damage) {
        for (TurretListener turret : turretListeners) {
            turret.onBulletMoved(id,x, y, damage);
        }
    }

    public void notifyBulletRemoved(int x, int y) {
        for (TurretListener turret : turretListeners) {
            turret.onBulletRemoved(x, y);
        }
    }
}
