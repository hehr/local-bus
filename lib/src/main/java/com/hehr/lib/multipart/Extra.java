package com.hehr.lib.multipart;

import java.util.Arrays;

public class Extra implements android.os.Parcelable {

    private String character;

    private boolean bool;

    private byte[] binary;

    private double digital;

    protected Extra(android.os.Parcel in) {
        character = in.readString();
        bool = in.readByte() != 0;
        binary = in.createByteArray();
        digital = in.readDouble();
    }

    public static final Creator<Extra> CREATOR = new Creator<Extra>() {
        @Override
        public Extra createFromParcel(android.os.Parcel in) {
            return new Extra(in);
        }

        @Override
        public Extra[] newArray(int size) {
            return new Extra[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeString(character);
        dest.writeByte((byte) (bool ? 1 : 0));
        dest.writeByteArray(binary);
        dest.writeDouble(digital);
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
}
