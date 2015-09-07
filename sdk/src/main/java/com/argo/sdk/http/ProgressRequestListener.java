package com.argo.sdk.http;

/**
 *
 * http://blog.csdn.net/sbsujjbcy/article/details/48194701
 *
 * Created by user on 9/6/15.
 */
public interface ProgressRequestListener {
    /**
     *
     * @param bytesWritten
     * @param contentLength
     * @param done
     */
    void onRequestProgress(long bytesWritten, long contentLength, boolean done);
}
