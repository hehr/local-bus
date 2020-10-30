package com.hehr.lib;

import com.hehr.lib.netty.IBus;
import com.hehr.lib.netty.IServer;
import com.hehr.lib.netty.NettyServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * server
 *
 * @author hehr
 */
public class BusServer {

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "Bootstrap");
        }
    });

    /**
     * 绑定接口，开始监听
     *
     * @return {@link BusServer}
     */
    public void bind() {

        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                server.bind(port, new IServer.Observer() {
                    @Override
                    public void bindDone() {
                        if (observer != null) {
                            observer.done();
                        }
                    }
                });
            }
        });
    }

    /**
     * 关闭服务，停止监听
     *
     * @return {@link BusServer}
     */
    public BusServer close() {
        return this;
    }

    /**
     * 指定端口号
     *
     * @param port 端口号
     * @return {@link BusClient}
     */
    public BusServer option(int port) {
        this.port = port;
        return this;
    }

    /**
     * 注册回掉
     *
     * @param observer {@link BusClient.Observer}
     * @return {@link BusClient}
     */
    public BusServer option(Observer observer) {
        this.observer = observer;
        return this;
    }

    private int port = IBus.DEFAULT_PORT;

    private Observer observer;

    private NettyServer server = new NettyServer();

    /**
     * 绑定监听
     */
    public interface Observer {
        /**
         * 绑定操作完成
         */
        void done();
    }

}
