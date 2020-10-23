package com.hehr.lib.netty;


import android.util.Log;

import com.hehr.lib.IClient;
import com.hehr.lib.protocol.MultipartDecoder;
import com.hehr.lib.protocol.MultipartEncoder;
import com.hehr.lib.protocol.multipart.Extra;
import com.hehr.lib.protocol.multipart.Multipart;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
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
import io.netty.handler.codec.DelimiterBasedFrameDecoder;


public abstract class NettyClient implements IClient, Runnable {

    public NettyClient() {
        new Thread(this, "client-" + join()).start();
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }

    @Override
    public void subscribe(String... topics) {

        for (String t : topics) {
            write(Multipart.newBuilder()
                    .setName(join())
                    .setType(Type.subscribe.value)
                    .setTopic(t)
                    .build());
        }

    }

    @Override
    public void unsubscribe(String... topics) {
        for (String t : topics) {
            write(Multipart.newBuilder()
                    .setName(join())
                    .setType(Type.unsubscribe.value)
                    .setTopic(t)
                    .build());
        }
    }


    @Override
    public void publish(String topic, Extra data) {
        write(Multipart.newBuilder()
                .setName(join())
                .setType(Type.broadcast.value)
                .setTopic(topic)
                .setExtra(data)
                .build());
    }

    @Override
    public void publish(String topic) {
        write(Multipart.newBuilder()
                .setName(join())
                .setType(Type.broadcast.value)
                .setTopic(topic)
                .build());
    }

    private void write(Multipart multipart) {
        if (channel != null && channel.isOpen()) {
            channel.writeAndFlush(multipart);
        } else {
            Log.e(join(), "drop multipart " + multipart.toString());
        }
    }


    //与服务端的连接通道
    private SocketChannel channel;


    private Bootstrap bootstrap;


    @Override
    public void run() {

        try {

            bootstrap = new Bootstrap()

                    .channel(NioSocketChannel.class)//NIO
                    .group(new NioEventLoopGroup(1, new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable runnable) {
                            return new Thread(runnable, "client-" + join());
                        }
                    }))
                    .option(ChannelOption.SO_KEEPALIVE, true)//长链接
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addFirst(new DelimiterBasedFrameDecoder(1024 * 1024, Unpooled.copiedBuffer((DELIMITER).getBytes())))
                                    .addLast(new MultipartDecoder())
                                    .addLast(new MultipartEncoder())
                                    .addLast(new InnerNettyHandlerImpl());
                        }
                    });

            connect(bootstrap);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void connect() {

        if (bootstrap != null) {
            try {
                connect(this.bootstrap);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException(" connect failed ,illegal state .");
        }

    }

    private void connect(Bootstrap bootstrap) throws InterruptedException {

        if (bootstrap != null) {
            bootstrap.connect(new InetSocketAddress(IP, DEFAULT_PORT))
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                Log.d(join(), " connect success .");
                                channel = (SocketChannel) future.channel();
                            } else {
                                EventLoop loop = future.channel().eventLoop();
                                loop.schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        connect();
                                    }
                                }, 500, TimeUnit.MILLISECONDS); //链接失败后500ms重连
                            }
                        }
                    }).sync();

        }

    }


    private class InnerNettyHandlerImpl extends SimpleChannelInboundHandler<Multipart> {


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

            Log.e(join(), "Caught exception , Throwable " + cause.getMessage());

            ctx.close();

        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Multipart multipart) {
            if (multipart != null) {
                Log.d(join(), "received  multipart : " + multipart.getTopic());
                onReceived(multipart.getTopic(), multipart.getExtra());
            }
        }


        //connect
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {

            //首帧报文
            write(Multipart.newBuilder()
                    .setName(join())
                    .setType(Type.join.value)
                    .setTopic("client.join")
                    .build());

            onCrete();

        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            Log.d(join(), "与服务端断开连接：" + ctx.toString());
            onExit();
        }

    }


    public abstract void onCrete();


    public abstract void onExit();

}
