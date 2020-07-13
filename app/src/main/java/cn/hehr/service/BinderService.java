package cn.hehr.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import cn.hehr.binder.IBinderPoolImpl;

public class BinderService extends Service {

    public BinderService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IBinderPoolImpl();
    }

}
