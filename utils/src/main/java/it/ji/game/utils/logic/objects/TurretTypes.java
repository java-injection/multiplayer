package it.ji.game.utils.logic.objects;

import it.ji.game.utils.exceptions.InvalidCoordinatesException;
import it.ji.game.utils.logic.Coordinates;

public enum TurretTypes {
    BASIC_TURRET (
            (int x, int y, int turretType) -> {
                Coordinates coordinates = new Coordinates(x, y);
                Coordinates bulletUP = new Coordinates(x, y);
                Coordinates bulletDOWN = new Coordinates(x, y);
                Coordinates bulletLEFT = new Coordinates(x, y);
                Coordinates bulletRIGHT = new Coordinates(x, y);

                for (int i = 0; i < 30; i++) {
                    try {
                        Thread.sleep(10000);
                        try {
                            TurretManager.getInstance().notifyBulletRemoved(bulletUP.x(), bulletUP.y());
                            bulletUP = Coordinates.up(bulletUP);
                            TurretManager.getInstance().notifyBulletMoved(bulletUP.x(), bulletUP.y(), 10);

                        } catch (InvalidCoordinatesException e) {
                            TurretManager.getInstance().notifyBulletRemoved(bulletUP.x(), bulletUP.y());
                        }
                        try {
                            TurretManager.getInstance().notifyBulletRemoved(bulletDOWN.x(), bulletDOWN.y());
                            bulletDOWN = Coordinates.down(bulletDOWN);
                            TurretManager.getInstance().notifyBulletMoved(bulletUP.x(), bulletUP.y(), 1);
                        } catch (InvalidCoordinatesException e) {
                            TurretManager.getInstance().notifyBulletRemoved(bulletUP.x(), bulletUP.y());
                        }
                        try {
                            TurretManager.getInstance().notifyBulletRemoved(bulletLEFT.x(), bulletLEFT.y());
                            bulletLEFT = Coordinates.left(bulletLEFT);
                            TurretManager.getInstance().notifyBulletMoved(bulletUP.x(), bulletUP.y(), 1);
                        } catch (InvalidCoordinatesException e) {
                            TurretManager.getInstance().notifyBulletRemoved(bulletUP.x(), bulletUP.y());
                        }
                        try {
                            TurretManager.getInstance().notifyBulletRemoved(bulletRIGHT.x(), bulletRIGHT.y());
                            bulletRIGHT = Coordinates.right(bulletRIGHT);
                            TurretManager.getInstance().notifyBulletMoved(bulletUP.x(), bulletUP.y(), 1);
                        } catch (InvalidCoordinatesException e) {
                            TurretManager.getInstance().notifyBulletRemoved(bulletUP.x(), bulletUP.y());
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
