package com.hehr.lib;

import android.os.Bundle;

/**
 * rpc callback
 */
public class RpcCallback {

    private Bundle result;

    private boolean isCalled = false;

    private String topic;

    public Bundle getResult() {
        return result;
    }

    public boolean isCalled() {
        return isCalled;
    }

    public String getTopic() {
        return topic;
    }

    /**
     * 外部允许通知结果改变
     *
     * @param bundle
     */
    public void update(Bundle bundle) {
        this.result = bundle;
        this.isCalled = true;
    }

    /**
     * 回设callback
     */
    public void reset() {
        this.isCalled = false;
        this.topic = "";
    }

    private RpcCallback(Bundle result, boolean isCalled, String topic) {
        this.result = result;
        this.isCalled = isCalled;
        this.topic = topic;
    }

    private RpcCallback(Builder builder) {
        this(builder.result, builder.isCalled, builder.topic);
    }

    public static class Builder {

        private Bundle result;

        private boolean isCalled = false;

        private String topic;

        public Builder setResult(Bundle result) {
            this.result = result;
            return this;
        }

        public Builder setCalled(boolean called) {
            isCalled = called;
            return this;
        }

        public Builder setTopic(String topic) {
            this.topic = topic;
            return this;
        }

        public RpcCallback create() {
            return new RpcCallback(this);
        }
    }

}
