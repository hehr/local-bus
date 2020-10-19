package com.hehr.lib;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.hehr.lib.multipart.Multipart;
import com.hehr.lib.socket.Socket;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

class TaskHandler implements Handler.Callback {

    private Handler dealHandler;

    private HandlerThread dealThread = new HandlerThread("bus-handle");

    private Observer mListener;

    public TaskHandler(Observer listener) {

        mListener = listener;

        dealThread.start();

        dealHandler = new Handler(dealThread.getLooper(), this);

    }

    private static final String TAG = "BusServer";

    public static final int WHAT_ADD_TASK = 0x001;


    private volatile java.util.Map<String, Socket> innerLst = new HashMap<>();


    /**
     * subscribed topics lst
     */
    private volatile java.util.Map<String, java.util.Set<String>> subscribeLst = new ConcurrentHashMap<>();


    /**
     * client join
     *
     * @param name   client name
     * @param socket {@link Socket }
     */
    private void join(String name, Socket socket) {
        if (innerLst != null) {
            android.util.Log.i(TAG, " >>> " + name + " <<< " + " joined ");
            innerLst.put(name, socket);
        }
    }

    /**
     * subscribe topic
     *
     * @param multipart {@link Multipart}
     */
    private void subscribe(Multipart multipart) {
        String topic = multipart.getTopic();
        final String name = multipart.getName();
        if (!TextUtils.isEmpty(topic) && innerLst.containsKey(name)) {
            android.util.Log.i(TAG, " >>> " + name + " <<< " + " subscribed " + topic);
            if (subscribeLst.containsKey(topic)) {
                subscribeLst.get(topic).add(name);
            } else {
                subscribeLst.put(topic, new HashSet<String>() {{
                    add(name);
                }});
            }
        } else {
            android.util.Log.e(TAG, " not found name " + " >>> " + name + " <<< ");
        }
    }

    /**
     * unsubscribe topic
     *
     * @param multipart {@link Multipart}
     */
    private void unsubscribe(Multipart multipart) {
        String topic = multipart.getTopic();
        String name = multipart.getName();
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(topic)) {
            if (subscribeLst.containsKey(topic)) {
                android.util.Log.i(TAG, " >>> " + name + " <<< " + " unsubscribe " + topic);
                subscribeLst.get(topic).remove(name);
                if (subscribeLst.get(topic).isEmpty()) {
                    subscribeLst.remove(topic);
                }
            } else {
                android.util.Log.e(TAG, topic + " have not subscribed. ");
            }
        }
    }

    /**
     * broadcast
     *
     * @param multipart {@link Multipart}
     */
    private void broadcast(Multipart multipart) throws IOException {
        String topic = multipart.getTopic();
        String from = multipart.getName();
        if (subscribeLst.containsKey(topic)) {
            java.util.Set<String> targets = subscribeLst.get(topic);
            for (String name : targets) {
                if (innerLst.containsKey(name)) {
                    android.util.Log.d(TAG, " >>> " + name + " <<< " + " received topic " + topic + " from " + " >>> " + from + " <<< ");
                    innerLst.get(name).write(multipart);
                } else {
                    android.util.Log.e(TAG, " discard " + name + " , " + multipart);
                }
            }
        } else {
            android.util.Log.e(TAG, " no client subscribe this topic " + " >>> " + topic + " <<< ");
        }
    }


    /**
     * client exit
     *
     * @param multipart {@link Multipart}
     */
    private void exit(Multipart multipart) {

        String name = multipart.getName();

        if (innerLst.containsKey(name) && multipart.getType() == IBus.Type.exit.value) {

            android.util.Log.w(TAG, " >>> " + name + " <<< " + " exit ...");

            for (Iterator<String> iteratorKey = subscribeLst.keySet().iterator(); iteratorKey.hasNext(); ) {

                String topic = iteratorKey.next();

                java.util.Set<String> set = subscribeLst.get(topic);

                for (Iterator<String> iterator = set.iterator(); iterator.hasNext(); ) {
                    String client = iterator.next();
                    if (TextUtils.equals(name, client)) {
                        android.util.Log.d(TAG, "remove have subscribe topic " + topic + " client " + client);
                        iterator.remove();
                    }
                }

                if (set.isEmpty()) {
                    android.util.Log.d(TAG, "remove topic from subscribe list " + topic);
                    subscribeLst.remove(topic);
                }

            }

            if (mListener != null) {
                mListener.onExit(innerLst.get(name));
            }

            try {
                innerLst.get(name).close();
                innerLst.remove(name);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    @Override
    public boolean handleMessage(Message msg) {

        if (msg.what == WHAT_ADD_TASK) {
            try {
                Task task = (Task) msg.obj;
                Multipart multipart = task.getMultipart();
                Socket socket = task.getSocket();
                if (multipart != null) {
                    switch (IBus.Type.findTypeByInteger(multipart.getType())) {
                        case join:
                            join(multipart.getName(), socket);
                            break;
                        case subscribe:
                            subscribe(multipart);
                            break;
                        case broadcast:
                            broadcast(multipart);
                            break;
                        case unsubscribe:
                            unsubscribe(multipart);
                            break;
                        case exit:
                            exit(multipart);
                            break;
                        default:
                            android.util.Log.e(TAG, "not support operate type code : " + multipart.getType());
                            throw new IllegalStateException("unknown bus server type");
                    }
                } else {
                    android.util.Log.e(TAG, "remote client may broken or invalid multipart ...");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }


    /**
     * add task
     *
     * @param task {@link Task}
     */
    public void addTask(Task task) {
        Message.obtain(dealHandler, TaskHandler.WHAT_ADD_TASK, task).sendToTarget();
    }


    static class Task {

        private Multipart multipart;

        private Socket socket;

        public Multipart getMultipart() {
            return multipart;
        }

        public Socket getSocket() {
            return socket;
        }

        private Task(Multipart multipart, Socket socket) {
            this.multipart = multipart;
            this.socket = socket;
        }

        private Task(Builder builder) {
            this(builder.multipart, builder.socket);
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        static class Builder {

            private Multipart multipart;

            private Socket socket;

            public Builder setMultipart(Multipart multipart) {
                this.multipart = multipart;
                return this;
            }

            public Builder setSocket(Socket socket) {
                this.socket = socket;
                return this;
            }

            public Task build() {
                return new Task(this);
            }
        }
    }

    interface Observer {

        /**
         * 节点退出报文
         *
         * @param socket {@link Socket}
         */
        void onExit(Socket socket);

    }
}
