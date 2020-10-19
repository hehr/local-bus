package com.hehr.lib.socket;

interface IClient {


    /**
     * connet to server
     *
     * @param remoteAddr 域名地址
     * @return
     */
    Socket connect(String remoteAddr);


}
