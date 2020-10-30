package com.hehr.lib;

import com.google.protobuf.ByteString;
import com.hehr.lib.netty.IBus;
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
public class BusClient implements IClient {

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "Bootstrap");
        }
    });

    /**
     * client name
     *
     * @param name String
     * @return {@link BusClient}
     */
    public BusClient create(final String name) {

        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                client = new InnerClient(name);
            }
        });

        return this;
    }

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

    private InnerClient client;

    /**
     * 主机地址
     */
    private String ip = IBus.DEFAULT_IP;
    /**
     * 端口号
     */
    private int port = IBus.DEFAULT_PORT;
    /**
     * 回调
     */
    private Observer observer;


    @Override
    public void subscribe(String... topics) throws IllegalConnectionStateException {
        if (client.isActive()) {
            client.subscribe(topics);
        } else {
            throw new IllegalConnectionStateException();
        }
    }

    @Override
    public void unsubscribe(String... topics) throws IllegalConnectionStateException {
        if (client.isActive()) {
            client.unsubscribe(topics);
        } else {
            throw new IllegalConnectionStateException();
        }
    }

    @Override
    public void publish(String topic, Extra data) throws IllegalConnectionStateException {
        if (client.isActive()) {
            client.publish(topic, transform(data));
        } else {
            throw new IllegalConnectionStateException();
        }
    }

    @Override
    public void publish(String topic) throws IllegalConnectionStateException {
        if (client.isActive()) {
            client.publish(topic);
        } else {
            throw new IllegalConnectionStateException();
        }
    }


    private class InnerClient extends NettyClient {

        public InnerClient(String name) {
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
                .setBinary(ByteString.copyFrom(extra.binary))
                .setBool(extra.bool)
                .setCharacter(extra.character)
                .setDigital(extra.digital)
                .build();
    }

}
