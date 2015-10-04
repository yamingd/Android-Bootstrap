package com.argo.sdk.http;

import com.argo.sdk.ApiError;
import com.argo.sdk.AppSession;
import com.argo.sdk.BootConstants;
import com.argo.sdk.event.AccountKickOffEvent;
import com.argo.sdk.event.EventBus;
import com.argo.sdk.protobuf.PAppResponse;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import timber.log.Timber;

/**
 * Created by user on 8/22/15.
 */
public class ProtobufReponseCallBack implements Callback {

    private long userId;
    private String url;
    private APICallback apiCallback;
    private Request request;
    private int securityTag;
    private AppSession appSession;
    private APIClientProvider apiClientProvider;
    private Call call;
    private boolean revStoppedEvent = false;

    public ProtobufReponseCallBack(long userId, String url, APICallback apiCallback, Request request,
                                   APIClientProvider apiClientProvider, Call call) {
        this.userId = userId;
        this.url = url;
        this.apiCallback = apiCallback;
        this.request = request;
        this.securityTag = apiClientProvider.getSecurityTag();
        this.appSession = apiClientProvider.getAppSession();
        this.apiClientProvider = apiClientProvider;
        this.call = call;
    }

    public String getUrl() {
        return url;
    }

    public void onHttpCallStopEvent(String[] filterUrls){
        revStoppedEvent = true;
        if (this.request.method().equalsIgnoreCase("post")){
            // these are ok
        }else{

            for (int i = 0; i < filterUrls.length; i++) {
                if (this.url.startsWith(filterUrls[i])){
                    return;
                }
            }

            Timber.d("HttpCall Stop. url=%s", this.url);
            call.cancel();
        }
    }

    public void onDone(){
        this.apiClientProvider.onResponseCallbackDone(this);
        this.apiClientProvider.releaseLock();
    }

    @Override
    public void onFailure(Request request, IOException e) {
        Timber.e(e, "url : %s", url);

        if (this.userId != appSession.get().getUserId() || revStoppedEvent){
            Timber.d("Http Callback ignore. url=%s, last userId=%s", this.url, this.userId);
            this.onDone();
            return;
        }

        ApiError error = new ApiError(408, e);
        error.setUrl(url);
        String msg = e.getMessage();
        if (e instanceof SocketTimeoutException){
            msg = "网络连接有错误, 请检查";
        }else if(e instanceof ConnectException){
            msg = "网络连接有错误, 请检查";
        }

        try {
            apiCallback.onResponse(failureResponse(408, msg), request, error);
        } catch (Exception e1) {
            Timber.e(e, "apiCallback.onResponse");
        }

        if (error.getCode() == 408){
            this.apiClientProvider.postNetworkStatusEvent(false);
        }

        this.onDone();
    }

    @Override
    public void onResponse(Response response) throws IOException {
        if (this.userId != appSession.get().getUserId() || revStoppedEvent){
            Timber.d("Http Callback ignore. url=%s, last userId=%s", this.url, this.userId);
            this.onDone();
            return;
        }

        long t1 = System.nanoTime();
        if (!response.isSuccessful()) {
            Timber.e("ERROR url : %s, %s", url, response);
            ApiError error = new ApiError(response.code(), response.message());
            error.setUrl(url);
            try {
                apiCallback.onResponse(failureResponse(408, response.message()), request, error);
            } catch (Exception e) {
                Timber.e(e, "apiCallback.onResponse");
            }
        } else {
            PAppResponse pbrsp = parseResponse(url, response);
            if (pbrsp.getCode() != 200) {
                Timber.e("Error PB Resp: %s %s \n %s", url, pbrsp.getMsg(), pbrsp.getErrorsList());
                if (pbrsp.getCode() == 60900){
                    EventBus.instance.post(new AccountKickOffEvent());
                    return;
                }
                ApiError error = new ApiError(pbrsp.getCode(), pbrsp.getMsg());
                error.setUrl(url);
                try {
                    apiCallback.onResponse(pbrsp, request, error);
                } catch (Exception e) {
                    Timber.e(e, "apiCallback.onResponse");
                }
            } else {
                Timber.d("Query Url: %s", request);
                try {
                    apiCallback.onResponse(pbrsp, request, null);
                } catch (Exception e) {
                    Timber.e(e, "apiCallback.onResponse");
                }
            }
        }

        long t2 = System.nanoTime();
        if (BootConstants.pringHttpTS) {
            Timber.i("HandleResponse for %s in %.1fms%n", response.request().url(), (t2 - t1) / 1e6d);
        }

        this.apiClientProvider.postNetworkStatusEvent(true);

        this.onDone();
    }

    /**
     * 构造网络失败响应对象
     * @param msg
     * @return
     */
    private PAppResponse failureResponse(int code, String msg){
        PAppResponse.Builder builder = PAppResponse.newBuilder();
        builder.setCode(code);
        builder.setMsg(msg);
        return builder.build();
    }

    private PAppResponse okResponse(String msg){
        PAppResponse.Builder builder = PAppResponse.newBuilder();
        builder.setCode(200);
        builder.setMsg(msg);
        return builder.build();
    }

    /**
     * 解析请求响应
     * @param url
     * @param response
     * @return
     */
    private PAppResponse parseResponse(String url, Response response){
        //RunningStatProvider.instance.printMemoryUsage("beforeParseResponse");
        String xtag = response.header(APIClientProvider.X_TAG);
        PAppResponse.Builder builder = PAppResponse.newBuilder();
        try {
            final byte[] input = response.body().bytes();
            if (xtag != null) {
                builder.mergeFrom(input, securityTag, input.length - securityTag);
            }else{
                builder.mergeFrom(input);
            }
        } catch (IOException e) {
            Timber.e(e, "url: %s", url);
        }
        PAppResponse appResponse = builder.build();
        try {
            response.body().close();
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
        //RunningStatProvider.instance.printMemoryUsage("afterParseResponse");
        return appResponse;
    }
}
