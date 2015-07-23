package com.argo.sdk.http;

import com.argo.sdk.ApiError;
import com.argo.sdk.protobuf.PAppResponse;

import com.squareup.okhttp.Request;

/**
 * Created by user on 6/15/15.
 */
public interface APICallback {

    /**
     *
     * @param response
     * @param request
     * @param error
     */
    void onResponse(PAppResponse response, Request request, ApiError error);
}
