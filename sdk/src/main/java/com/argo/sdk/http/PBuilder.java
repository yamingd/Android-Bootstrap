package com.argo.sdk.http;

import android.support.v4.util.ArrayMap;

import java.util.Map;

/**
 * Created by user on 6/23/15.
 */
public final class PBuilder {

    private Map<String, Object> values = new ArrayMap<String, Object>();

    public static PBuilder i(){
        return new PBuilder();
    }

    public PBuilder v(String name, Object value){
        if (null != value) {
            values.put(name, value);
        }
        return this;
    }

    public PBuilder r(String name){
        values.remove(name);
        return this;
    }

    public Map<String, Object> vs(){
        return values;
    }

    public boolean empty(){
        return values.isEmpty();
    }
}
