package com.argo.sdk.core;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

/**
 * Created by user on 8/3/15.
 */
public class AppSecurity {

    static {
        System.loadLibrary("c++_shared");
        System.loadLibrary("kcrypto");
        System.loadLibrary("argoboot");
    }

    private Context context;
    private byte[] salt;

    public AppSecurity(Context context) {
        this.context = context;
        loadSalt();
    }

    private void loadSalt(){
        byte[] tmp = null;
        InputStream in = null;
        try {
            in = this.context.getResources().getAssets().open("cert");
            tmp = new byte[128];
            int total = in.read(tmp);
            Timber.d("to signSalt, total=%s", total);
        } catch (IOException e) {
            Timber.e(e, "load cert");
        }finally {
            if (null != in){
                try {
                    in.close();
                } catch (IOException e) {
                    Timber.e(e, "loadSalt");
                }
            }
        }

        salt = signSalt(tmp);
    }

    private native byte[] signSalt(byte[] data);

    public byte[] getSalt() {
        return salt;
    }

    public native String authHeader(String sid, String secret, String userId);

    public native String genDeviceToken(String deviceId, String appName);

    public native String genSessionId(String sid, String secret);

    public native String signRequest(String sid, String secret, String url, String userId);

    public native String encrypt(String plain);

    public String decrypt(String data){
        return decrypt(data.toCharArray());
    }

    public native String decrypt(char[] data);

    public native void clean();

    public native void init(String seed);
}
