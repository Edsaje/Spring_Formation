package com.hibouxe.square_games.exception;

public class NotPlayerTurnException extends RuntimeException {

    public NotPlayerTurnException(String message) {
        super(message);
    }
}
