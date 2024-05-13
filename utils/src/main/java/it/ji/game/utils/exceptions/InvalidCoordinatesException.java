package it.ji.game.utils.exceptions;

import com.mysql.cj.x.protobuf.MysqlxCrud;

public class InvalidCoordinatesException extends Exception {
    public InvalidCoordinatesException(String message) {
        super(message);
    }

}
