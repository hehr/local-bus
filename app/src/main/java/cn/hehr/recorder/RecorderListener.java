package cn.hehr.recorder;

/**
 * 录音机
 */
public interface RecorderListener {

    /**
     * 注册录音回调方法，已经启动，但是尚未读取录音数据时调用;
     */
    void onRecordStarted();

    /**
     * 经过信号处理引擎后的单路数据
     *
     * @param buffer
     * @param size
     */
    void onDataReceived(byte[] buffer, int size);

    /**
     * 注册录音回调方法，在录音停止后调用
     */
    void onRecordStopped();

    /**
     * 注册录音回调方法，在录音机资源释放后调用
     */
    void onRecordReleased();

    /**
     * 注册录音回调方法，在异常发生时调用
     *
     * @param e Exception
     */
    void onException(String e);

}
