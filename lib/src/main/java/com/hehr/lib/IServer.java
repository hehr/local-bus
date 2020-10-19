package com.hehr.lib;

/**
 * sock server 定义
 *
 * @author hehr
 */
public interface IServer extends IBus {

    /**
     * 断开连接
     */
    void close();

}
