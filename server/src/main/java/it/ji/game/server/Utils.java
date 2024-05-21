package it.ji.game.server;

import it.ji.game.utils.logic.Coordinates;
import it.ji.game.utils.settings.Settings;

public class Utils {

    //create a random string of capital letters with length 4
    public static String generateServerId(){
        String serverId = "";
        for (int i = 0; i < 4; i++) {
            serverId += (char) (Math.random() * 26 + 'A');
        }
        return serverId;
    }
    public static Coordinates getRandomCoordinates(){
        return new Coordinates((int) (Math.random()* Settings.getInstance().getHeight()),(int) (Math.random()*Settings.getInstance().getWitdh()));
    }

}
