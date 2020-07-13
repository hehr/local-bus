package cn.hehr;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import cn.hehr.service.NodesService;

public class App extends Application {

    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();

//        appContext().bindService(
//                new Intent(getApplicationContext(), NodesService.class)
//                , conn, BIND_AUTO_CREATE);


    }

    public Context appContext() {
        return this.getApplicationContext();
    }

    private NodesService mService;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {

            NodesService.NodesServiceBinder serviceBinder = (NodesService.NodesServiceBinder) binder;

            mService = serviceBinder.getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


}
