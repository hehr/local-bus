package com.hehr.lib;


import com.hehr.lib.multipart.Extra;
import com.hehr.lib.multipart.Multipart;
import com.hehr.lib.socket.Client;
import com.hehr.lib.socket.Socket;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * socket client impl
 *
 * @author hehr
 */
public abstract class BusClient implements IClient, ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, "client-" + join());
    }


    /**
     * sock holder
     */
    private InnerHolder mHolder;

    public BusClient() {

        mExecuteThread.execute(new Runnable() {

            Lock lock = new ReentrantLock();

            Condition isConnected = lock.newCondition();

            @Override
            public void run() {

                Socket socket = new Client().connect(DEFAULT_ADDRESS);

                lock.lock();
                try {
                    while (socket == null) {
                        android.util.Log.d(join(), "connect failed ,try it again ... ");
                        isConnected.await(100, TimeUnit.MILLISECONDS);
                        socket = new Client().connect(DEFAULT_ADDRESS);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }

                android.util.Log.d(join(), join() + " connect " + DEFAULT_ADDRESS + " server success ,connect states : " + socket.isConnect());

                mHolder = new InnerHolder(socket);

                mExecuteThread.execute(mHolder);

            }
        });

    }

    private java.util.concurrent.ExecutorService mExecuteThread = Executors.newSingleThreadExecutor(this);


    private class InnerHolder implements Runnable {

        private volatile boolean isRunning = false;

        private Socket mSocket;

        public InnerHolder(Socket socket) {
            this.mSocket = socket;

        }

        public void write(Multipart multipart) {
            synchronized (this) {
                if (isConnect()) {
                    try {
                        mSocket.write(multipart);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    android.util.Log.e(join(), "write failed , connect have broken !");
                }
            }
        }

        public boolean isConnect() {
            return mSocket != null && mSocket.isConnect();
        }

        public void close() {

            write(Multipart.newBuilder()
                    .setName(join())
                    .setType(Type.exit.value)
                    .setTopic("client.exit")
                    .build());

            isRunning = false;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    onExit();
                }
            }).start();

        }

        @Override
        public void run() {

            if (mSocket.isConnect()) {

                write(Multipart.newBuilder()
                        .setType(Type.join.value)
                        .setName(join())
                        .setTopic("client.join")
                        .setExtra(Extra.newBuilder()
                                .setCharacter(join())
                                .build())
                        .build()
                );

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        onCrete();
                    }
                }).start();

                isRunning = true;

            } else {
                throw new IllegalStateException("connect is invalid ... ");
            }

            try {

                Multipart multipart;

                while (isRunning && mSocket.isConnect()) {
                    try {
                        if ((multipart = mSocket.read()) != null) {
                            switch (Type.findTypeByInteger(multipart.getType())) {
                                case broadcast:
                                    onReceived(multipart.getTopic(), multipart.getExtra());
                                    break;
                                case exit:
                                    close();
                                    break;
                                default:
                                    throw new IllegalStateException("unknown bus client type");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                android.util.Log.d(join(), join() + " has exits .");

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void publish(String topic) {
        if (mHolder != null && mHolder.isConnect()) {
            android.util.Log.d(join(), join() + " publish topic " + topic);
            mHolder.write(Multipart.newBuilder()
                    .setName(join())
                    .setType(Type.broadcast.value)
                    .setTopic(topic)
                    .build()
            );
        }
    }

    @Override
    public void publish(String topic, Extra data) {
        if (mHolder != null && mHolder.isConnect()) {
            android.util.Log.d(join(), join() + " publish topic " + topic);
            mHolder.write(Multipart.newBuilder()
                    .setName(join())
                    .setType(Type.broadcast.value)
                    .setTopic(topic)
                    .setExtra(data)
                    .build()
            );
        }
    }


    @Override
    public void subscribe(String... topics) {
        if (mHolder != null && mHolder.isConnect()) {
            for (String topic : topics) {
                mHolder.write(Multipart.newBuilder()
                        .setName(join())
                        .setType(Type.subscribe.value)
                        .setTopic(topic)
                        .build()
                );
            }
        }
    }

    @Override
    public void unsubscribe(String... topics) {
        if (mHolder != null && mHolder.isConnect()) {
            for (String topic : topics) {
                mHolder.write(Multipart.newBuilder()
                        .setName(join())
                        .setTopic(topic)
                        .setType(Type.unsubscribe.value)
                        .build()
                );
            }
        }
    }

    @Override
    public void close() {
        if (mHolder != null && mHolder.isConnect()) {
            mHolder.close();
        }
    }

    public abstract void onCrete();


    public abstract void onExit();


}
