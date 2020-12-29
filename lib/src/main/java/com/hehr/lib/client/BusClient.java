package com.hehr.lib.client;

import com.google.protobuf.ByteString;
import com.hehr.lib.Extra;
import com.hehr.lib.IBus;
import com.hehr.lib.IllegalConnectionStateException;
import com.hehr.lib.netty.NettyClient;
import com.hehr.lib.proto.RespProto;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * client
 *
 * @author hehr
 */
public class BusClient implements IClient, IBus {

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "Bootstrap");
        }
    });

    /**
     * 指定端主机地址
     *
     * @param ip 主机地址，默认"127.0.0.1"
     * @return {@link BusClient}
     */
    public BusClient option(String ip) {
        this.ip = ip;
        return this;
    }

    /**
     * 指定端口号
     *
     * @param port 端口号
     * @return {@link BusClient}
     */
    public BusClient option(int port) {
        this.port = port;
        return this;
    }

    /**
     * 注册回掉
     *
     * @param observer {@link Observer}
     * @return {@link BusClient}
     */
    public BusClient option(Observer observer) {
        this.observer = observer;
        return this;
    }

    private InnerHolder holder;

    /**
     * 主机地址
     */
    private String ip = DEFAULT_IP;
    /**
     * 端口号
     */
    private int port = DEFAULT_PORT;
    /**
     * 回调
     */
    private Observer observer;


    @Override
    public void subscribe(String... topics) throws IllegalConnectionStateException {
        holder.subscribe(topics);
    }

    @Override
    public void unsubscribe(String... topics) throws IllegalConnectionStateException {
        holder.unsubscribe(topics);
    }

    @Override
    public void publish(String topic, Extra data) throws IllegalConnectionStateException {
        holder.publish(topic, transform(data));
    }

    @Override
    public void publish(String topic) throws IllegalConnectionStateException {
        holder.publish(topic);

    }

    @Override
    public void destroy() throws IllegalConnectionStateException {

        if (holder.isActive()) {
            holder.close();
            holder = null;
        }

        if (observer != null) {
            observer = null;
        }
    }

    @Override
    public void create(final String name) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                holder = new InnerHolder(name);
            }
        });
    }


    private class InnerHolder extends NettyClient {

        public InnerHolder(String name) {
            super(ip, port, name);
        }

        @Override
        public void onCrete() {
            if (observer != null) {
                observer.onConnect();
            }
        }

        @Override
        public void onExit() {
            if (observer != null) {
                observer.onExit();
            }
        }

        @Override
        public void onReceived(String topic, RespProto.Resp.Extra data) {
            if (observer != null) {
                observer.onReceived(topic, transform(data));
            }
        }

    }

    public interface Observer {

        /**
         * 链接创建成功
         */
        void onConnect();

        /**
         * 链接断开
         */
        void onExit();

        /**
         * 接受到报文
         *
         * @param topic 消息头
         * @param extra 消息报文
         */
        void onReceived(String topic, Extra extra);

    }


    /**
     * transform
     *
     * @param extra {@link RespProto.Resp.Extra}
     * @return {@link Extra}
     */
    private Extra transform(RespProto.Resp.Extra extra) {
        return Extra.newBuilder()
                .setBinary(extra.getBinary().toByteArray())
                .setBool(extra.getBool())
                .setCharacter(extra.getCharacter())
                .setDigital(extra.getDigital())
                .build();
    }

    /**
     * transform
     *
     * @param extra {@link Extra}
     * @return {@link RespProto.Resp.Extra}
     */
    private RespProto.Resp.Extra transform(Extra extra) {
        return RespProto.Resp.Extra.newBuilder()
                .setBinary(ByteString.copyFrom(extra.getBinary()))
                .setBool(extra.isBool())
                .setCharacter(extra.getCharacter())
                .setDigital(extra.getDigital())
                .build();
    }

}
