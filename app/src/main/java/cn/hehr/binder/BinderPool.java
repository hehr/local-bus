package cn.hehr.binder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.CountDownLatch;

import cn.hehr.IBinderPool;
import cn.hehr.service.BinderService;

/**
 * bind pool
 */
public class BinderPool implements IPool {

    private Context mContext;

    private BinderPool(Context context) {
        mContext = context;
        connectBinderPoolService();
    }

    private IBinderPool mIBinderPool;

    private static BinderPool instance = null;

    public static BinderPool getInstance(Context context) {
        if (instance == null) {
            synchronized (BinderPool.class) {
                if (instance == null)
                    instance = new BinderPool(context);
            }
        }
        return instance;
    }

    private CountDownLatch mCountDownLatch;

    private static final String TAG = "BinderPool";

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.e(TAG, "onServiceConnected called ...");

            mIBinderPool = IBinderPool.Stub.asInterface(service);
            try {
                mIBinderPool.asBinder().linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            // 计数减一
            mCountDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            mIBinderPool.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mIBinderPool = null;
            connectBinderPoolService();
        }
    };

    private void connectBinderPoolService() {
        //实例化一个倒计数器，count指定计数个数
        mCountDownLatch = new CountDownLatch(1);
        Intent intent = new Intent(mContext, BinderService.class);
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        try {
            //等待，当计数减到0时，所有线程并行执行
            mCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public IBinder queryBinder(int binderCode) {

        IBinder iBinder = null;

        if (mIBinderPool != null) {
            try {
                iBinder = mIBinderPool.queryBinder(binderCode);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        Log.e(TAG, "queryBinder called , binderCode : " + binderCode + " ,iBinder name : " + iBinder.getClass().getSimpleName());

        return iBinder;
    }

}
