package it.ji.game.utils.logic.objects;

public interface TurretListener {
    void onBulletMoved(int x, int y, int damage);
    void onBulletRemoved(int x, int y);
}
