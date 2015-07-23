package com.argo.sdk.http;

import android.content.Context;
import android.webkit.MimeTypeMap;

import com.google.protobuf.ByteString;
import com.argo.sdk.ApiError;
import com.argo.sdk.AppSession;
import com.argo.sdk.cache.CacheProvider;
import com.argo.sdk.cache.CacheWriter;
import com.argo.sdk.protobuf.PAppResponse;
import com.argo.sdk.providers.UserAgentProvider;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okio.BufferedSink;
import okio.Okio;
import timber.log.Timber;


/**
 * Created by user on 6/15/15.
 */
public class APIClientProvider {

    public static final MediaType MEDIA_TYPE_DEFAULT = MediaType.parse("application/octet-stream");
    public static APIClientProvider instance = null;

    private AppSession appSession;
    private OkHttpClient client;
    private String baseUrl;
    private Set<Map.Entry<String, String>> headers;
    private String userAgent;
    private Context context;
    private CacheProvider cacheProvider;

    public APIClientProvider(AppSession appSession, UserAgentProvider userAgentProvider, Context context, CacheProvider cacheProvider){
        this.appSession = appSession;
        this.headers = appSession.getHttpHeader().entrySet();
        this.client = new OkHttpClient();
        this.userAgent = userAgentProvider.get();
        this.context = context;
        this.baseUrl = appSession.getConfigValue("ApiBaseUrl", null);
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

        //拦截器
        this.client.interceptors().add(new LoggingInterceptor());
    }

    /**
     * 设置Http Header
     * @param builder
     */
    private void wrapHttpHeader(Request.Builder builder){
        for (Map.Entry<String, String> item : this.headers){
            builder.addHeader(item.getKey(), item.getValue());
        }
        appSession.getHttpHeader().entrySet();
        builder.addHeader("Accept", "application/x-protobuf");
        builder.addHeader("User-Agent", this.userAgent);
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
        String fullUrl = this.baseUrl + pathUrl;
        HttpUrl.Builder hb = HttpUrl.parse(fullUrl).newBuilder();
        if (null != params){
            Set<String> keys = params.keySet();
            for (String key : keys){
                hb.addQueryParameter(key, params.get(key) + "");
            }
        }
        return hb.build();
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
        wrapHttpHeader(builder);
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
            RequestBody file = RequestBody.create(MediaType.parse(mimeType), tmp);
            multipartBuilder.addFormDataPart(key, tmp.getName(), file);
            Timber.d("Media Type. %s, %s, %s", tmp, mimeType, file.contentType());
        }else if (o instanceof  byte[]){
            byte[] tmp = (byte[])o;
            RequestBody file = RequestBody.create(MEDIA_TYPE_DEFAULT, tmp);
            multipartBuilder.addFormDataPart(key, new Date().getTime() / 1000 + "", file);
        }
        else{
            multipartBuilder.addFormDataPart(key, o.toString());
        }
    }

    /**
     * 构造POST请求对象
     * @param pathUrl
     * @param params
     * @return
     */
    private Request buildPOSTRequest(String pathUrl, Map<String, Object> params){

        String fullUrl = this.baseUrl + pathUrl;
        RequestBody body = null;
        boolean hasFile = false;

        if (null != params) {

            Set<String> keys = params.keySet();
            for (String key : keys){
                Object o = params.get(key);
                if (null !=o && o instanceof File){
                    hasFile = true;
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
            formEncodingBuilder.add("_utm_", System.currentTimeMillis() / 1000 + "");
            body = formEncodingBuilder.build();

        }

        Request.Builder builder = new Request.Builder().url(fullUrl).post(body);
        wrapHttpHeader(builder);
        return builder.build();
    }

    /**
     * 解析请求响应
     * @param url
     * @param response
     * @return
     */
    private PAppResponse parseResponse(String url, Response response){
        PAppResponse.Builder builder = PAppResponse.newBuilder();
        try {
            builder.mergeFrom(response.body().byteStream());
        } catch (IOException e) {
            Timber.e(e, "url: %s", url);
        }
        return builder.build();
    }

    /**
     * 构造网络失败响应对象
     * @param msg
     * @return
     */
    private PAppResponse failureResponse(String msg){
        PAppResponse.Builder builder = PAppResponse.newBuilder();
        builder.setCode(500);
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
     * 异步读取URL, 返回Protobuf
     * @param url
     * @param params
     * @return
     */
    public Call asyncGet(final String url, Map<String, Object> params, final APICallback apiCallback){

        final Request request = buildGetRequest(url, params);

        Call call = client.newCall(request);

        call.enqueue(new Callback() {

            @Override
            public void onFailure(Request request, IOException e) {
                Timber.e(e, "url : %s", url);
                ApiError error = new ApiError(500, e);
                error.setUrl(url);
                apiCallback.onResponse(failureResponse(e.getMessage()), request, error);
            }

            @Override
            public void onResponse(Response response) throws IOException {

                if (!response.isSuccessful()) {
                    Timber.e("ERROR url : %s, %s", url, response);
                    ApiError error = new ApiError(response.code(), response.message());
                    error.setUrl(url);
                    apiCallback.onResponse(failureResponse(response.message()), request, error);
                } else {
                    PAppResponse pbrsp = parseResponse(url, response);
                    if (pbrsp.getCode() != 200) {
                        Timber.e("Error PB Resp: %s", pbrsp);
                        ApiError error = new ApiError(pbrsp.getCode(), pbrsp.getMsg());
                        error.setUrl(url);
                        apiCallback.onResponse(pbrsp, request, error);
                    } else {
                        Timber.d("Query Url: %s", request);
                        apiCallback.onResponse(pbrsp, request, null);
                    }
                }

            }

        });

        return call;
    }

    /**
     * 提交数据，支持文件、图片、音频.
     * params的value为File对象
     * @param url
     * @param params
     * @param apiCallback
     * @return
     */
    public Call asyncPOST(final String url, Map<String, Object> params, final APICallback apiCallback){

        final Request request = buildPOSTRequest(url, params);

        Call call = client.newCall(request);

        call.enqueue(new Callback() {

            @Override
            public void onFailure(Request request, IOException e) {
                Timber.e(e, "url : %s", url);
                ApiError error = new ApiError(500, e);
                error.setUrl(url);
                apiCallback.onResponse(failureResponse(e.getMessage()), request, error);
            }

            @Override
            public void onResponse(Response response) throws IOException {

                if (!response.isSuccessful()){
                    Timber.e("ERROR url : %s, %s", url, response);
                    ApiError error = new ApiError(response.code(), response.message());
                    error.setUrl(url);
                    apiCallback.onResponse(failureResponse(response.message()), request, error);
                }else{
                    PAppResponse pbrsp = parseResponse(url, response);
                    if (pbrsp.getCode() != 200){
                        Timber.e("Error PB Resp: %s", pbrsp);
                        ApiError error = new ApiError(pbrsp.getCode(), pbrsp.getMsg());
                        error.setUrl(url);
                        apiCallback.onResponse(pbrsp, request, error);
                    }else{
                        Timber.d("POST Url: %s", request);
                        apiCallback.onResponse(pbrsp, request, null);
                    }
                }

            }

        });

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
        }

        return list;
    }
}
