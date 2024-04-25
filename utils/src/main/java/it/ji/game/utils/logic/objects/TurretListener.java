package it.ji.game.utils.logic.objects;

public interface TurretListener {
    void onBulletMoved(long id,int x, int y, int damage);
    void onBulletRemoved(long id, int x, int y);
}
