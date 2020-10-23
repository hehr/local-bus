package cn.hehr.client;

import android.util.Log;

import com.hehr.lib.protocol.multipart.Extra;
import com.hehr.lib.netty.NettyClient;


public class Remoter extends NettyClient {

    private final String TAG = "RemoteNode";


    @Override
    public void onReceived(String topic, Extra extra) {
        Log.d(TAG, "received " + topic + " , data : " + extra.getBinary().length);
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

    }
}
