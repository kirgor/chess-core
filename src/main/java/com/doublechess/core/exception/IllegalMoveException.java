package com.doublechess.core.exception;

public class IllegalMoveException extends Exception {
    public IllegalMoveException() {
        super("Move is illegal");
    }

    public IllegalMoveException(String message) {
        super(message);
    }
}
