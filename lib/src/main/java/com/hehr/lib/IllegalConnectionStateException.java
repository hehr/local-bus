package com.hehr.lib;

public class IllegalConnectionStateException extends Exception {

    public IllegalConnectionStateException() {
        this("connection has broken.");
    }

    public IllegalConnectionStateException(String message) {
        super(message);
    }
}
