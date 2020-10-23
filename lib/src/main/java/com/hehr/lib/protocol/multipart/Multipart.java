package com.hehr.lib.protocol.multipart;

import android.text.TextUtils;

import com.hehr.lib.IBus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * multipart run in local bus
 *
 * @author hehr
 */
public class Multipart {

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
                ", name='" + name + '\'' +
                ", extra=" + extra +
                '}';
    }


    /**
     * encode multipart
     *
     * @return byte[]
     * @throws JSONException
     * @throws UnsupportedEncodingException
     */
    public byte[] encode() throws JSONException, UnsupportedEncodingException {

        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("topic", topic);
        json.put("name", name);
        if (extra != null && !extra.isEmpty()) {
            json.put("extra", extra.toJson());
        }

        return json.toString().getBytes();

    }

    /**
     * decode  bytes
     *
     * @param bytes 报文
     * @return {@link Multipart}
     * @throws UnsupportedEncodingException
     * @throws JSONException
     */
    public static Multipart decode(byte[] bytes) throws UnsupportedEncodingException, JSONException {
        return decode(bytes, true);
    }

    /**
     * decode  bytes
     *
     * @param bytes      报文
     * @param isComplete 是否完整解码
     * @return {@link Multipart}
     * @throws UnsupportedEncodingException
     * @throws JSONException
     */
    public static Multipart decode(byte[] bytes, boolean isComplete) throws UnsupportedEncodingException, JSONException {
        JSONObject json = new JSONObject(new String(bytes, "utf-8"));
        Builder builder = newBuilder();
        builder.setType(json.getInt("type"));
        builder.setName(json.getString("name"));
        builder.setTopic(json.getString("topic"));
        if (isComplete && json.has("extra")) {
            builder.setExtra(Extra.transform(json.optJSONObject("extra")));
        }
        return builder.build();
    }


}
