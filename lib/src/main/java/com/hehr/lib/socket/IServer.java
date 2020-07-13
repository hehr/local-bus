package com.hehr.lib.socket;

/**
 * sock server 定义
 *
 * @author hehr
 */
public interface IServer extends IBus {

    /**
     * start listen
     *
     * @return address connect address
     */
    void listen();

    /**
     * 断开连接
     */
    void close();

}
