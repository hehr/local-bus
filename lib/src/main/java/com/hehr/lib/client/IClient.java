package com.hehr.lib.client;

import com.hehr.lib.Extra;
import com.hehr.lib.IllegalConnectionStateException;

interface IClient {

    /**
     * subscribe topic
     *
     * @param topics topic
     * @throws IllegalConnectionStateException {@link IllegalConnectionStateException}
     */
    void subscribe(String... topics) throws IllegalConnectionStateException;

    /**
     * unsubscribe topic
     *
     * @param topics topic
     * @throws IllegalConnectionStateException {@link IllegalConnectionStateException}
     */
    void unsubscribe(String... topics) throws IllegalConnectionStateException;


    /**
     * publish topic with extra
     *
     * @param topic topic
     * @param data  data
     * @throws IllegalConnectionStateException {@link IllegalConnectionStateException}
     */
    void publish(String topic, Extra data) throws IllegalConnectionStateException;

    /**
     * publish topic without extra
     *
     * @param topic
     * @throws IllegalConnectionStateException {@link IllegalConnectionStateException}
     */
    void publish(String topic) throws IllegalConnectionStateException;

    /**
     * connect to server
     *
     * @param name declare your name
     */
    void create(String name);

    /**
     * close connection
     */
    void destroy() throws IllegalConnectionStateException;

}
