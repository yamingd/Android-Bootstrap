package com.argo.sdk.http;

import com.argo.sdk.BootConstants;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import timber.log.Timber;

/**
 * Created by user on 6/15/15.
 */
class LoggingInterceptor implements Interceptor {

    @Override public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        Timber.d("Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers());

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        if (BootConstants.pringHttpTS) {
            Timber.i("Received response for %s in %.1fms%n", response.request().url(), (t2 - t1) / 1e6d);
        }

        return response;
    }
}
