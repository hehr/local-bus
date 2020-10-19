package com.hehr.lib.socket;

import android.net.LocalServerSocket;
import android.net.LocalSocket;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * server
 *
 * @author hehr
 */
public class Server implements IServer, ThreadFactory {

    private static final String TAG = "SocketServer";

    private LocalServerSocket mServer;

    private java.util.concurrent.ExecutorService mPool = Executors.newSingleThreadExecutor(this);

    private volatile boolean isRunning = false;

    @Override
    public void bind(final String remoteAddr, final Observer observer) {

        mPool.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    mServer = new LocalServerSocket(remoteAddr);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new IllegalStateException("remote address " + remoteAddr + " may already used ,try other address.");
                }

                android.util.Log.d(TAG, "socket server bind " + remoteAddr + " success , wait for accept .");

                isRunning = true;

                while (isRunning) {
                    try {
                        LocalSocket localSocket = mServer.accept();
                        observer.accept(new Socket(localSocket, 0));//非阻塞模式
                    } catch (IOException e) {
                        e.printStackTrace();
                        android.util.Log.e(TAG, "accept failed ...");
                    }
                }

            }
        });

    }

    @Override
    public void close() {

        isRunning = false;

        try {
            mServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, "bus-listen");
    }

    public interface Observer {
        /**
         * accept connect
         *
         * @param socket {@link Socket}
         */
        void accept(Socket socket);
    }
}
