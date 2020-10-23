package cn.hehr.client;

import android.util.Log;

import com.hehr.lib.protocol.multipart.Extra;
import com.hehr.lib.netty.NettyClient;


/**
 * @author hehr
 */
public class Notifier extends NettyClient {

    private static final String TAG = "NotifyNode";


    @Override
    public void onReceived(String topic, Extra extra) {

        Log.d(TAG, "received " + topic + " , data : " + extra.getBinary().length);
    }

    @Override
    public String join() {
        return "notify";
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
