package cn.hehr.recorder;

public interface IRecorder {
    /**
     * 创建录音机
     *
     * @param audioSource
     * @param sampleRate
     * @param channel
     * @param format
     * @param bufferSizeInBytes
     */
    void create(int audioSource, int sampleRate, int channel, int format, int bufferSizeInBytes);

    /**
     * 创建录音机
     * @param type 1 双麦 0 四麦
     */
    void create(int type);

    /**
     * 启动录音机
     */
    void start(RecorderListener listener);

    /**
     * 停止
     */
    void stop();

    /**
     * 销毁录音机
     */
    void release();

}
