package it.ji.game.utils.logic.objects;

import it.ji.game.utils.exceptions.InvalidCoordinatesException;
import it.ji.game.utils.logic.Coordinates;

public enum TurretTypes {
    BASIC_TURRET (
            (int x, int y, int turretType) -> {
                Coordinates bulletUP = new Coordinates(x, y);
                Coordinates bulletDOWN = new Coordinates(x, y);
                Coordinates bulletLEFT = new Coordinates(x, y);
                Coordinates bulletRIGHT = new Coordinates(x, y);
                int id1 = TurretManager.getInstance().insertNewBullet(bulletUP);
                int id2 = TurretManager.getInstance().insertNewBullet(bulletDOWN);
                int id3 = TurretManager.getInstance().insertNewBullet(bulletLEFT);
                int id4 = TurretManager.getInstance().insertNewBullet(bulletRIGHT);
                for (int i = 0; i < 30; i++) {
                    try {
                        Thread.sleep(1000);
                        try {
                            bulletUP = Coordinates.up(bulletUP);
                            System.out.println("[DEBUG] Bullet moved UP at coordinates: " + bulletUP.x() + " " + bulletUP.y());
                            TurretManager.getInstance().notifyBulletMoved(id1,bulletUP.x(), bulletUP.y(), 10);

                        } catch (InvalidCoordinatesException e) {
                            TurretManager.getInstance().notifyBulletRemoved(bulletUP.x(), bulletUP.y());
                        }
                        try {
                            bulletDOWN = Coordinates.down(bulletDOWN);
                            System.out.println("[DEBUG] Bullet moved DOWN at coordinates: " + bulletDOWN.x() + " " + bulletDOWN.y());
                            TurretManager.getInstance().notifyBulletMoved(id2,bulletDOWN.x(), bulletDOWN.y(), 1);
                        } catch (InvalidCoordinatesException e) {
                            TurretManager.getInstance().notifyBulletRemoved(bulletDOWN.x(), bulletDOWN.y());
                        }
                        try {
                            bulletLEFT = Coordinates.left(bulletLEFT);
                            TurretManager.getInstance().notifyBulletMoved(id3,bulletLEFT.x(), bulletLEFT.y(), 1);
                        } catch (InvalidCoordinatesException e) {
                            TurretManager.getInstance().notifyBulletRemoved(bulletLEFT.x(), bulletLEFT.y());
                        }
                        try {
                            bulletRIGHT = Coordinates.right(bulletRIGHT);
                            TurretManager.getInstance().notifyBulletMoved(id4,bulletRIGHT.x(), bulletRIGHT.y(), 1);
                        } catch (InvalidCoordinatesException e) {
                            TurretManager.getInstance().notifyBulletRemoved(bulletRIGHT.x(), bulletRIGHT.y());
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            });
    /*SNIPER_TURRET,
    LASER_TURRET,
    ROCKET_TURRET;*/

    private FireMode fireMode;

    TurretTypes(FireMode fireMode) {
        this.fireMode = fireMode;
    }

    public FireMode getFireMode() {
        return fireMode;
    }
}
