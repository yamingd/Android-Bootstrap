package com.argo.sdk.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 6/23/15.
 */
public final class PBuilder {

    private Map<String, Object> values = new HashMap<String, Object>();

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
}
