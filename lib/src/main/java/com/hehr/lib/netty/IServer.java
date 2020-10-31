package com.hehr.lib.netty;

import com.hehr.lib.IBus;

/**
 * sock server 定义
 *
 * @author hehr
 */
public interface IServer extends IBus {
    /**
     * 启动服务并绑定端口号
     *
     * @param port     端口号
     * @param observer 回调
     */
    void bind(int port, Observer observer);

    /**
     * 关闭服务
     */
    void close();


    /**
     * 绑定监听
     */
    interface Observer {
        /**
         * 绑定操作完成
         */
        void bindDone();
    }

}
