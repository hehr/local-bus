package com.hehr.lib;

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
    public int type;

    /**
     * topic,必选
     */
    public String topic;

    /**
     * param
     */
    public Bundle args;

    /**
     * result
     */
    public Bundle ret;


    public Multipart(int type, String topic) {
        this.type = type;
        this.topic = topic;
    }

    protected Multipart(Parcel in) {
        type = in.readInt();
        topic = in.readString();
        args = in.readBundle();
        ret = in.readBundle();
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

    public Multipart setArgs(Bundle args) {
        this.args = args;
        return this;
    }

    public Multipart setRet(Bundle ret) {
        this.ret = ret;
        return this;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(topic);
        dest.writeBundle(args);
        dest.writeBundle(ret);
    }
}
