package com.hehr.lib.server;

public interface IServer {
    /**
     * 绑定接口，开始监听
     */
    void create();

    /**
     * 关闭服务，停止监听
     */
    void destroy();

    /**
     * 绑定监听
     */
    interface Observer {
        /**
         * 绑定操作完成
         */
        void done();
    }
}
