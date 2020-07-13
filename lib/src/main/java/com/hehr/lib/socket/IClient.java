package com.hehr.lib.socket;

import android.os.Bundle;

/**
 * client interface
 *
 * @author hehr
 */
public interface IClient extends IBus{

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
     */
    Bundle call(String topic);

    /**
     * rpc call
     *
     * @param topic String
     * @param param {@link Bundle}
     * @return result
     */
    Bundle call(String topic, Bundle param);

    /**
     * client quit actively
     */
    void close();


}
