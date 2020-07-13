package cn.hehr.binder;

import android.os.IBinder;

interface IPool {

    /**
     * 获取binder 实例
     *
     * @param code int
     * @return {@link IBinder}
     */
    IBinder queryBinder(int code);
}
