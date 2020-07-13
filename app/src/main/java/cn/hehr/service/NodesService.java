package cn.hehr.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import cn.hehr.client.Remoter;
import cn.hehr.client.XRecorder;


/**
 * 多节点服务
 */
public class NodesService extends Service {

    private String TAG = "NodesService";


    @Override
    public void onCreate() {
        super.onCreate();
    }

    private XRecorder recorder;

    private Remoter remoter;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (recorder == null) {
            recorder = new XRecorder();
        }
        if (remoter == null) {
            remoter = new Remoter();
        }

        return super.onStartCommand(intent, flags, startId);


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    private NodesServiceBinder binder = new NodesServiceBinder();

    /**
     * connect binder
     */
    public class NodesServiceBinder extends Binder {
        /**
         * 获取当前service 实例
         *
         * @return {@link NodesService }
         */
        public NodesService getService() {
            return NodesService.this;
        }

    }

}
