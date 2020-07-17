package com.hehr.lib;

public interface IBus {

    /**
     * local socket bind address
     */
    String DEFAULT_ADDRESS = "CN.HEHR.LOCAL.BUS";

    /**
     * socket host
     */
    String DEFAULT_HOST = "localhost";

    /**
     * socket  port
     */
    int DEFAULT_PORT = 49999;


    /**
     * bus operate type
     *
     * @author hehr
     */
    @interface Type {

        /**
         * mount client
         */
        int join = 0x001;

        /**
         * subscribe
         */
        int subscribe = 0x002;

        /**
         * unsubscribe
         */
        int unsubscribe = 0x003;

        /**
         * broadcast
         */
        int bct = 0x004;

        /**
         * send rpc
         */
        int rpc = 0x005;

        /**
         * rpc called
         */
        int rpced = 0x006;

        /**
         * send callback
         */
        int callback = 0x007;
        /**
         * received callback
         */
        int callbacked = 0x008;

        /**
         * registered rpc
         */
        int registered = 0x100;

        /**
         * exit
         */
        int exit = 0x101;

        /**
         * ping
         */
        int ping = 0x111;

        /**
         * pong
         */
        int pong = 0x112;

    }
}
