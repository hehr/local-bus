package com.hehr.lib;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Bundle;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * base inner holder
 *
 * @author hehr
 */
public abstract class SocketHolder implements Runnable, ISocketHolder {

    protected LocalSocket mSocket;

    private BufferedInputStream mReader;

    private BufferedOutputStream mWriter;

    protected String name;

    @Override
    public void setSocket(LocalSocket mSocket) {
        this.mSocket = mSocket;
        if (isConnect())
            connected();

    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 写入失败
     */
    protected int writeFailTime = 0;

    @Override
    public void write(Multipart multipart) {

        if (isConnect() && mWriter != null && !multipart.isEmpty()) {
            try {
                Parcel parcel = Parcel.obtain();
                parcel.writeParcelable(multipart, 0);
                mWriter.write(parcel.marshall());
                mWriter.flush();
                parcel.recycle();
            } catch (IOException e) {
                e.printStackTrace();
                writeFailTime++;
            }
        }
    }


    @Override
    public Multipart read() {
        try {
            if (isConnect() && mReader != null && mReader.available() >= 0) {
                byte[] bytes = new byte[mReader.available()];
                mReader.read(bytes);
                Parcel parcel = Parcel.obtain();
                parcel.unmarshall(bytes, 0, bytes.length);
                parcel.setDataPosition(0);
                Multipart multipart = parcel.readParcelable(Multipart.class.getClassLoader());
                parcel.recycle();
                return multipart;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isConnect() {
        return mSocket == null ? false : mSocket.isConnected();
    }


    private Lock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    @Override
    public void connect(String address) {

        int connectLimit = 100;

        lock.lock();

        try {
            int tryTime = 0;
            while (!isConnect()) {
                if (++tryTime >= connectLimit)
                    throw new RuntimeException("local socket client connect failed , some unknown reasons happened .");

                if (mSocket != null) {
                    try {
                        mSocket.connect(new LocalSocketAddress(address));
                        mSocket.setSoTimeout(1000 * 1000 * 1000 * 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                condition.await(100, TimeUnit.MILLISECONDS);
            }

            connected(); //callback connected

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void disconnect() {

        synchronized (this) {
            try {
                if (mSocket != null) {
                    mSocket.close();
                    mSocket = null;
                }
                if (mWriter != null) {
                    mWriter.close();
                }
                if (mReader != null) {
                    mReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * topic & IRpc mapping
     */
    private Map<String, IRpc> rpcLst = new HashMap ();

    @Override
    public void registeredRpc(String topic, IRpc rpc) {
        synchronized (this) {
            if (rpc != null && !TextUtils.isEmpty(topic)) {
                rpcLst.put(topic, rpc);
                write(new Multipart(IBus.Type.registered, topic));//registered into bus
            }
        }
    }

    volatile RpcCallback callback = null;

    /**
     * 回调通知callback 结果改变
     *
     * @param bundle {@link Bundle}
     */
    private void notifyCallback(Bundle bundle) {
        synchronized (this) {
            if (callback != null) {
                callback.update(bundle);
            }
        }
    }

    @Override
    public Bundle call(final Multipart multipart) throws InterruptedException, ExecutionException {

        if (callback != null && callback.isCalled()) {
            Log.e(name, "already running rpc call , do not call multiple rpc request !");
            return null;
        }

        callback = new RpcCallback.Builder()
                .setTopic(multipart.topic)
                .setCalled(false)
                .create();

        return rpcThreadPool.submit(new Callable<Bundle>() {

            private Lock rpcLock = new ReentrantLock();

            private Condition rpcCondition = rpcLock.newCondition();

            int awaitTimes = 60 * 1000;//等待60s，然后主动超时

            @Override
            public Bundle call() throws Exception {

                write(multipart);

                int tryTime = 0;

                rpcLock.lock();

                try {
                    while (callback != null && !callback.isCalled() && tryTime < awaitTimes) {
                        tryTime++;
                        rpcCondition.await(1, TimeUnit.MILLISECONDS);//开始阻塞
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    rpcLock.unlock();
                    callback.reset();
                }

                return callback.getResult();
            }
        }).get();
    }

    /**
     * 用于rpc 操作的单线程线程池
     */
    private ExecutorService rpcThreadPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, name + "-" + "rpc");
        }
    });

    @Override
    public void callbacked(Multipart multipart) {
        if (TextUtils.equals(multipart.topic, callback.getTopic())) {
            notifyCallback(multipart.ret);
        } else {
            Log.e(name, "drop may timeout multipart " + multipart.topic);
        }
    }

    @Override
    public void called(final Multipart multipart) {
        final String topic = multipart.topic;
        if (rpcLst.containsKey(topic)) {
            try {
                //开起线程池执行callback操作，避免rpc操作阻塞节点循环线程
                feedback(topic, rpcThreadPool.submit(new Callable<Bundle>() {
                    @Override
                    public Bundle call() throws Exception {
                        return rpcLst.get(topic).invoke(multipart.args);
                    }
                }).get(59, TimeUnit.SECONDS));
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();//call timeout
            }
        } else {
            feedback(topic, null);
        }

    }

    /**
     * feedback rpc result
     *
     * @param topic
     * @param bundle
     */
    private void feedback(String topic, Bundle bundle) {
        Multipart multipart = new Multipart(IBus.Type.callback, topic);
        if (bundle != null && !bundle.isEmpty()) {
            multipart.setRet(bundle);
        }
        write(multipart);
    }


    /**
     * connected
     */
    protected void connected() {

        if (mSocket != null && isConnect()) {
            try {
                mReader = new BufferedInputStream(mSocket.getInputStream());
                mWriter = new BufferedOutputStream(mSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
