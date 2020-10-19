package com.hehr.lib.socket;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import java.io.IOException;

public class Client implements IClient {

    private static final String TAG = "SocketClient";

    @Override
    public Socket connect(String remoteAddr) {

        LocalSocket localSocket = new LocalSocket();

        try {
            localSocket.connect(
                    new LocalSocketAddress(remoteAddr)
            );
            return new Socket(localSocket);
        } catch (IOException e) {
            e.printStackTrace();
            android.util.Log.e(TAG, " connect server failed , try it again ! ");
        }

        return null;

    }


}
