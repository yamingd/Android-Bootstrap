package com.argo.sdk.http;

import com.squareup.okhttp.ResponseBody;

/**
 * Created by user on 6/26/15.
 */
public interface DownloadCallback {

    /**
     *
     * @param e
     */
    void onError(Exception e);

    /**
     * 如cache则返回cacheKey, responseBody=null,
     * 否则cachekey=null, responseBody=文件流
     */
    void onCompleted(String cacheKey, ResponseBody responseBody);

    /**
     * 还没支持
     * @param total
     * @param current
     * @param finished
     */
    void onDownloading(long total, long current, boolean finished);

}
