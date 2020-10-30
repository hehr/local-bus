package com.hehr.lib;

/**
 * extra
 *
 * @author hehr
 */
public class Extra {

    String character;
    boolean bool;
    byte[] binary;
    double digital;

    private Extra(String character, boolean bool, byte[] binary, double digital) {
        this.character = character;
        this.bool = bool;
        this.binary = binary;
        this.digital = digital;
    }

    private Extra(Builder builder) {
        this(builder.character, builder.bool, builder.binary, builder.digital);
    }

    public String getCharacter() {
        return character;
    }

    public boolean isBool() {
        return bool;
    }

    public byte[] getBinary() {
        return binary;
    }

    public double getDigital() {
        return digital;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        String character;
        boolean bool;
        byte[] binary;
        double digital;

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

}
