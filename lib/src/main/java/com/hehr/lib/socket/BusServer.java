package com.hehr.lib.socket;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * bus server impl
 *
 * @author hehr
 */
public class BusServer implements IServer {

    private static final String TAG = "BusServer";

    private volatile LocalServerSocket mServer;

    private int tryLimit = 100; //尝试一百次，不行就崩掉

    /**
     * 开始监听
     */
    @Override
    public void listen() {

        mPool.execute(new Runnable() {

            private Lock mLock = new ReentrantLock();

            private Condition condition = mLock.newCondition();

            @Override
            public void run() {

                mLock.lock();

                int i = 0;

                try {
                    while ((mServer = bind(DEFAULT_ADDRESS)) == null) {
                        if (++i >= tryLimit) {
                            throw new RuntimeException(" bus server bind failed , some unknown reasons happened .");
                        }
                        Log.w(TAG, "try bind server time : " + i);
                        condition.await();
                    }
                    loop(mServer);//wait for accept
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    mLock.unlock();
                }
            }
        });

    }

    /**
     * local socket
     *
     * @param address 绑定域名地址
     * @return {@link LocalServerSocket}
     */
    private LocalServerSocket bind(String address) {
        try {
            return new LocalServerSocket(address);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, address + " may already used , try to bind other address .");
            return null;
        }
    }

    /**
     * 开始监听,运行在独立线程中
     *
     * @param server {@link LocalServerSocket}
     */
    private void loop(LocalServerSocket server) {

        Log.d(TAG, "bus server ready , wait for client connect.");

        if (server == null)
            throw new RuntimeException("null local socket server");

        while (true) {
            try {
                LocalSocket socket = server.accept();
                if (socket == null) {
                    throw new RuntimeException("accept null socket");
                } else {
                    new Thread(new InnerHolder(socket)).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 断开链接
     */
    @Override
    public void close() {
        if (mServer != null) {
            try {
                mServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * hold socket to communicate with the actual client
     */
    class InnerHolder extends SocketHolder {

        public InnerHolder(LocalSocket socket) {
            setSocket(socket);
        }

        @Override
        public void run() {
            while (isConnect()) {
                try {
                    Multipart multipart = read();
                    if (multipart != null && !multipart.isEmpty()) {
                        switch (multipart.type) {
                            case Type.join:
                                String clientName = new String(multipart.args);
                                setName(clientName);
                                join(name, this);
                                break;
                            case Type.rpc:
                                rpc(name, multipart);
                                break;
                            case Type.callback:
                                callback(multipart);
                                break;
                            case Type.registered://registered rpc
                                registered(multipart.topic, name);
                                break;
                            case Type.subscribe:
                                subscribe(name, multipart);
                                break;
                            case Type.bct:
                                broadcast(name, multipart);
                                break;
                            case Type.unsubscribe:
                                unsubscribe(name, multipart);
                                break;
                            case Type.ping:
                                write(new Multipart(Type.pong, "server.pong"));
                                break;
                            case Type.exit:
                                exit(name, multipart);
                                break;
                            default:
                                Log.e(TAG, "not support operate type code : " + multipart.type);
                                throw new IllegalStateException("unknown bus server type");
                        }
                    }
                } finally {

                    if (writeFailTime >= 10) {//连续10次写入失败
                        exit(name, new Multipart(Type.exit, "write.failed"));
                    }

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * rpc thread pool
     */
    private ExecutorService mPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "bus-listen");
        }
    });

    /**
     * subscribed topics lst
     */
    private volatile Map<String, Set<String>> subscribeLst = new HashMap<>();
    /**
     * mount node names lst
     */
    private volatile Map<String, InnerHolder> innerLst = new HashMap<>();

    /**
     * 注册rpc 消息的节点关系映射
     * k topic
     * v 节点名称
     */
    private volatile Map<String, String> rpcLst = new HashMap<>();


    /**
     * client join
     *
     * @param name   client name
     * @param client {@link InnerHolder }
     */
    private void join(String name, InnerHolder client) {
        synchronized (this) {
            if (innerLst != null) {
                Log.i(TAG, " >>> " + name + " <<< " + " joined ");
                innerLst.put(name, client);
            }
        }
    }

    private Map<String, String> calledLst = new HashMap<>();

    /**
     * 收到client 发出的rpc请求
     *
     * @param fromClient from client name
     * @param multipart  {@link Multipart}
     */
    private void rpc(String fromClient, Multipart multipart) {
        synchronized (this) {
            String topic = multipart.topic;
            if (rpcLst.containsKey(topic)) {
                String registeredClient = rpcLst.get(topic);
                calledLst.put(topic, fromClient);
                innerLst.get(registeredClient).write(new Multipart(Type.rpced, topic).setRpcParam(multipart.rpcParam));
            } else {
                Log.e(TAG, "no clent registered rpc topic : " + topic);
                innerLst.get(fromClient).write(new Multipart(Type.callbacked, topic).setRpcResult(null));
            }
        }
    }

    /**
     * 收到callback 回复
     *
     * @param multipart
     */
    private void callback(Multipart multipart) {
        String topic = multipart.topic;
        if (calledLst.containsKey(topic)) {
            String fromClient = calledLst.get(topic);
            calledLst.remove(topic);
            innerLst.get(fromClient).write(new Multipart(Type.callbacked, topic).setRpcResult(multipart.rpcResult));
        }
    }

    /**
     * 注册rpc 消息
     *
     * @param topic topic
     * @param name  节点名称
     */
    private void registered(String topic, String name) {
        synchronized (this) {
            if (!TextUtils.isEmpty(topic) && innerLst.containsKey(name)) {
                Log.i(TAG, " >>> " + name + " <<< " + " registered " + topic);
                rpcLst.put(topic, name);
            }
        }
    }

    /**
     * subscribe topic
     *
     * @param name      client name
     * @param multipart {@link Multipart}
     */
    private void subscribe(final String name, Multipart multipart) {
        synchronized (this) {
            String topic = multipart.topic;
            if (!TextUtils.isEmpty(topic) && !TextUtils.isEmpty(name)) {
                Log.i(TAG, " >>> " + name + " <<< " + " subscribed " + topic);
                if (subscribeLst.containsKey(topic)) {
                    subscribeLst.get(topic).add(name);
                } else {
                    subscribeLst.put(topic, new HashSet<String>() {{
                        add(name);
                    }});
                }
            }
        }
    }

    /**
     * unsubscribe topic
     *
     * @param name      client name
     * @param multipart {@link Multipart}
     */
    private void unsubscribe(String name, Multipart multipart) {
        synchronized (this) {
            String topic = multipart.topic;
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(topic)) {
                if (subscribeLst.containsKey(topic)) {
                    Log.i(TAG, " >>> " + name + " <<< " + " unsubscribe " + topic);
                    subscribeLst.get(topic).remove(name);
                    if (subscribeLst.get(topic).isEmpty()) {
                        subscribeLst.remove(topic);
                    }
                } else {
                    Log.e(TAG, topic + " have not subscribed. ");
                }
            }
        }
    }

    /**
     * broadcast
     *
     * @param from      send client name
     * @param multipart {@link Multipart}
     */
    private void broadcast(String from, Multipart multipart) {
        String topic = multipart.topic;
        Log.d(TAG, from + " send < " + multipart.toString());
        if (subscribeLst.containsKey(topic)) {
            Set<String> targets = subscribeLst.get(topic);
            for (String name : targets) {
                if (innerLst.containsKey(name)) {
                    innerLst.get(name).write(multipart);
                } else {
                    Log.e(TAG, " discard " + name + " , " + multipart);
                }
            }
        } else {
            Log.e(TAG, " no client subscribe this topic");
        }
    }

    /**
     * client exit
     *
     * @param name      client name
     * @param multipart {@link Multipart}
     */
    private void exit(String name, Multipart multipart) {
        synchronized (this) {
            if (innerLst.containsKey(name) && multipart.type == Type.exit) {
                Log.w(TAG, " >>> " + name + " <<< " + " exit ...");
                innerLst.get(name).disconnect();
                innerLst.remove(name);
            }
        }
    }

}