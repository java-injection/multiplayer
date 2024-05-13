package it.ji.game.utils.logic.objects;

import it.ji.game.utils.logic.Coordinates;
import it.ji.game.utils.logic.Player;

public class Turret extends Items {
    private Player owner;
    private int turretCode;

    private TurretTypes type;
    private Coordinates coordinates;

    public Turret(Player owner, String serverID, TurretTypes type, String name, Coordinates coordinates, int turretCode){
        super(name,serverID);
        this.owner = owner;
        this.turretCode = turretCode;
        this.type = type;
        this.coordinates = coordinates;
    }

    public Player getOwner() {
        return owner;
    }


    @Override
    public void use() throws Exception {
        Thread t = new Thread(() -> {
            type.getFireMode().fire(coordinates.x(), coordinates.y(),turretCode);
        });
        t.start();
    }
}
