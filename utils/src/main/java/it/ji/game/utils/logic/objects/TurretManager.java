package it.ji.game.utils.logic.objects;

import it.ji.game.utils.logic.Coordinates;

import java.util.*;

public class TurretManager {
    private static TurretManager instance = null;
    private Map<Long, Coordinates> bulletsId = new HashMap<>();
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
    public long insertNewBullet(Coordinates coordinates){
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {

        }
        long id = new Date().getTime();
        bulletsId.put(id, coordinates);
        System.out.println("[DEBUG] Inserted new bullet with id: " + id + " at coordinates: " + coordinates.x() + " " + coordinates.y());
        return id;
    }
    public void removeBullet(int id){
        bulletsId.remove(id);
    }
    public Coordinates getBulletCoordinates(int id){
        return bulletsId.get(id);
    }

    public void notifyBulletMoved(long id ,int x, int y, int damage) {
        for (TurretListener turret : turretListeners) {
            turret.onBulletMoved(id,x, y, damage);
        }
    }

    public void notifyBulletRemoved(long id,int x, int y) {
        for (TurretListener turret : turretListeners) {
            turret.onBulletRemoved(id,x, y);
        }
    }

    public boolean isBulletExisting(long id) {
        return bulletsId.containsKey(id);
    }
    public void removeBulletFromMap(long id){
        bulletsId.remove(id);
    }
}
