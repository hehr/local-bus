package cn.hehr.client;

import android.util.Log;

import com.hehr.lib.BusClient;
import com.hehr.lib.Extra;
import com.hehr.lib.IllegalConnectionStateException;

import cn.hehr.recorder.Recorder;
import cn.hehr.recorder.RecorderListener;

public class XRecorder implements RecorderListener {

    private final String TAG = "RecorderNode";

    private Recorder mRecorder = new Recorder();

    private BusClient mBusClient;

    public XRecorder() {
        mBusClient = new BusClient()
                .option(new BusClient.Observer() {
                    @Override
                    public void onConnect() {
                        mRecorder.create(1);
                        try {
                            mBusClient.subscribe("recorder.start", "recorder.stop");
                        } catch (IllegalConnectionStateException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onExit() {
                        mRecorder.stop();
                        mRecorder.release();
                        mRecorder = null;
                        try {
                            mBusClient.unsubscribe("recorder.start", "recorder.stop");
                        } catch (IllegalConnectionStateException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onReceived(String topic, Extra extra) {
                        switch (topic) {
                            case "recorder.start":
                                if (mRecorder != null) {
                                    mRecorder.start(XRecorder.this);
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
                })
                .create("recorder");
    }

    @Override
    public void onRecordStarted() {

    }

    @Override
    public void onDataReceived(byte[] buffer, int size) {

        Log.d(TAG, "send pcm  , buffer size :" + buffer.length);

        try {
            mBusClient.publish("recorder.pcm", Extra.newBuilder()
                    .setBinary(buffer)
                    .setBool(true)
                    .setCharacter("1112333")
                    .build());
        } catch (IllegalConnectionStateException e) {
            e.printStackTrace();
        }

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
