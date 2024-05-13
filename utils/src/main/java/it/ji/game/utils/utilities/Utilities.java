package it.ji.game.utils.utilities;

import it.ji.game.utils.logic.Coordinates;

public class Utilities {
    public static String[] leaveOnlyNumbers(String toClean) {
        toClean =toClean.replaceAll("[^0-9,]", "");
        String[] split = toClean.split(",");
        return split;
    }
    public static void main(String[] args) {
        String test = "[51,2]";
        String[] split = leaveOnlyNumbers(test);
        for (String s : split) {
            System.out.println(s);
        }
    }
}
