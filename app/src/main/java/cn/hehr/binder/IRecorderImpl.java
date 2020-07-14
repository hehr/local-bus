package cn.hehr.binder;


import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.hehr.ICallback;
import cn.hehr.IRecorder;
import cn.hehr.recorder.Recorder;
import cn.hehr.recorder.RecorderListener;

public class IRecorderImpl extends IRecorder.Stub {

    private static final String TAG = "IRecorderImpl";

    private Recorder mRecorder = new Recorder();

    public IRecorderImpl() {
        mRecorder.create(1);
    }

    boolean isRunning = false;

    private List<ICallback> lstOfCallback = new ArrayList<>();

    @Override
    public void start(ICallback callback) {
        Log.e(TAG, "start called .");
        if (!isRunning) {
            isRunning = true;
            lstOfCallback.add(callback);
            mRecorder.start(new RecorderListenerImpl());
        }
    }

    @Override
    public void stop() {
        if (isRunning) {
            mRecorder.stop();
            lstOfCallback.clear();
            isRunning = false;
        }
    }

    private class RecorderListenerImpl implements RecorderListener {

        @Override
        public void onRecordStarted() {
            Log.e(TAG, "onRecordStarted : ");
        }

        @Override
        public void onDataReceived(byte[] buffer, int size) {
            Log.e(TAG, "onDataReceived : " + size);
            try {
                for (ICallback c : lstOfCallback) {
                    if (c != null) {
                        c.onData(buffer);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onRecordStopped() {
            Log.e(TAG, "onRecordStopped : ");
        }

        @Override
        public void onRecordReleased() {

        }

        @Override
        public void onException(String e) {

        }
    }
}
