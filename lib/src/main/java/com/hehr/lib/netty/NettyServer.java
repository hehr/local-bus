package com.hehr.lib.netty;


import com.hehr.lib.IServer;

import java.util.concurrent.ThreadFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * netty server impl
 */
public class NettyServer implements IServer {

    private EventLoopGroup bossGroup = new NioEventLoopGroup(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "bus-looper");
        }
    });//单线程模式


    public void bind() {
        try {
            new ServerBootstrap()
                    .group(bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addFirst(new DelimiterBasedFrameDecoder(1024 * 1024, Unpooled.copiedBuffer((DELIMITER).getBytes())))
                                    .addLast(EventDispatcher.getInstance());
                        }
                    })
                    .bind(DEFAULT_PORT).sync()
                    //开始阻塞
                    .channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
        }

    }

    @Override
    public void close() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
    }

}
