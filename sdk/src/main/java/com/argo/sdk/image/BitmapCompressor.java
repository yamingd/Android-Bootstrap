package com.argo.sdk.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

/**
 * 压缩
 * Created by user on 12/13/15.
 */
public class BitmapCompressor {

    public static final int compressMaxSizeOut = 500 * 1024; // 500k
    public static final int compressStep = 5;
    public static final int compressGag = 50 * 1024;

    public final static int MAX_HEIGHT = 1600;
    public final static int MAX_WIDTH = 2560;

    /**
     *
     */
    public interface OnBitmapCompressorListener{
        /**
         *
         */
        void onBitmapCompressStart(int quality);
        /**
         *
         */
        void onBitmapCompressing(int quality);
        /**
         *
         */
        void onBitmapCompressDone(int quality);

        /**
         *
         * @param exception
         */
        void onBitmapCompressError(Exception exception);
    }

    public static class Options{
        private boolean verbose;
        private File sourceFile;
        private Uri sourceUri;
        private Bitmap sourceBitmap;
        private int width;
        private int height;
        private int quality;
        private int maxSize;

        public Options() {
            verbose = false;
            width = MAX_WIDTH;
            height = MAX_HEIGHT;
            maxSize = compressMaxSizeOut;
            quality = 100;
        }

        public boolean isVerbose() {
            return verbose;
        }

        /**
         * 是否打印详细日志
         * @param verbose
         * @return
         */
        public Options setVerbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        public File getSourceFile() {
            return sourceFile;
        }

        /**
         * 图片源
         * @param sourceFile
         * @return
         */
        public Options setSourceFile(File sourceFile) {
            this.sourceFile = sourceFile;
            return this;
        }

        public Bitmap getSourceBitmap() {
            return sourceBitmap;
        }

        /**
         * 源图
         * @param sourceBitmap
         * @return
         */
        public Options setSourceBitmap(Bitmap sourceBitmap) {
            this.sourceBitmap = sourceBitmap;
            return this;
        }

        public Uri getSourceUri() {
            return sourceUri;
        }

        /**
         * 图片源
         * @param sourceUri
         * @return
         */
        public Options setSourceUri(Uri sourceUri) {
            this.sourceUri = sourceUri;
            return this;
        }

        public int getWidth() {
            return width;
        }

        /**
         * 默认为 MAX_WIDTH
         * @param width
         * @return
         */
        public Options setWidth(int width) {
            this.width = width;
            return this;
        }

        public int getHeight() {
            return height;
        }

        /**
         * 默认为 MAX_HEIGHT
         * @param height
         * @return
         */
        public Options setHeight(int height) {
            this.height = height;
            return this;
        }

        public int getQuality() {
            return quality;
        }

        /**
         * 默认为100
         * @param quality
         * @return
         */
        public Options setQuality(int quality) {
            this.quality = quality;
            return this;
        }

        public int getMaxSize() {
            return maxSize;
        }

        /**
         * 单位为KB
         * @param maxSize
         * @return
         */
        public Options setMaxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }
    }

    /**
     * 压缩图片
     * @param options
     * @return
     */
    public static File compressFile(Options options, OnBitmapCompressorListener listener){
        int osize = (int)(options.getSourceFile().length());
        if (osize <= options.getMaxSize()){
            return options.getSourceFile();
        }

        int diff = osize > options.getMaxSize() ? osize - options.getMaxSize() : options.getMaxSize() - osize;
        if (diff <= compressGag){
            //在50K范围内可以不压缩
            return options.getSourceFile();
        }

        if (options.isVerbose()) {
            Timber.d("ImageUtils compress file: %s", options.getSourceFile());
        }

        BitmapFactory.Options bitmapOps = new BitmapFactory.Options();
        bitmapOps.inJustDecodeBounds = true;
        BitmapHandler.decodeBitmap(options.getSourceFile(), bitmapOps);

        if (options.isVerbose()) {
            Timber.d("ImageUtils width: %d, height: %d", bitmapOps.outWidth, bitmapOps.outHeight);
        }

        Bitmap bitmap = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream(osize);
        try {

            BitmapHandler.calculateInSampleSize(options.getWidth(), options.getHeight(),
                    bitmapOps.outWidth, bitmapOps.outHeight,
                    bitmapOps);

            File outFile = null;

            bitmap = BitmapHandler.decodeBitmap(options.getSourceFile(), bitmapOps);
            if (bitmap == null) {
                Timber.e("ImageUtils compressFile, bitmap is NULL");
                listener.onBitmapCompressError(new Exception("Can't decode bitmap from: " + options.getSourceFile() + ", with options: " + bitmapOps));
                return null;
            }

            if (listener != null) {
                listener.onBitmapCompressStart(options.getQuality());
            }

            while (outFile == null && options.getQuality() > 0) {

                bitmap.compress(Bitmap.CompressFormat.JPEG, options.getQuality(), os);

                final int length = os.size();
                diff = length - options.getMaxSize();

                if (options.isVerbose()) {
                    Timber.d("ImageUtils bitmap size: %d bytes, quality: %d, diff: %d bytes", length, options.getQuality(), diff);
                }

                if (diff > compressGag) {
                    os.reset();
                    options.setQuality(options.getQuality() - compressStep);
                    if (listener != null) {
                        listener.onBitmapCompressing(options.getQuality());
                    }
                } else {
                    if (options.isVerbose()) {
                        Timber.d("ImageUtils decode out size: %d bytes", length);
                    }
                    outFile = new File(options.getSourceFile().getAbsolutePath() + ".500" + BitmapHandler.TYPE_NAME);
                    BitmapHandler.save(outFile, os.toByteArray());
                    if (listener != null) {
                        listener.onBitmapCompressDone(options.getQuality());
                    }
                }
            }

            return outFile;

        } catch (OutOfMemoryError ignore) {
            options.setQuality(options.getQuality() - compressStep);
            return compressFile(options, listener);
        } finally {
            if (bitmap != null){
                bitmap.recycle();
            }
            try {
                os.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * 流大小
     * @param is
     * @return
     * @throws IOException
     */
    public static int getSize(InputStream is) throws IOException {

        int len;
        int size = 1024;
        byte[] buf;

        if (is instanceof ByteArrayInputStream) {
            size = is.available();
            buf = new byte[size];
            len = is.read(buf, 0, size);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buf = new byte[size];
            while ((len = is.read(buf, 0, size)) != -1)
                bos.write(buf, 0, len);
            buf = bos.toByteArray();
        }

        return buf.length;
    }

    /**
     * 压缩图片
     * @param context
     * @param options
     * @return
     */
    public static File compressUri(final Context context, final Options options, final OnBitmapCompressorListener listener) throws IOException {
        File outFile = null;

        InputStream inputStream = getInputStream(context, options);
        int size = getSize(inputStream);
        Timber.d("compress. uri=%s, size=%s, inputStream=%s", options.getSourceUri(), size, inputStream != null);
        if (size <= options.getMaxSize()){
            inputStream = getInputStream(context, options);
            outFile = BitmapHandler.getSavePath();
            BitmapHandler.save(outFile, inputStream);
            return outFile;
        }

        inputStream = getInputStream(context, options);

        BitmapFactory.Options bitmapOps = new BitmapFactory.Options();
        bitmapOps.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(inputStream, null, bitmapOps);

        if (options.isVerbose()) {
            Timber.d("ImageUtils size:%d width: %d, height: %d", size, bitmapOps.outWidth, bitmapOps.outHeight);
        }

        int diff = 0;
        Bitmap bitmap = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream(size);
        try {

            BitmapHandler.calculateInSampleSize(options.getWidth(), options.getHeight(),
                    bitmapOps.outWidth, bitmapOps.outHeight,
                    bitmapOps);

            inputStream = getInputStream(context, options);
            bitmap = BitmapFactory.decodeStream(inputStream, null, bitmapOps);
            if (bitmap == null) {
                Timber.e("ImageUtils compressStream, bitmap is NULL, %s", outFile);
                if (null != listener){
                    listener.onBitmapCompressError(new Exception("Can't decode bitmap from. " + options.getSourceUri() + ", options:" + bitmapOps));
                }
                return null;
            }

            if (null != listener){
                listener.onBitmapCompressStart(options.getQuality());
            }

            boolean done = false;
            while (!done && options.getQuality() > 0) {

                bitmap.compress(Bitmap.CompressFormat.JPEG, options.getQuality(), os);
                //bitmap.recycle();

                final int length = os.size();
                diff = length - options.getMaxSize();

                if (options.isVerbose()) {
                    Timber.d("ImageUtils bitmap size: %d bytes, quality: %d, diff: %d bytes", length, options.getQuality(), diff);
                }

                if (diff > compressGag) {
                    options.setQuality(options.getQuality() - compressGag);
                    if (null != listener){
                        listener.onBitmapCompressing(options.getQuality());
                    }
                    if (options.getQuality() > 0){
                        os.reset();
                        continue;
                    }
                }

                if (options.isVerbose()) {
                    Timber.d("ImageUtils decode out size: %d bytes", length);
                }
                outFile = BitmapHandler.getSavePath();
                BitmapHandler.save(outFile, os.toByteArray());
                done = true;
                if (null != listener){
                    listener.onBitmapCompressDone(options.getQuality());
                }
            }

            return outFile;

        } catch (OutOfMemoryError ignore) {
            options.setQuality(options.getQuality() - compressGag);
            return compressUri(context, options, listener);
        } finally {
            if (bitmap != null){
                bitmap.recycle();
                bitmap = null;
            }
            if (inputStream != null){
                try {
                    inputStream.close();
                    inputStream = null;
                } catch (IOException e) {

                }
            }
            try {
                os.close();
                os = null;
            } catch (IOException ignore) {
            }
        }
    }

    /**
     *
     * @param context
     * @param options
     * @return
     * @throws FileNotFoundException
     */
    private static InputStream getInputStream(Context context, Options options) throws FileNotFoundException {
        return context.getContentResolver().openInputStream(options.getSourceUri());
    }

}
