package com.argo.sdk.http;

/**
 * Created by user on 6/16/15.
 */
public interface APICallProgress {

    void onUpdate(long total, long current, boolean finished);

}
