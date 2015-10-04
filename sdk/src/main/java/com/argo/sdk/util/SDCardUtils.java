package com.argo.sdk.util;

import android.content.Context;
import android.os.Environment;

import com.argo.sdk.BootConstants;

import java.io.File;

/**
 * Created by allenduan on 2015/7/16 0016.
 */
public class SDCardUtils {

    public static String ROOT_FOLDER = null;

    /**
     * Check the SD card
     *
     * @return
     */
    public static boolean isSDCardAvailable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * get the SD card root dir
     *
     * @return
     */
    public static String getSdcardRootDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * get the cache root dir
     *
     * @return
     */
    public static String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                && !Environment.isExternalStorageRemovable()
                && context.getExternalCacheDir()!=null) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    /**
     * get the  download  media cache root dir
     *
     * @return
     */
    public static String getDownloadDiskCacheDir(Context context) {
        return getDiskCacheDir(context) + File.separator + BootConstants.CACHE_ROOT_DIR;
    }


    /**
     * get the  record  media cache root dir
     *
     * @return
     */
    public static String getRecorderDiskCacheDir(Context context) {
        return getDiskCacheDir(context) + File.separator + BootConstants.SDCARD_ROOT_DIR;
    }

    /**
     * @return
     */
    public static boolean ensureRootFolder(Context context) {
        String sdcardCachePath = getDiskCacheDir(context);
        String dir = sdcardCachePath + File.separator + BootConstants.SDCARD_ROOT_DIR;
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        ROOT_FOLDER = dir;
        return true;
    }


    /**
     * @return
     */
    public static boolean ensureDiskCacheFolder(Context context) {
        String sdcardCachePath = getDiskCacheDir(context);
        String dir = sdcardCachePath + File.separator + BootConstants.CACHE_ROOT_DIR;
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return true;
    }
}
