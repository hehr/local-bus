package cn.hehr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.hehr.lib.server.BusServer;

import cn.hehr.client.Notifier;
import cn.hehr.service.NodesService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyAudioPermissions(this);

        initBus();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    private Notifier notifier;

    /**
     * 使用 socket bus 方式进行通信
     */
    private void initBus() {

        new BusServer()
                .option(new BusServer.Observer() {
                    @Override
                    public void done() {

                        Intent i = new Intent(getApplicationContext(), NodesService.class);
                        startService(i);//start remote server

                    }
                }).create();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //sleep 500ms wait for server ready
                notifier = new Notifier();//start local client

            }
        }).start();



    }

    /**
     * start button click
     *
     * @param v {@link View}
     */
    public void clickStart(View v) {

        Log.d(TAG, "clickStart called ...");

        startBySocketBus();
    }

    /**
     * stop button click
     *
     * @param v {@link View}
     */
    public void clickStop(View v) {

        Log.d(TAG, "clickStop called ...");

        stopBySocketBus();

    }


    private void startBySocketBus() {
        if (notifier != null) {
            notifier.publish("recorder.start");
        }
    }

    private void stopBySocketBus() {
        if (notifier != null) {
            notifier.publish("recorder.stop");
        }
    }

}