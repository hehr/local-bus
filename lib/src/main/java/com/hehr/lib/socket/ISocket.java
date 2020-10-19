package com.hehr.lib.socket;


import com.hehr.lib.multipart.Multipart;

import java.io.IOException;

public interface ISocket {


    /**
     * read
     *
     * @return size
     */
    Multipart read() throws IOException;

    /**
     * write
     *
     * @param multipart data
     */
    void write(Multipart multipart) throws IOException;

    /**
     * close connection
     */
    void close() throws IOException;

    /**
     * is connect
     *
     * @return boolean
     */
    boolean isConnect();

}
