package cn.hehr.binder;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import cn.hehr.IBinderPool;

public class IBinderPoolImpl extends IBinderPool.Stub {

    /**
     * binder code of recorder
     */
    public static final int CODE_RECORDER = 1;

    private static final String TAG = "IBinderPoolImpl";

    @Override
    public IBinder queryBinder(int code) throws RemoteException {

        switch (code) {
            case CODE_RECORDER:
                Log.e(TAG, "new IRecorderImpl");
                return new IRecorderImpl();
            default:
                return null;
        }
    }

}
