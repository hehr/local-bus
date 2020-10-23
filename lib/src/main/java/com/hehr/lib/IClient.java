package com.hehr.lib;


import com.hehr.lib.protocol.multipart.Extra;

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
    void onReceived(String topic, Extra data);

    /**
     * join bus
     *
     * @return name
     */
    String join();


    /**
     * close connect
     */
    void close();


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
     * publish topic with extra
     *
     * @param topic topic
     * @param data  data
     */
    void publish(String topic, Extra data);

    /**
     * publish topic without extra
     *
     * @param topic topic
     */
    void publish(String topic);

}
