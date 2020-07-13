package com.hehr.lib.socket;

public interface IBus {

    /**
     * socket bind address
     */
    String DEFAULT_ADDRESS = "CN.HEHR.LOCAL.BUS";


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
