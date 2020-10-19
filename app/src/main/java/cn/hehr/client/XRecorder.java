package cn.hehr.client;

import android.util.Log;

import com.hehr.lib.BusClient;
import com.hehr.lib.multipart.Extra;

import cn.hehr.recorder.Recorder;
import cn.hehr.recorder.RecorderListener;

public class XRecorder extends BusClient implements RecorderListener {

    private final String TAG = "RecorderNode";

    private Recorder mRecorder = new Recorder();

    @Override
    public void onReceived(String topic, Extra data) {
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
    public void onCrete() {
        Log.e(TAG, "RecorderNode connected");
        mRecorder.create(1);
        subscribe("recorder.start", "recorder.stop");
    }

    @Override
    public void onExit() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    @Override
    public void onRecordStarted() {

    }

    @Override
    public void onDataReceived(byte[] buffer, int size) {
        publish("recorder.pcm", Extra.newBuilder()
                .setBinary(buffer)
                .build());
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
