package com.hehr.lib;

import com.hehr.lib.netty.NettyServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * server
 *
 * @author hehr
 */
public class BusServer implements IServer, IBus {

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "Bootstrap");
        }
    });


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
     * @param observer {@link IServer.Observer}
     * @return {@link BusClient}
     */
    public BusServer option(Observer observer) {
        this.observer = observer;
        return this;
    }

    private int port = DEFAULT_PORT;

    private Observer observer;

    private NettyServer innerNettyServer = new NettyServer();

    @Override
    public void create() {

        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                innerNettyServer.bind(port, new NettyServer.Observer() {
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

    @Override
    public void destroy() {
        if (innerNettyServer != null) {
            innerNettyServer.close();
            innerNettyServer = null;
        }
    }

}
