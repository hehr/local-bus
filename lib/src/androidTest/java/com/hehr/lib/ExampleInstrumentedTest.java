package com.hehr.lib;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.hehr.lib.bus.localsocket.BusClient;
import com.hehr.lib.bus.Listener;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private static final String TAG = "ExampleInstrumentedTest";

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.hehr.lib.test", appContext.getPackageName());
    }

    AANode clientA;

    BBNode clientB;

    @Test
    public void testBusServerInit() {

//        BusServer server = new BusServer();
//
//        server.listen(new Listener() {
//            @Override
//            public void onError(int code, String msg) {
//                Log.e(TAG, "code : " + code + " , msg " + msg);
//                if (code == 0) {
//                    clientA = new AANode();
//                    clientB = new BBNode();
//                }
//            }
//        });

        new Thread(new Runnable() {
            @Override
            public void run() {
//                if (clientA != null) {
//                    clientA.publish("test", "aaaaaa");
//                }
                Log.d(TAG , "A run.....");
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
//                if (clientB != null) {
//                    clientB.publish("test", "bbbbb");
//                }
                Log.d(TAG , "B run.....");
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        try {
            Thread.sleep(1000 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private class AANode extends BusClient {

        private String TAG = "AANode";

        public AANode() {
            super();
            subscribe("test");
        }

        @Override
        public void onReceived(String topic, byte[] data) {
            Log.e(TAG, "onReceived : " + topic + " , data" + new String(data));
        }

        @Override
        public boolean onRequested(String topic, byte[] data) {
            return false;
        }

        @Override
        public String join() {
            return "a";
        }
    }

    private class BBNode extends BusClient {

        private final String TAG = "BBNode";

        public BBNode() {
            super();
            subscribe("test");
        }

        @Override
        public void onReceived(String topic, byte[] data) {
            Log.e(TAG, "onReceived : " + topic + " , data" + new String(data));
        }

        @Override
        public boolean onRequested(String topic, byte[] data) {
            return false;
        }

        @Override
        public String join() {
            return "b";
        }
    }
}
