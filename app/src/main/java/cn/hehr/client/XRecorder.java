package cn.hehr.client;

import android.util.Log;

import com.google.protobuf.ByteString;
import com.hehr.lib.netty.NettyClient;
import com.hehr.lib.proto.RespProto;

import cn.hehr.recorder.Recorder;
import cn.hehr.recorder.RecorderListener;

public class XRecorder extends NettyClient implements RecorderListener {

    private final String TAG = "RecorderNode";

    private Recorder mRecorder = new Recorder();

    @Override
    public void onReceived(String topic, RespProto.Resp.Extra data) {
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
        unsubscribe("recorder.start", "recorder.stop");
    }

    @Override
    public void onRecordStarted() {

    }

    @Override
    public void onDataReceived(byte[] buffer, int size) {

        Log.d(TAG, "send pcm  , buffer size :" + buffer.length);

        publish("recorder.pcm", RespProto.Resp.Extra.newBuilder()
                .setBinary(ByteString.copyFrom(buffer))
                .setBool(true)
                .setCharacter("1112333")
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
