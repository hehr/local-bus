package cn.hehr.client;

import android.util.Log;

import com.hehr.lib.client.BusClient;
import com.hehr.lib.Extra;
import com.hehr.lib.IllegalConnectionStateException;


/**
 * @author hehr
 */
public class Notifier {

    private static final String TAG = "NotifyNode";

    private BusClient mClient;

    public Notifier() {

        mClient = new BusClient()
                .option(new BusClient.Observer() {
                    @Override
                    public void onConnect() {
                        try {
                            mClient.subscribe("recorder.pcm");
                        } catch (IllegalConnectionStateException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onExit() {
                        try {
                            mClient.unsubscribe("recorder.pcm");
                        } catch (IllegalConnectionStateException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onReceived(String topic, Extra extra) {
                        Log.d(TAG, "received " + topic);
                    }
                });

        mClient.create("notify");

    }


    public void publish(String topic) {
        Log.d(TAG, "publish topic " + topic);
        try {
            mClient.publish(topic);
        } catch (IllegalConnectionStateException e) {
            e.printStackTrace();
        }
    }

}
