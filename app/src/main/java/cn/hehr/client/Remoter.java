package cn.hehr.client;

import android.os.Bundle;
import android.util.Log;

import com.hehr.lib.IRpc;
import com.hehr.lib.BusClient;


public class Remoter extends BusClient {

    private final String TAG = "RemoteNode";

    @Override
    public void onConnected() {

        Log.e(TAG, "RemoteNode connected");

        subscribe("recorder.pcm");

        registered("remoter.increase", new IRpc() {
            @Override
            public Bundle invoke(Bundle bundle) {
                Log.e(TAG, "invoke has called ... ");
                bundle.putInt("ret", bundle.getInt("int") + 1);
                return bundle;
            }
        });
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onReceived(String topic, Bundle data) {
        Log.d(TAG, "received " + topic + " , data : " + data.getByteArray("pcm"));
    }

    @Override
    public String join() {
        return "remote";
    }
}
