package com.hehr.lib.socket;

import android.os.Bundle;

/**
 * client interface
 *
 * @author hehr
 */
public interface IClient extends IBus {

    /**
     * send broadcast
     *
     * @param topic topic
     */
    void publish(String topic);

    /**
     * send broadcast
     *
     * @param topic topic
     * @param data  data
     */
    void publish(String topic, String data);

    /**
     * send broadcast
     *
     * @param topic topic
     * @param data  data
     */
    void publish(String topic, byte[] data);

    /**
     * receive broadcast
     *
     * @param topic topic
     * @param data  message data
     */
    void onReceived(String topic, byte[] data);

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
     * rpc call
     *
     * @param topic topic
     * @return result
     * @throws RemoteException {@link RemoteException}
     */
    Bundle call(String topic) throws RemoteException;

    /**
     * rpc call
     *
     * @param topic String
     * @param param {@link Bundle}
     * @return result
     * @throws RemoteException {@link RemoteException}
     */
    Bundle call(String topic, Bundle param) throws RemoteException;

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
