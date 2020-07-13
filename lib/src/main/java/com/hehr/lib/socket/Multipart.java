package com.hehr.lib.socket;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * multipart
 *
 * @author hehr
 */
public class Multipart implements Parcelable {
    /**
     * 操作符
     */
    public int type = -1;

    /**
     * topic,必选
     */
    public String topic;

    /**
     * 数据包,非必选
     */
    public byte[] args;

    /**
     * rpc  param
     */
    public Bundle rpcParam;

    /**
     * rpc result
     */
    public Bundle rpcResult;


    public Multipart(int type, String topic) {
        this.type = type;
        this.topic = topic;
    }

    public Multipart setArgs(byte[] args) {
        this.args = args;
        return this;
    }

    public Multipart setArgs(String args) {
        this.args = args.getBytes();
        return this;
    }

    public Multipart setRpcParam(Bundle rpcParam) {
        this.rpcParam = rpcParam;
        return this;
    }

    public Multipart setRpcResult(Bundle rpcResult) {
        this.rpcResult = rpcResult;
        return this;
    }

    protected Multipart(Parcel in) {
        type = in.readInt();
        topic = in.readString();
        args = in.createByteArray();
        rpcParam = in.readBundle();
        rpcResult = in.readBundle();
    }

    public static final Creator<Multipart> CREATOR = new Creator<Multipart>() {
        @Override
        public Multipart createFromParcel(Parcel in) {
            return new Multipart(in);
        }

        @Override
        public Multipart[] newArray(int size) {
            return new Multipart[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(topic);
        dest.writeByteArray(args);
        dest.writeBundle(rpcParam);
        dest.writeBundle(rpcResult);
    }

    public boolean isEmpty() {
        return type == -1 || TextUtils.isEmpty(topic);
    }

    @Override
    public String toString() {
        return "Multipart{" +
                "type=" + type +
                ", topic='" + topic +
                '}';
    }
}
