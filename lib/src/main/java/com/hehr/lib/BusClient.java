package com.hehr.lib;

import android.net.LocalSocket;
import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * socket client impl
 *
 * @author hehr
 */
public abstract class BusClient implements IClient {

    public BusClient() {
        connect();
    }

    private ExecutorService mExecuteThread = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "client-" + join());
        }
    });

    /**
     * socket holder
     */
    private class InnerHolder extends SocketHolder {

        /**
         * 节点退出
         */
        public void exit() {
            if (isConnect())
                write(new Multipart(Type.exit, "client.exit"));
            disconnect();
            onDisconnect();
        }

        @Override
        public void run() {

            if (!isConnect()) {
                if (mSocket == null) {
                    setSocket(new LocalSocket());
                }
                connect(DEFAULT_ADDRESS); //阻塞方法
            }

            while (isConnect()) {
                try {
                    Multipart multipart;
                    if ((multipart = read()) != null && !multipart.isEmpty()) {
                        int type = multipart.type;
                        String topic = multipart.topic;
                        switch (type) {
                            case Type.bct:
                                onReceived(topic, multipart.args);
                                break;
                            case Type.exit:
                                exit();
                                break;
                            case Type.callbacked:
                                callbacked(multipart);
                                break;
                            case Type.rpced:
                                called(multipart);
                                break;
                            case Type.pong: // alive check
                                Log.d(join(), "receive pong .");
                                break;
                            default:
                                throw new IllegalStateException("unknown bus client type");
                        }
                    }
                } finally {
                    if ((System.currentTimeMillis() / 1000) - lastTimeStamp > 10) {//10s check 一次消息
                        Log.d(join(), "send ping .");
                        write(new Multipart(Type.ping, "client.ping"));
                        lastTimeStamp = ((System.currentTimeMillis() / 1000));
                    }
                    if (writeFailTime >= 10) {
                        exit();
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private long lastTimeStamp = System.currentTimeMillis() / 1000;


        @Override
        protected void connected() {
            super.connected();
            Bundle joinBundle = new Bundle();
            joinBundle.putString("client.name", join());
            write(new Multipart(Type.join, "client.join").setArgs(joinBundle));
            setName(join());
            onConnected();
        }
    }

    /**
     * sock holder
     */
    private InnerHolder mHolder;

    /**
     * connect socket server
     */
    private void connect() {
        mHolder = new InnerHolder();
        mExecuteThread.execute(mHolder);
    }

    @Override
    public void publish(String topic, Bundle data) {
        if (mHolder != null && mHolder.isConnect()) {
            mHolder.write(new Multipart(Type.bct, topic).setArgs(data));
        }
    }


    @Override
    public void subscribe(String... topics) {
        if (mHolder != null && mHolder.isConnect()) {
            for (String topic : topics) {
                mHolder.write(new Multipart(Type.subscribe, topic));
            }
        }
    }

    @Override
    public void unsubscribe(String... topics) {
        if (mHolder != null && mHolder.isConnect()) {
            for (String topic : topics) {
                mHolder.write(new Multipart(Type.unsubscribe, topic));
            }
        }
    }

    @Override
    public void close() {
        if (mHolder != null && mHolder.isConnect()) {
            mHolder.exit();
        }
    }

    @Override
    public void registered(String topic, IRpc rpc) {
        if (mHolder != null && mHolder.isConnect()) {
            mHolder.registeredRpc(topic, rpc);
        }
    }

    private volatile boolean isRpcing = false;

    @Override
    public Bundle call(String topic, Bundle param) throws RemoteException {
        if (isRpcing)
            throw new RemoteException();
        else {
            synchronized (this) {
                isRpcing = true;
                if (mHolder != null && mHolder.isConnect()) {
                    try {
                        Multipart multipart = new Multipart(Type.rpc, topic);
                        if (param != null) {
                            multipart.setArgs(param);
                        }
                        Bundle bundle = mHolder.call(multipart);
                        isRpcing = false;
                        return bundle;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                isRpcing = false;
                return null;
            }
        }

    }

    /**
     * 连接完成
     */
    public abstract void onConnected();

    /**
     * 连接退出
     */
    public abstract void onDisconnect();


}