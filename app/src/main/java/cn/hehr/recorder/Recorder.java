package cn.hehr.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author hehr
 * 录音机
 */
public class Recorder implements IRecorder {

    /**
     * Android 标准录音机api
     */
    private volatile AudioRecord mAudioRecorder;
    /**
     *
     */
    private RecorderListener mListener;

    private ExecutorService mThreadPool = Executors.newSingleThreadExecutor();

    private volatile boolean isRecording = false;

    /**
     * 录音机采样间隔
     */
    private int intervalTime = 100;

    private int audioSource;

    private int sampleRate;

    private int channel;

    private int format;

    private int bufferSize;

    private int micType = 1;

    private void setAudioSource(int audioSource) {
        this.audioSource = audioSource;
    }

    private void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    private void setChannel(int channel) {
        this.channel = channel;
    }

    private void setFormat(int format) {
        this.format = format;
    }

    private void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public void create(int audioSource, int sampleRate, int channel, int format, int bufferSizeInBytes) {
        setAudioSource(audioSource);
        setSampleRate(sampleRate);
        setChannel(channel);
        setFormat(format);
        setBufferSize(bufferSizeInBytes);
        mAudioRecorder = new AudioRecord(audioSource, sampleRate, channel, format, bufferSizeInBytes);
    }

    @Override
    public void create(int type) {
        micType = type;
        if(type == 1){
                    create(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                16000,
                AudioFormat.CHANNEL_IN_STEREO, // 立体声,2通道数据
                AudioFormat.ENCODING_PCM_16BIT,
                AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT));
        }else{
            //4mic
            create(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    32000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    192000);
        }
    }

    private Lock mLock = new ReentrantLock();

    @Override
    public void start(RecorderListener listener) {

        mLock.lock();

        try {
            isRecording = true;
            mListener = listener;
            if (mListener != null) {
                mListener.onRecordStarted();
            }
            mThreadPool.execute(new ReadRunnable());
            if (mAudioRecorder != null) {
                mAudioRecorder.startRecording();
            }
        } finally {
            mLock.unlock();
        }

    }

    @Override
    public void stop() {

        mLock.lock();

        try {
            if (mListener != null) {
                mListener.onRecordStopped();
            }
            isRecording = false;
            if (mAudioRecorder != null) {
                mAudioRecorder.stop();
            }
        } finally {
            mLock.unlock();
        }

    }

    @Override
    public void release() {
        if (mListener != null) {
            mListener.onRecordReleased();
        }
    }

    private class ReadRunnable implements Runnable {

        @Override
        public void run() {
            if (mAudioRecorder == null) {
                create(micType);
            }

            int size = calculateReadBufferSize(format, sampleRate, intervalTime);

            while (true) {
                if (!isRecording)
                    break;
                byte[] buffer = new byte[size];
                int readSize = mAudioRecorder.read(buffer, 0, size);
                if (readSize > 0) {
                    byte[] bytes = new byte[readSize];
                    System.arraycopy(buffer, 0, bytes, 0, readSize);
                    if (mListener != null) {
                        mListener.onDataReceived(bytes, readSize);
                    }
                }
            }

        }
    }

    /**
     * 计算读取音频buffer大小
     *
     * @return
     */
    private int calculateReadBufferSize(int format, int sampleRate, int intervalTime) {

        int channelNumber = 1;

        switch (format) {
            case AudioFormat.CHANNEL_IN_MONO:
                channelNumber = 1;
                break;
            case AudioFormat.CHANNEL_IN_STEREO:
                channelNumber = 2;
                break;
            default:
                break;
        }

        return sampleRate * channelNumber * format
                * intervalTime / 1000;
    }

}
