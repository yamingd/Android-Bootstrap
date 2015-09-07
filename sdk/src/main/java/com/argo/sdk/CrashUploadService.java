package com.argo.sdk;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.util.ArrayMap;

import com.argo.sdk.http.APIClientProvider;
import com.argo.sdk.protobuf.PAppResponse;
import com.argo.sdk.http.APICallback;
import com.squareup.okhttp.Request;

import java.io.File;
import java.util.Map;

/**
 * Created by user on 8/12/15.
 */
public class CrashUploadService extends IntentService {

    public static String uploadUrl;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public CrashUploadService() {
        super("CrashUploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (CrashHandler.rootFolder == null){
            return;
        }

        File[] files = CrashHandler.rootFolder.listFiles();
        if (files.length > 0){

            Map<String, Object> map = new ArrayMap<String, Object>();
            final File file = files[0];
            map.put("crash", file);

            APIClientProvider.instance.asyncPOST(uploadUrl, map, new APICallback() {

                @Override
                public void onResponse(PAppResponse response, Request request, ApiError error) {
                    if (null != error) {
                        error.printout();
                    }

                    file.delete();
                }

            });

        }

    }

}
