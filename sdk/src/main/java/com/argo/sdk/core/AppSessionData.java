package com.argo.sdk.core;

import com.argo.sqlite.annotations.Column;

/**
 * Created by user on 6/10/15.
 */
public class AppSessionData {

    @Column(pk = true)
    private String key;

    @Column
    private byte[] data;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
