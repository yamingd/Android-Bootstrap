package com.argo.sdk.util;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okio.BufferedSink;
import okio.Okio;
import timber.log.Timber;

/**
 * Created by user on 6/19/15.
 */
public final class AssetsUtil {

    /**
     *
     * @param context
     * @param name
     * @return
     */
    public static boolean checkAssetsExists(Context context, String name){
        InputStream in = null;
        try {
            in = context.getResources().getAssets().open(name);
        } catch (IOException e) {
            return false;
        }finally {
            if (null == in){
                return false;
            }else{
                try {
                    in.close();
                } catch (IOException e) {

                }
            }
        }

        return true;
    }

    /**
     * 拷贝Assets的文件
     * @param context
     * @param destFile
     * @param name
     * @return
     */
    public static boolean copyFromAssets(Context context, File destFile, String name){

        try {

            InputStream in = context.getResources().getAssets().open(name);
            if (null == in){
                Timber.e("File does not exist in assets. name=%s", name);
                return false;
            }

            Timber.d("start copy file: %s to %s", name, destFile);

            BufferedSink sink = Okio.buffer(Okio.sink(destFile));
            byte[] buf = new byte[1024];
            int len;

            try {

                while ((len = in.read(buf)) > 0){
                    sink.write(buf, 0, len);
                }
                return true;
            } catch (IOException e) {
                Timber.e(e, e.getMessage());
            }finally {
                try {
                    sink.close();
                    in.close();
                } catch (IOException e) {
                    Timber.e(e, e.getMessage());
                }
            }

        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }

        return false;
    }

}
