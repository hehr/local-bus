package com.hehr.lib.socket;

import android.net.LocalSocket;

import com.hehr.lib.multipart.Multipart;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LocalBusSocket
 *
 * @author hehr
 */
public class Socket implements ISocket {

    private java.io.OutputStream mWriter;

    private java.io.InputStream mReader;

    private LocalSocket mSocket;

    private int blockingTime = -1;

    public Socket(LocalSocket socket) {
        this(socket, 5);
    }

    public Socket(LocalSocket socket, int time) {

        mSocket = socket;

        blockingTime = time;

        try {
            mReader = mSocket.getInputStream();
            mWriter = mSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(" socket disconnected ! ");
        }

    }

    private Lock mLock = new ReentrantLock();

    private Condition isAvailable = mLock.newCondition();


    @Override
    public boolean isConnect() {
        if (mSocket == null) {
            throw new IllegalStateException(" set socket first ! ");
        }
        return mSocket.isConnected();
    }


    /**
     * blocking method
     *
     * @return available size
     */
    public int available() throws IOException {

        int size = mReader.available();

        if (size == 0 && blockingTime > 0) {
            mLock.lock();
            try {
                while (size == 0) {
                    isAvailable.await(blockingTime, TimeUnit.MILLISECONDS);
                    size = mReader.available();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mLock.unlock();
            }
        }

        return size;

    }

    @Override
    public Multipart read() throws IOException {

        if (isConnect()) {
            byte[] bytes = new byte[available()];
            mReader.read(bytes);
            android.os.Parcel parcel = android.os.Parcel.obtain();
            parcel.unmarshall(bytes, 0, bytes.length);
            parcel.setDataPosition(0);
            Multipart multipart = parcel.readParcelable(Multipart.class.getClassLoader());
            parcel.recycle();
            return multipart;
        } else {
            throw new IOException("local bus have disconnected ! ");
        }

    }

    @Override
    public void write(Multipart multipart) throws IOException {
        if (isConnect()) {
            android.os.Parcel parcel = android.os.Parcel.obtain();
            parcel.writeParcelable(multipart, 0);
            mWriter.write(parcel.marshall());
            mWriter.flush();
            parcel.recycle();
        } else {
            throw new IOException("local bus have disconnected ! ");
        }
    }

    @Override
    public void close() throws IOException {

        if (mWriter != null) {
            mWriter.close();
            mWriter = null;
        }

        if (mReader != null) {
            mReader.close();
            mReader = null;
        }

    }

}
