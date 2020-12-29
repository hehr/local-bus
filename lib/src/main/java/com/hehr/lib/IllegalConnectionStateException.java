package com.hehr.lib;

/**
 * 非法链接状态异常
 *
 * @author hehr
 */
public class IllegalConnectionStateException extends Exception {

    public IllegalConnectionStateException() {
        this("connection has broken.");
    }

    public IllegalConnectionStateException(String message) {
        super(message);
    }
}
