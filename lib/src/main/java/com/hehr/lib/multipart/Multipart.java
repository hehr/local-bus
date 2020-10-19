package com.hehr.lib.multipart;

import android.text.TextUtils;

import com.hehr.lib.IBus;

/**
 * multipart run in local bus
 *
 * @author hehr
 */
public class Multipart implements android.os.Parcelable, Cloneable {

    /**
     * 操作符
     */
    private int type;
    /**
     * 消息名
     */
    private String topic;
    /**
     * client 名称
     */
    private String name;
    /**
     * 可选消息
     */
    private Extra extra;

    protected Multipart(android.os.Parcel in) {
        type = in.readInt();
        topic = in.readString();
        extra = in.readParcelable(Extra.class.getClassLoader());
        name = in.readString();
    }

    public static final Creator<Multipart> CREATOR = new Creator<Multipart>() {
        @Override
        public Multipart createFromParcel(android.os.Parcel in) {
            return new Multipart(in);
        }

        @Override
        public Multipart[] newArray(int size) {
            return new Multipart[size];
        }
    };

    public int getType() {
        return type;
    }

    public String getTopic() {
        return topic;
    }

    public Extra getExtra() {
        return extra;
    }

    public String getName() {
        return name;
    }

    private Multipart(int type, String topic, Extra extra, String name) {
        this.type = type;
        this.topic = topic;
        this.extra = extra;
        this.name = name;
    }

    private Multipart(Builder builder) {
        this(builder.type, builder.topic, builder.extra, builder.name);
    }


    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(topic);
        dest.writeParcelable(extra, flags);
        dest.writeString(name);
    }


    public static class Builder {

        private int type;

        private String topic;

        private String name;

        private Extra extra;

        public Builder setType(int type) {
            this.type = type;
            return this;
        }

        public Builder setTopic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder setExtra(Extra extra) {
            this.extra = extra;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        boolean isIllegal() {
            return TextUtils.isEmpty(topic) || TextUtils.isEmpty(name) || IBus.Type.isIllegal(type);
        }

        public Multipart build() {
            if (isIllegal()) {
                throw new IllegalArgumentException(" illegal multipart ! ");
            }
            return new Multipart(this);
        }

    }

    @Override
    public String toString() {
        return "Multipart{" +
                "type=" + type +
                ", topic='" + topic + '\'' +
                ", extra=" + extra +
                '}';
    }

    @Override
    public Multipart clone() throws CloneNotSupportedException {
        return (Multipart) super.clone();
    }
}
