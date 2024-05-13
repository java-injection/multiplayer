package it.ji.game.server;

public class Utils {

    //create a random string of capital letters with length 4
    public static String generateServerId(){
        String serverId = "";
        for (int i = 0; i < 4; i++) {
            serverId += (char) (Math.random() * 26 + 'A');
        }
        return serverId;
    }

}
