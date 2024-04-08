package it.ji.game.utils.logic.objects;

import it.ji.game.utils.logic.Player;
import it.ji.game.utils.redis.RedisManager;
import it.ji.game.utils.redis.RedisMessage;
import it.ji.game.utils.redis.RedisMessageListener;

import java.util.List;

public class Turret implements Items {
    private Player owner;
    private int duration;
    private int damage;
    private int range;
    private int cost;
    private String serverID;
    private String infoString;

    public Turret(Player owner, int duration, int damage, int range, int cost, String serverID) {
        this.owner = owner;
        this.duration = duration;
        this.damage = damage;
        this.range = range;
        this.cost = cost;
        this.serverID = serverID;
        this.infoString = serverID+":"+owner.username()+":"+duration+":"+damage+":"+range+":"+cost;
    }
    @Override
    public void use() throws Exception {
        System.out.println("Turret used");
        for (int i = 0; i < duration; i++) {
            Thread.sleep(2000);
        }
    }
    private void shoot(){

    }
    public String getInfoString() {
        return infoString;
    }

}
