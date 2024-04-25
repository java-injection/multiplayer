package it.ji.game.utils.logic.objects;

import it.ji.game.utils.exceptions.InvalidCoordinatesException;
import it.ji.game.utils.logic.Coordinates;

public enum TurretTypes {
    BASIC_TURRET (
            (int x, int y, int turretType) -> {

                for (int i = 0; i < 10; i++) {
                    System.out.println("[IMPORTANT] NEW ROUND");
                    try {
                        Thread t = new Thread(()->{
                            Coordinates bulletUP = new Coordinates(x, y);
                            Coordinates bulletDOWN = new Coordinates(x, y);
                            Coordinates bulletLEFT = new Coordinates(x, y);
                            Coordinates bulletRIGHT = new Coordinates(x, y);

                            long id1 = TurretManager.getInstance().insertNewBullet(bulletUP);
                            long id2 = TurretManager.getInstance().insertNewBullet(bulletDOWN);
                            long id3 = TurretManager.getInstance().insertNewBullet(bulletLEFT);
                            long id4 = TurretManager.getInstance().insertNewBullet(bulletRIGHT);
                            for (int j = 0; j < 30; j++) {

                                try {
                                    Thread.sleep(1000);
                                    System.out.println("[IMPORTANT] X=" + x + " Y=" + y);

                                    try {
                                        if (TurretManager.getInstance().isBulletExisting(id1) ) {

                                            /*TurretManager.getInstance().notifyBulletRemoved(bulletUP.x(), bulletUP.y());*/
                                            bulletUP = Coordinates.up(bulletUP);
                                            TurretManager.getInstance().notifyBulletMoved(id1, bulletUP.x(), bulletUP.y(), 10);
                                        }
                                    } catch (InvalidCoordinatesException e) {
                                        TurretManager.getInstance().notifyBulletRemoved(id1,bulletUP.x(), bulletUP.y());
                                        TurretManager.getInstance().removeBulletFromMap(id1);
                                    }
                                    try {
                                        if (TurretManager.getInstance().isBulletExisting(id2)) {
                                            /*TurretManager.getInstance().notifyBulletRemoved(bulletDOWN.x(), bulletDOWN.y());*/
                                            bulletDOWN = Coordinates.down(bulletDOWN);
                                            TurretManager.getInstance().notifyBulletMoved(id2, bulletDOWN.x(), bulletDOWN.y(), 1);
                                        }
                                    } catch (InvalidCoordinatesException e) {
                                        TurretManager.getInstance().notifyBulletRemoved(id2,bulletDOWN.x(), bulletDOWN.y());
                                        TurretManager.getInstance().removeBulletFromMap(id2);
                                    }
                                    try {
                                        if (TurretManager.getInstance().isBulletExisting(id3)) {
                                            /*TurretManager.getInstance().notifyBulletRemoved(bulletLEFT.x(), bulletLEFT.y());*/
                                            bulletLEFT = Coordinates.left(bulletLEFT);
                                            TurretManager.getInstance().notifyBulletMoved(id3,bulletLEFT.x(), bulletLEFT.y(), 1);
                                        }

                                    } catch (InvalidCoordinatesException e) {
                                        TurretManager.getInstance().removeBulletFromMap(id3);
                                        TurretManager.getInstance().notifyBulletRemoved(id3,bulletLEFT.x(), bulletLEFT.y());
                                    }
                                    try {
                                        if (TurretManager.getInstance().isBulletExisting(id4)) {
                                            /*TurretManager.getInstance().notifyBulletRemoved(bulletRIGHT.x(), bulletRIGHT.y());*/
                                            bulletRIGHT = Coordinates.right(bulletRIGHT);
                                            TurretManager.getInstance().notifyBulletMoved(id4,bulletRIGHT.x(), bulletRIGHT.y(), 1);
                                        }
                                    } catch (InvalidCoordinatesException e) {
                                        TurretManager.getInstance().notifyBulletRemoved(id4,bulletRIGHT.x(), bulletRIGHT.y());
                                        TurretManager.getInstance().removeBulletFromMap(id4);
                                    }
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }

                            }
                        });
                        t.start();
                        Thread.sleep(10000);
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
