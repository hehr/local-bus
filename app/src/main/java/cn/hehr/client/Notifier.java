package cn.hehr.client;

import android.util.Log;

import com.hehr.lib.socket.BusClient;


public class Notifier extends BusClient {

    private static final String TAG = "NotifyNode";

    @Override
    public void onReceived(String topic, byte[] data) {
        Log.d(TAG, "received " + topic + " , data : " + data.length);
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
