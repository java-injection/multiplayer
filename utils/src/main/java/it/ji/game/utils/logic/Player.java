package it.ji.game.utils.logic;

public class Player {
    private int HP = 100;
    private String name;
    private PlayerType type;

    public Player(String name, PlayerType type) {
        this.name = name;
        this.type = type;
    }

    public int getHP() {
        return HP;
    }

    public String getUsername() {
        return name;
    }
    public void hit(int damage) {
        HP -= damage;
    }


    public boolean isDead() {
        return HP <= 0;
    }
}
