package cn.hehr.client;

import android.os.Bundle;
import android.util.Log;


import com.hehr.lib.IRpc;
import com.hehr.lib.BusClient;

import cn.hehr.recorder.Recorder;
import cn.hehr.recorder.RecorderListener;

public class XRecorder extends BusClient implements RecorderListener {

    private final String TAG = "RecorderNode";

    private Recorder mRecorder = new Recorder();

    @Override
    public void onReceived(String topic, Bundle data) {
        switch (topic) {
            case "recorder.start":

                if (mRecorder != null) {
                    mRecorder.start(this);
                }

                break;
            case "recorder.stop":

                if (mRecorder != null) {
                    mRecorder.stop();
                }

                break;
            default:
                break;
        }
    }

    @Override
    public String join() {
        return "recorder";
    }

    @Override
    public void onConnected() {
        Log.e(TAG, "RecorderNode connected");
        mRecorder.create(1);
        subscribe("recorder.start", "recorder.stop");
        registered("recorder.increase", new IRpc() {
            @Override
            public Bundle invoke(Bundle bundle) {
                bundle.putInt("ret", 100 + bundle.getInt("int"));
                try {
                    Thread.sleep(1000 * 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return bundle;
            }
        });

    }

    @Override
    public void onDisconnect() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    @Override
    public void onRecordStarted() {

    }

    @Override
    public void onDataReceived(byte[] buffer, int size) {
        Bundle bundle = new Bundle();
        bundle.putByteArray("pcm", buffer);
        publish("recorder.pcm", bundle);
    }

    @Override
    public void onRecordStopped() {

    }

    @Override
    public void onRecordReleased() {

    }

    @Override
    public void onException(String e) {

    }
}
