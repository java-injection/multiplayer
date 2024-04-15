package it.ji.game.utils.logic;

import it.ji.game.utils.exceptions.InvalidCoordinatesException;
import it.ji.game.utils.settings.Settings;

public record Coordinates(int x, int y) {
    public static Coordinates left(Coordinates coordinates) throws InvalidCoordinatesException {
        if (coordinates.x() == 0)
            throw new InvalidCoordinatesException("");
        return new Coordinates(coordinates.x() - 1, coordinates.y());
    }
    public static Coordinates right(Coordinates coordinates) throws InvalidCoordinatesException {
        if (coordinates.x() == Settings.getInstance().getWitdh() - 1)
            throw new InvalidCoordinatesException("Invalid coordinates");
        return new Coordinates(coordinates.x() + 1, coordinates.y());
    }
    public static Coordinates up(Coordinates coordinates) throws InvalidCoordinatesException {
        if (coordinates.y() == Settings.getInstance().getHeight() - 1)
            throw new InvalidCoordinatesException("Invalid coordinates");
        return new Coordinates(coordinates.x(), coordinates.y() - 1);
    }
    public static Coordinates down(Coordinates coordinates) throws InvalidCoordinatesException {
        if (coordinates.y() == 0)
            throw new InvalidCoordinatesException("Invalid coordinates");
        return new Coordinates(coordinates.x(), coordinates.y() + 1);
    }
}
