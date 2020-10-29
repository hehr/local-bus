package cn.hehr.client;

import android.util.Log;

import com.hehr.lib.netty.NettyClient;
import com.hehr.lib.proto.RespProto;


public class Remoter extends NettyClient {

    private final String TAG = "RemoteNode";

    @Override
    public void onReceived(String topic, RespProto.Resp.Extra extra) {
        Log.d(TAG, "received " + topic);
    }

    @Override
    public String join() {
        return "remote";
    }

    @Override
    public void onCrete() {
        subscribe("recorder.pcm");
    }

    @Override
    public void onExit() {
        unsubscribe("recorder.pcm");
    }
}
