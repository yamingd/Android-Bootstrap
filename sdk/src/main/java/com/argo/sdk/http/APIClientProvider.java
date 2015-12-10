package com.argo.sdk.http;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v4.util.ArrayMap;
import android.webkit.MimeTypeMap;

import com.argo.sdk.AppSession;
import com.argo.sdk.cache.CacheProvider;
import com.argo.sdk.cache.CacheWriter;
import com.argo.sdk.event.EventBus;
import com.argo.sdk.event.NetworkConnectionStatusEvent;
import com.argo.sdk.protobuf.PAppResponse;
import com.argo.sdk.providers.UserAgentProvider;
import com.google.protobuf.ByteString;
import com.jakewharton.disklrucache.DiskLruCache;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import okio.BufferedSink;
import okio.Okio;
import timber.log.Timber;


/**
 * Created by user on 6/15/15.
 */
public class APIClientProvider {

    public static final MediaType MEDIA_TYPE_DEFAULT = MediaType.parse("application/octet-stream");
    public static final String HEADER_X_SIGN = "X-sign";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String APPLICATION_X_PROTOBUF = "application/x-protobuf";
    public static final String X_TAG = "X-tag";

    public static APIClientProvider instance = null;


    private final static Map<String, String> MIME = new ArrayMap<>();

    static  {
        MIME.put("pdf", "application/pdf");
        MIME.put("zip", "application/zip");
        MIME.put("gzip", "application/gzip");
        MIME.put("mp4", "audio/mp4");
        MIME.put("mp3", "audio/mpeg");
        MIME.put("aac", "audio/aac");
    }

    private AppSession appSession;
    private OkHttpClient client;
    private String baseUrl;
    private Set<Map.Entry<String, String>> headers;
    private String userAgent;
    private Context context;
    private CacheProvider cacheProvider;
    private WifiManager wifiManager;
    private WifiManager.WifiLock wifiLock;
    private int securityTag = 16;
    private List<ProtobufReponseCallBack> callBackList = new ArrayList<>();
    private AtomicLong byteDataId = new AtomicLong();

    public APIClientProvider(AppSession appSession, UserAgentProvider userAgentProvider, Context context, CacheProvider cacheProvider, WifiManager wifiManager){
        this.appSession = appSession;
        this.headers = appSession.getHttpHeader().entrySet();
        this.client = new OkHttpClient();
        this.userAgent = userAgentProvider.get();
        this.context = context;
        this.cacheProvider = cacheProvider;
        this.wifiManager = wifiManager;
        this.baseUrl = appSession.getConfigValue("ApiBaseUrl", null);
        this.wifiLock = wifiManager.createWifiLock(this.getClass().getName());
        this.configHttpClient();
        instance = this;
    }

    /**
     * 配置Client对象
     */
    private void configHttpClient(){
        this.client.setConnectTimeout(30, TimeUnit.SECONDS);
        this.client.setReadTimeout(30, TimeUnit.SECONDS);
        this.client.setWriteTimeout(30, TimeUnit.SECONDS);
        this.client.setConnectionPool(ConnectionPool.getDefault());
        this.client.getDispatcher().setMaxRequestsPerHost(3);
        this.client.getDispatcher().setMaxRequests(3);
        //拦截器
        this.client.interceptors().add(new LoggingInterceptor());
    }

    /**
     * 设置Http Header
     * @param builder
     */
    private void wrapHttpHeader(Request.Builder builder, String url){
        for (Map.Entry<String, String> item : this.headers){
            builder.addHeader(item.getKey(), item.getValue());
        }
        final String signRequest = appSession.signRequest(url);
        builder.addHeader(HEADER_X_SIGN, signRequest);
        builder.addHeader(HEADER_ACCEPT, APPLICATION_X_PROTOBUF);
        builder.addHeader(HEADER_USER_AGENT, this.userAgent);
        //Timber.e("signRequest: %s %s", url, signRequest);
    }

    public void resetHeaders(){
        this.headers = appSession.getHttpHeader().entrySet();
    }

    /**
     * 构建URL
     * @param pathUrl
     * @param params
     * @return
     */
    private HttpUrl buildUrl(String pathUrl, Map<String, Object> params){
        String fullUrl = "";
        if(pathUrl.contains(this.baseUrl)){
            fullUrl = pathUrl;
        }else{
            fullUrl = this.baseUrl + pathUrl;
        }
        HttpUrl.Builder hb = HttpUrl.parse(fullUrl).newBuilder();
        if (null != params){
            Set<String> keys = params.keySet();
            for (String key : keys){
                hb.addQueryParameter(key, params.get(key) + "");
            }
        }
        HttpUrl httpUrl = hb.build();
        //Timber.i("fullUrl:%s host:%s, port:%s", fullUrl, httpUrl.host(), httpUrl.port());
        return httpUrl;
    }

    /**
     * 构造GET请求对象
     * @param url
     * @param params
     * @return
     */
    private Request buildGetRequest(String url, Map<String, Object> params){
        HttpUrl fullUrl = buildUrl(url, params);
        Request.Builder builder = new Request.Builder().url(fullUrl);
        wrapHttpHeader(builder, url);
        builder.tag(url);
        return builder.build();
    }


    private void addPart(MultipartBuilder multipartBuilder, String key, Object o){
        if (o == null){
            return;
        }

        if (o instanceof File) {
            File tmp = (File) o;
            String extType = MimeTypeMap.getFileExtensionFromUrl(tmp.getAbsolutePath());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extType);
            if (mimeType == null){
                mimeType = MIME.get(extType.toLowerCase());
            }
            RequestBody file = RequestBody.create(MediaType.parse(mimeType), tmp);
            multipartBuilder.addFormDataPart(key, tmp.getName(), file);
            Timber.d("Media Type. %s, %s, %s", tmp, mimeType, file.contentType());
        }else if (o instanceof  byte[]){
            byte[] tmp = (byte[])o;
            RequestBody file = RequestBody.create(MEDIA_TYPE_DEFAULT, tmp);
            multipartBuilder.addFormDataPart(key, byteDataId.incrementAndGet() + "", file);
        }
        else{
            multipartBuilder.addFormDataPart(key, o.toString());
        }
    }

    /**
     * 构造POST、PUT、DELETE请求对象
     * @param pathUrl
     * @param params
     * @return
     */
    private Request buildRequest(String method, String pathUrl, Map<String, Object> params){

        String fullUrl = this.baseUrl + pathUrl;
        RequestBody body = null;
        boolean hasFile = false;

        if (null != params) {

            Set<String> keys = params.keySet();
            for (String key : keys){
                Object o = params.get(key);
                if (null == o){
                    continue;
                }
                if (o instanceof File){
                    hasFile = true;
                    break;
                }else if (o instanceof  List){
                    List list = (List) o;
                    for (int i = 0; i < list.size(); i++) {
                        Object o2 = list.get(i);
                        if (null != o2 && o2 instanceof File){
                            hasFile = true;
                            break;
                        }
                    }
                }
                if (hasFile){
                    break;
                }
            }
            if (hasFile) {
                MultipartBuilder multipartBuilder = new MultipartBuilder();
                for (String key : keys) {
                    Object o = params.get(key);
                    if (null == o) {
                        Timber.e("参数%s为NULL. ", key);
                        continue;
                    }

                    if (o instanceof List) {
                        List list = (List) o;
                        for (int i = 0; i < list.size(); i++) {
                            Object o2 = list.get(i);
                            addPart(multipartBuilder, key, o2);
                        }
                    }else if (o instanceof  Map){
                        Timber.e("don't support Map as parameter value. %s", o);
                    }else{
                        addPart(multipartBuilder, key, o);
                    }
                }
                body = multipartBuilder.build();

            }else{

                FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
                for (String key : keys) {
                    Object o = params.get(key);
                    if (null == o) {
                        Timber.e("参数%s为NULL. ", key);
                        continue;
                    }
                    formEncodingBuilder.add(key, o.toString());
                }
                body = formEncodingBuilder.build();
            }
        }else{

            FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
            formEncodingBuilder.add("_utm_", System.currentTimeMillis() + "");
            body = formEncodingBuilder.build();

        }

        Request.Builder builder = new Request.Builder().url(fullUrl).method(method, body);
        wrapHttpHeader(builder, pathUrl);
        return builder.build();
    }

    public int getSecurityTag() {
        return securityTag;
    }

    public void setSecurityTag(int securityTag) {
        this.securityTag = securityTag;
    }

    public AppSession getAppSession() {
        return appSession;
    }

    /**
     * 注销、停止时调用
     */
    public void stopAll(String[] filterUrls){
//        for (int i = 0; i < callBackList.size(); i++) {
//            callBackList.get(i).onHttpCallStopEvent(filterUrls);
//        }
//        callBackList.clear();
    }

    public void releaseLock(){
        if (this.wifiLock.isHeld()){
            this.wifiLock.release();
        }
    }

    public void onResponseCallbackDone(ProtobufReponseCallBack callBack){
        callBackList.remove(callBack);
    }

    private int trackNetworkStatus = 0;
    public void postNetworkStatusEvent(boolean ok){
        EventBus.instance.post(new NetworkConnectionStatusEvent(ok));
    }

    /**
     * 异步读取URL, 返回Protobuf
     * @param url
     * @param params
     * @return
     */
    public Call asyncGet(final String url, Map<String, Object> params, final APICallback apiCallback){

        //RunningStatProvider.instance.printMemoryUsage("beforeAsyncGet");

        final Request request = buildGetRequest(url, params);

        Call call = client.newCall(request);

        this.wifiLock.acquire();

        final ProtobufReponseCallBack responseCallback = new ProtobufReponseCallBack(appSession.get().getUserId(), url, apiCallback, request, this, call);
        //callBackList.add(responseCallback);

        call.enqueue(responseCallback);

        return call;
    }

    /**
     * 提交数据，支持文件、图片、音频.
     * params的value为File对象
     * @param url
     * @param params
     * @param apiCallback
     * @return Call
     */
    public Call asyncPOST(final String url, Map<String, Object> params, final APICallback apiCallback){

        final Request request = buildRequest("POST", url, params);

        Call call = client.newCall(request);

        this.wifiLock.acquire();


        final ProtobufReponseCallBack responseCallback = new ProtobufReponseCallBack(appSession.get().getUserId(), url, apiCallback, request, this, call);
        //callBackList.add(responseCallback);

        call.enqueue(responseCallback);

        return call;

    }

    /**
     * 提交数据，支持文件、图片、音频.
     * params的value为File对象
     * @param url
     * @param params
     * @param apiCallback
     * @return Call
     */
    public Call asyncPUT(final String url, Map<String, Object> params, final APICallback apiCallback){

        final Request request = buildRequest("PUT", url, params);

        Call call = client.newCall(request);

        this.wifiLock.acquire();


        final ProtobufReponseCallBack responseCallback = new ProtobufReponseCallBack(appSession.get().getUserId(), url, apiCallback, request, this, call);
        //callBackList.add(responseCallback);

        call.enqueue(responseCallback);

        return call;

    }

    /**
     * 删除数据.
     * @param url
     * @param params
     * @param apiCallback
     * @return Call
     */
    public Call asyncDelete(final String url, Map<String, Object> params, final APICallback apiCallback){

        final Request request = buildRequest("DELETE", url, params);

        Call call = client.newCall(request);

        this.wifiLock.acquire();

        final ProtobufReponseCallBack responseCallback = new ProtobufReponseCallBack(appSession.get().getUserId(), url, apiCallback, request, this, call);
        //callBackList.add(responseCallback);

        call.enqueue(responseCallback);

        return call;

    }

    /**
     * 下载文件
     * @param url
     * @param cache
     * @param callback
     * @return
     */
    public Call asyncDownload(final String url, final boolean cache, final DownloadCallback callback){

        final Request request = buildGetRequest(url, null);

        Call call = client.newCall(request);

//        final ProgressResponseListener progressResponseListener = new ProgressResponseListener() {
//
//            @Override
//            public void onResponseProgress(long bytesRead, long contentLength, boolean done) {
//                Timber.d("downloading... read=%s, total=%s", bytesRead, contentLength);
//                callback.onDownloading(contentLength, bytesRead, done);
//            }
//        };
//
//        final Interceptor interceptor = new Interceptor() {
//            @Override
//            public Response intercept(Interceptor.Chain chain) throws IOException {
//                //拦截
//                Response originalResponse = chain.proceed(chain.request());
//                //包装响应体并返回
//                return originalResponse.newBuilder()
//                        .body(new ProgressResponseBody(originalResponse.body(), progressResponseListener))
//                        .build();
//            }
//        };
//
//        //克隆
//        OkHttpClient clone = client.clone();
//
//        //增加拦截器
//        clone.networkInterceptors().add(interceptor);


        call.enqueue(new Callback() {

            @Override
            public void onFailure(Request request, IOException e) {
                Timber.e(e, "url : %s", url);
                callback.onError(e);
            }

            @Override
            public void onResponse(final Response response) throws IOException {

                if (!response.isSuccessful()) {

                    Timber.e("ERROR url : %s, %s", url, response);
                    callback.onError(new Exception(response.message()));

                } else {

                    if (cache){

                        final String[] keys = new String[]{null};
                        cacheProvider.put(url, new CacheWriter() {
                            @Override
                            public boolean write(String cacheKey, DiskLruCache.Editor editor) {

                                BufferedSink sink = null;
                                try {
                                    OutputStream outputStream = editor.newOutputStream(0);
                                    sink = Okio.buffer(Okio.sink(outputStream));
                                }  catch (IOException e) {
                                    Timber.e(e.getMessage(), e);
                                    return false;
                                }

                                boolean hasError = false;

                                try {
                                    sink.writeAll(response.body().source());
                                    keys[0] = cacheKey;
                                } catch (IOException e) {
                                    hasError = true;
                                    Timber.e(e, e.getMessage());

                                }finally {
                                    try {
                                        sink.close();
                                    } catch (IOException e) {
                                        Timber.e(e, e.getMessage());
                                    }
                                    if (!hasError) {
                                        Timber.d("Download Url: %s", request);
                                    }
                                }

                                return !hasError;
                            }
                        });

                        callback.onCompleted(keys[0], null);

                    }else{

                        callback.onCompleted(null, response.body());

                    }

                }

            }

        });

        return call;

    }

    /**
     * 转换PB数据
     * @param response
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> List<T> parseProtobufResponse(PAppResponse response, Class<?> clazz) throws Exception {

        List<T> list = new ArrayList<T>();
        if (response.getDataCount() > 0){

            Timber.d("%s parseProtobufResponse start: %s", this, System.currentTimeMillis());

            Method parseFrom = null;
            try {
                parseFrom = clazz.getMethod("parseFrom", ByteString.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Expected a protobuf message but was " + clazz.getName());
            }

            for (ByteString bytes : response.getDataList()){

                try {

                    Object o = parseFrom.invoke(null, bytes);
                    list.add((T)o);

                } catch (InvocationTargetException e) {
                    throw new ConversionException(clazz.getName() + ".parseFrom() failed", e.getCause());
                } catch (IllegalAccessException e) {
                    throw new AssertionError();
                }

            }

            Timber.d("%s parseProtobufResponse end: %s", this, System.currentTimeMillis());
        }

        return list;
    }
}
