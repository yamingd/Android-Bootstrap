package com.argo.sdk.cache;

import android.content.Context;
import android.os.Environment;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import timber.log.Timber;

/**
 * Created by user on 6/26/15.
 */
public class CacheProvider implements Closeable {

    public static CacheProvider instance = null;

    private final int appVersion = 1;
    private final int maxSize = 50 * 1024 * 1024;

    private File cacheDir;
    private File journalFile;
    private File journalBkpFile;
    private DiskLruCache diskLruCache;

    private Context context;

    public CacheProvider(Context context) {
        this.context = context;

        cacheDir = getDiskCacheDir(context, "com.inno");
        try {
            diskLruCache = DiskLruCache.open(cacheDir, appVersion, 1, maxSize);
        } catch (IOException e) {
            Timber.e(e, "初始化Cache Provider Error.");
        }

        instance = this;
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                && !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    @Override
    public void close() throws IOException {
        try {
            if (diskLruCache != null){
                diskLruCache.close();
            }
        } catch (IOException e) {
            Timber.e(e.getMessage(), e);
        }
    }

    public void flush(){
        try {
            diskLruCache.flush();
        } catch (IOException e) {
            Timber.e(e.getMessage(), e);
        }
    }

    private String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     *
     * 缓存数据
     * @param url
     * @param writer
     */
    public void put(String url, boolean flush, CacheWriter writer){
        String cacheKey = hashKeyForDisk(url);
        try {
            DiskLruCache.Editor editor = diskLruCache.edit(cacheKey);
            if (null != editor){
                boolean ok = writer.write(cacheKey, editor);
                if (ok){
                    editor.commit();
                }else{
                    editor.abort();
                }
            }
            if (flush) {
                diskLruCache.flush();
            }
        } catch (IOException e) {
            Timber.e(e.getMessage(), e);
        }
    }

    public void put(String url, CacheWriter writer){
        this.put(url, true, writer);
    }

    /**
     * 读取缓存文件流
     * @param url
     * @return
     */
    public InputStream get(String url){
        String cacheKey = hashKeyForDisk(url);
        try {
            DiskLruCache.Snapshot snapShot = diskLruCache.get(cacheKey);
            if (snapShot != null){
                InputStream is = snapShot.getInputStream(0);
                return is;
            }
        } catch (IOException e) {
            Timber.e(e.getMessage(), e);
        }

        return null;

    }

    /**
     *
     * @param url
     */
    public void remove(String url){

        String cacheKey = hashKeyForDisk(url);
        try {
            diskLruCache.remove(cacheKey);
        } catch (IOException e) {
            Timber.e(e.getMessage(), e);
        }

    }

}
