package com.hehr.lib.netty;


import com.hehr.lib.IBus;
import com.hehr.lib.IllegalConnectionStateException;
import com.hehr.lib.proto.RespProto;

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
    void onReceived(String topic, RespProto.Resp.Extra data);


    /**
     * close connect
     */
    void close() throws IllegalConnectionStateException;


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
    void publish(String topic, RespProto.Resp.Extra data) throws IllegalConnectionStateException;

    /**
     * publish topic without extra
     *
     * @param topic topic
     */
    void publish(String topic) throws IllegalConnectionStateException;

}
