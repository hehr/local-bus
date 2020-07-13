package com.hehr.lib.socket;

import android.net.LocalSocket;
import android.os.Bundle;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * holder declare
 *
 * @author hehr
 */
interface ISocketHolder {

    /**
     * 设置socket
     *
     * @param socket
     */
    void setSocket(LocalSocket socket);

    /**
     * 设置节点名称
     *
     * @param name
     */
    void setName(String name);

    /**
     * 写数据流
     *
     * @param multipart
     */
    void write(Multipart multipart);

    /**
     * 读数据流
     *
     * @return
     */
    Multipart read();

    /**
     * socket 连接状态
     *
     * @return boolean
     */
    boolean isConnect();

    /**
     * 创建连接 , block method
     */
    void connect(String address);

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 注册rpc 调用
     *
     * @param topic topic
     * @param rpc   {@link IRpc}
     */
    void registeredRpc(String topic, IRpc rpc);

    /**
     * call rpc
     *
     * @param multipart {@link Multipart}
     * @return {@link Multipart}
     */
    Bundle call(Multipart multipart) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * 被其其他节点call 到
     *
     * @param multipart {@link Multipart}
     */
    void called(Multipart multipart);

    /**
     * received callback
     *
     * @param multipart {@link Multipart}
     * @return bundle
     */
    void callbacked(Multipart multipart);

}
