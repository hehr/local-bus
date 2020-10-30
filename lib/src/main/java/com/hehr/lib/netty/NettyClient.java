package com.hehr.lib.netty;


import android.util.Log;

import com.hehr.lib.proto.RespProto.Resp;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


public abstract class NettyClient implements IClient, Runnable {


    private String ip;

    private int port;

    private String name;

    public NettyClient(String ip, int port, String name) {

        this.ip = ip;

        this.port = port;

        this.name = name;

        new Thread(this, "client-" + name).start();
    }


    @Override
    public void close() {
        write(Resp.newBuilder()
                .setName(name)
                .setType(Type.exit.value)
                .setTopic("client.exit")
                .build());
        if (channel != null) {
            channel.close();
            channel = null;
        }
        onExit();
    }

    @Override
    public void subscribe(String... topics) {

        for (String t : topics) {
            write(Resp.newBuilder()
                    .setName(name)
                    .setType(Type.subscribe.value)
                    .setTopic(t)
                    .build());
        }

    }

    @Override
    public void unsubscribe(String... topics) {
        for (String t : topics) {
            write(Resp.newBuilder()
                    .setName(name)
                    .setType(Type.unsubscribe.value)
                    .setTopic(t)
                    .build());
        }
    }


    @Override
    public void publish(String topic, Resp.Extra data) {
        write(Resp.newBuilder()
                .setName(name)
                .setType(Type.broadcast.value)
                .setTopic(topic)
                .setExtra(data)
                .build());
    }

    @Override
    public void publish(String topic) {
        write(Resp.newBuilder()
                .setName(name)
                .setType(Type.broadcast.value)
                .setTopic(topic)
                .build());
    }

    /**
     * 写报文
     *
     * @param resp {@link Resp}
     */
    private void write(Resp resp) {
        if (isActive())
            channel.writeAndFlush(resp);
        else
            Log.e(name, "drop resp " + resp.toString());

    }

    public boolean isActive() {
        return channel != null && channel.isOpen();
    }


    private SocketChannel channel;

    private Bootstrap bootstrap;


    @Override
    public void run() {

        NioEventLoopGroup group = new NioEventLoopGroup(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, "client-" + name);
            }
        });

        try {

            bootstrap = new Bootstrap()
                    .channel(NioSocketChannel.class)//NIO
                    .group(group)
                    .option(ChannelOption.SO_KEEPALIVE, true)//长链接
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new ProtobufVarint32FrameDecoder())
                                    .addLast(new ProtobufDecoder(Resp.getDefaultInstance()))
                                    .addLast(new ProtobufVarint32LengthFieldPrepender())
                                    .addLast(new ProtobufEncoder())
                                    .addLast(new InnerNettyHandlerImpl());
                        }
                    });

            connect(this.ip, this.port);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * connect
     *
     * @param ip
     * @param port
     * @throws InterruptedException
     */
    private void connect(final String ip, final int port) throws InterruptedException {

        if (bootstrap != null) {
            bootstrap.connect(new InetSocketAddress(ip, port))
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                Log.d(name, " connect success .");
                                channel = (SocketChannel) future.channel();
                            } else {
                                EventLoop loop = future.channel().eventLoop();
                                loop.schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            connect(ip, port);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 100, TimeUnit.MILLISECONDS); //链接失败后100ms重连
                            }
                        }
                    }).sync();
        }

    }


    private class InnerNettyHandlerImpl extends SimpleChannelInboundHandler<Resp> {


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            Log.e(name, "Caught exception , Throwable " + cause.getMessage());
            ctx.close();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Resp resp) {
            if (resp != null) {
                Log.d(name, "received  multipart : " + resp.getTopic());
                onReceived(resp.getTopic(), resp.getExtra());
            }
        }


        //connect
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {

            //首帧报文
            write(Resp.newBuilder()
                    .setName(name)
                    .setType(Type.join.value)
                    .setTopic("client.join")
                    .build());

            onCrete();

        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {

            Log.e(name, "链接已断开：" + ctx.toString());

            onExit();

        }

    }


    public abstract void onCrete();


    public abstract void onExit();

}
