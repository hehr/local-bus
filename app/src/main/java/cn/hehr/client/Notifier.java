package cn.hehr.client;

import android.os.Bundle;
import android.util.Log;

import com.hehr.lib.BusClient;


public class Notifier extends BusClient {

    private static final String TAG = "NotifyNode";

    @Override
    public void onReceived(String topic, Bundle data) {
        Log.d(TAG, "received " + topic + " , data : " + data.getByteArray("pcm"));
    }

    @Override
    public String join() {
        return "notify";
    }

    @Override
    public void onConnected() {
        subscribe("recorder.pcm");
    }

    @Override
    public void onDisconnect() {
        unsubscribe("recorder.pcm");
    }
}
