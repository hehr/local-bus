package com.hehr.lib;

interface IClient {

    /**
     * subscribe topic
     *
     * @param topics topic
     */
    void subscribe(String... topics) throws IllegalConnectionStateException;

    /**
     * unsubscribe topic
     *
     * @param topics topic
     */
    void unsubscribe(String... topics) throws IllegalConnectionStateException;


    /**
     * publish topic with extra
     *
     * @param topic topic
     * @param data  data
     */
    void publish(String topic, Extra data) throws IllegalConnectionStateException;

    /**
     * publish topic without extra
     *
     * @param topic topic
     */
    void publish(String topic) throws IllegalConnectionStateException;


}
