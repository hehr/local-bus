package com.hehr.lib;


import com.hehr.lib.socket.Server;
import com.hehr.lib.socket.Socket;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * bus server impl
 *
 * @author hehr
 */
public class BusServer implements IServer {

    private static final String TAG = "BusServer";

    private Server mServer;

    private java.util.concurrent.ExecutorService mPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "bus-poll");
        }
    });

    private TaskHandler mHandler;

    private volatile java.util.List<Socket> innerHolderLst = new CopyOnWriteArrayList<>();

    public BusServer() {

        mHandler = new TaskHandler(new TaskHandler.Observer() {
            @Override
            public void onExit(Socket socket) {
                dequeue(socket);
            }
        });

        mPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket;
                    Iterator<Socket> iterator;
                    while (true) { //fixme support local socket epoll model
                        for (iterator = innerHolderLst.iterator(); iterator.hasNext(); ) {
                            socket = iterator.next();
                            if (socket.available() != 0) {
                                mHandler.addTask(
                                        TaskHandler.Task.newBuilder()
                                                .setMultipart(socket.read().clone())
                                                .setSocket(socket)
                                                .build()
                                );
                            }
                        }
                        Thread.sleep(5);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        });

        mServer = new Server();

        mServer.bind(DEFAULT_ADDRESS, new Server.Observer() {
            @Override
            public void accept(Socket socket) {
                if (socket != null && socket.isConnect()) {
                    android.util.Log.d(TAG, "receive accept ... ");
                    enqueue(socket);
                } else {
                    throw new IllegalStateException("invalid socket accept ...");
                }

            }
        });

    }

    private void enqueue(Socket socket) {
        synchronized (this) {
            if (innerHolderLst != null && socket.isConnect()) {
                innerHolderLst.add(socket);
            } else {
                android.util.Log.e(TAG, "drop invalid Socket");
            }
        }
    }

    private void dequeue(Socket socket) {
        synchronized (this) {
            if (innerHolderLst != null && innerHolderLst.contains(socket)) {
                innerHolderLst.remove(socket);
            }
        }
    }


    /**
     * 断开链接
     */
    @Override
    public void close() {

        if (mServer != null) {
            mServer.close();
            mServer = null;
        }

        if (innerHolderLst != null) {
            innerHolderLst.clear();
            innerHolderLst = null;
        }

        if (mHandler != null) {
            mHandler = null;
        }
    }


}