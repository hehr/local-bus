package com.hehr.lib.protocol.multipart;

import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class Extra {

    private String character;

    private boolean bool;

    private byte[] binary;

    private double digital;

    public String getCharacter() {
        return character;
    }

    public boolean getBool() {
        return bool;
    }

    public byte[] getBinary() {
        return binary;
    }

    public double getDigital() {
        return digital;
    }

    private Extra(String character, boolean bool, byte[] binary, double digital) {
        this.character = character;
        this.bool = bool;
        this.binary = binary;
        this.digital = digital;
    }

    private Extra(Builder builder) {
        this(builder.character, builder.bool, builder.binary, builder.digital);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String character;

        private boolean bool;

        private byte[] binary;

        private double digital;

        public Builder setCharacter(String character) {
            this.character = character;
            return this;
        }

        public Builder setBool(boolean bool) {
            this.bool = bool;
            return this;
        }

        public Builder setBinary(byte[] binary) {
            this.binary = binary;
            return this;
        }

        public Builder setDigital(double digital) {
            this.digital = digital;
            return this;
        }

        public Extra build() {
            return new Extra(this);
        }
    }

    @Override
    public String toString() {
        return "Extra{" +
                "character='" + character + '\'' +
                ", bool=" + bool +
                ", binary=" + Arrays.toString(binary) +
                ", digital=" + digital +
                '}';
    }

    boolean isEmpty() {
        return TextUtils.isEmpty(character) && bool == false && binary == null && digital == 0;

    }

    /**
     * 转JSON
     *
     * @return {@link JSONObject}
     * @throws JSONException
     * @throws UnsupportedEncodingException
     */
    public JSONObject toJson() throws JSONException {

        JSONObject json = new JSONObject();

        json.put("character", character);

        json.put("bool", bool);

        json.put("binary", Base64.encodeToString(binary, Base64.NO_WRAP));

        json.put("digital", digital);

        return json;
    }

    /**
     * transform json to Extra
     *
     * @param json
     * @return {@link Extra}
     */
    public static Extra transform(JSONObject json) {

        Builder builder = newBuilder();

        if (json.has("character")) {
            builder.setCharacter(json.optString("builder"));
        }

        if (json.has("bool")) {
            builder.setBool(json.optBoolean("bool"));
        }

        if (json.has("binary")) {
            builder.setBinary(Base64.decode(json.optString("binary"), Base64.NO_WRAP));
        }

        if (json.has("digital")) {
            builder.setDigital(json.optDouble("digital"));
        }

        return builder.build();

    }
}
