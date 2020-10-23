package cn.hehr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.hehr.lib.netty.NettyServer;

import cn.hehr.binder.BinderPool;
import cn.hehr.binder.IBinderPoolImpl;
import cn.hehr.client.Notifier;
import cn.hehr.service.NodesService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyAudioPermissions(this);

//        //1 初始化 socket bus
        try {
            initBus();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        2.初始化binderPool
//        initPool();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notifier.close();
        notifier = null;

    }

    //申请录音权限
    private static final int GET_RECODE_AUDIO = 1;
    private static String[] PERMISSION_AUDIO = {
            Manifest.permission.RECORD_AUDIO
    };

    /*
     * 申请录音权限*/
    public void verifyAudioPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSION_AUDIO,
                    GET_RECODE_AUDIO);
        }
    }


    /**
     * 使用binder pool 方式通信
     */
    private void initPool() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (binderRecorder == null) {
                    binderRecorder = IRecorder.Stub.asInterface(
                            BinderPool.getInstance(MainActivity.this)
                                    .queryBinder(IBinderPoolImpl.CODE_RECORDER));
                }
            }
        }).start();

    }


    private Notifier notifier;

    /**
     * 使用 socket bus 方式进行通信
     */
    private void initBus() throws InterruptedException {


        new Thread(new Runnable() {
            @Override
            public void run() {
                //1.bind server
                new NettyServer().bind();

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    notifier = new Notifier();//start local client
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        Intent i = new Intent(getApplicationContext(), NodesService.class);
        startService(i);//start remote server

    }

    /**
     * start button click
     *
     * @param v {@link View}
     */
    public void clickStart(View v) {

        Log.d(TAG, "clickStart called ...");

        //1. start by binder
//        startByBinder();

        //2. start by socket bus
        startBySocketBus();
    }

    /**
     * stop button click
     *
     * @param v {@link View}
     */
    public void clickStop(View v) {

        Log.d(TAG, "clickStop called ...");

        //1. stop by binder
//        stopByBinder();

        //2.stop by socket bus
        stopBySocketBus();

    }


    private IRecorder binderRecorder;

    private void startByBinder() {

        try {
            binderRecorder.start(ICallback.Stub.asInterface(new CallbackImpl()));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void stopByBinder() {
        try {
            binderRecorder.stop();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void startBySocketBus() {
        if (notifier != null) {
            notifier.publish("recorder.start", null);
        }
    }

    private void stopBySocketBus() {
        if (notifier != null) {
            notifier.publish("recorder.stop", null);
        }
    }

    /**
     * callback impl
     */
    private class CallbackImpl extends ICallback.Stub {

        @Override
        public void onData(byte[] data) throws RemoteException {
            Log.e(TAG, "data size : " + data.length);
        }
    }

}