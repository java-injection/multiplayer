package it.ji.game.logging.defaults;

import it.ji.game.logging.Logger;

public class Main {
    public static void main(String[] args) {
        Logger.info("ciao").db().file();
    }
}
