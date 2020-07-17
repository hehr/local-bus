package com.hehr.lib.bus;

import android.os.Bundle;

import com.hehr.lib.IBus;
import com.hehr.lib.IRpc;

/**
 * client interface
 *
 * @author hehr
 */
public interface IClient extends IBus {

    /**
     * receive broadcast
     *
     * @param topic topic
     * @param data  message data
     */
    void onReceived(String topic, Bundle data);

    /**
     * join bus
     *
     * @return name
     */
    String join();

    /**
     * subscribe topic
     *
     * @param topics topic
     */
    void subscribe(String... topics);

    /**
     * unsubscribe topic
     *
     * @param topics topic
     */
    void unsubscribe(String... topics);

    /**
     * registered rpc call
     *
     * @param topic topic
     * @param rpc   {@link IRpc}
     */
    void registered(String topic, IRpc rpc);

    /**
     * send broadcast
     *
     * @param topic topic
     * @param data  data {@link Bundle}
     */
    void publish(String topic, Bundle data);

    /**
     * rpc call
     *
     * @param topic String
     * @param data  {@link Bundle}
     * @return result
     * @throws RemoteException {@link RemoteException}
     */
    Bundle call(String topic, Bundle data) throws RemoteException;

    /**
     * client quit actively
     */
    void close();

    /**
     * 重复的rpc请求
     */
    class RemoteException extends Exception {

        private static String error = "重复的rpc请求";

        public RemoteException() {
            this(error);
        }

        public RemoteException(String message) {
            super(message);
        }
    }


}
