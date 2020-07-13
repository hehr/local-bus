package com.hehr.lib.socket;

import android.os.Bundle;

/**
 * rpc 接口
 *
 * @author hehr
 */
public interface IRpc {

    /**
     * 支持单参数构造函数
     *
     * @param bundle 远程调用参数,不要在此函数内执行过度耗时操作
     * @return {@link Object}
     */
    Bundle invoke(Bundle bundle);

}
