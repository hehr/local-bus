package com.hehr.lib.socket;

import java.io.IOException;

interface IServer {

    /**
     * bind to listen
     *
     * @param remoteAddr 链接地址
     * @param observer   监听器
     * @return {@link Socket}
     */
    void bind(String remoteAddr, Server.Observer observer) throws IOException;

    /**
     * close server
     */
    void close();
}
