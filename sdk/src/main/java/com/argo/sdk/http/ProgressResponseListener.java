package com.argo.sdk.http;

/**
 *
 * http://blog.csdn.net/sbsujjbcy/article/details/48194701
 *
 * Created by user on 9/6/15.
 */
public interface ProgressResponseListener {

    /**
     *
     * @param bytesRead
     * @param contentLength
     * @param done
     */
    void onResponseProgress(long bytesRead, long contentLength, boolean done);
}
