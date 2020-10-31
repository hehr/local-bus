package cn.hehr.client;


import android.util.Log;

import com.hehr.lib.BusClient;
import com.hehr.lib.Extra;
import com.hehr.lib.IllegalConnectionStateException;

public class Remoter {

    private final String TAG = "RemoteNode";

    private BusClient mBusClient;

    public Remoter() {
        mBusClient = new BusClient()
                .option(new BusClient.Observer() {
                    @Override
                    public void onConnect() {
                        try {
                            mBusClient.subscribe("recorder.pcm");
                        } catch (IllegalConnectionStateException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onExit() {

                    }

                    @Override
                    public void onReceived(String topic, Extra extra) {
                        Log.d(TAG, "received " + topic + " , extra size " + extra.getBinary().length);
                    }
                });

        mBusClient.create("remote");
    }


}
