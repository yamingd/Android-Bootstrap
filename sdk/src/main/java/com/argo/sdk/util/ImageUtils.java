package com.argo.sdk.util;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.argo.sdk.ui.ImageRecyclable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import okio.BufferedSink;
import okio.Okio;
import timber.log.Timber;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Color.WHITE;
import static android.graphics.PorterDuff.Mode.DST_IN;

/**
 * Image utilities
 */
public final class ImageUtils {



    public static final String TYPE_NAME = ".jpeg";

    /**
     * This is a utility class.
     */
    private ImageUtils() {
        //never called
    }

    /**
     * Get a bitmap from the image path
     *
     * @param imagePath
     * @return bitmap or null if read fails
     */
    public static Bitmap getBitmap(final String imagePath) {
        return getBitmap(imagePath, 1);
    }

    /**
     * Get a bitmap from the image path
     *
     * @param imagePath
     * @param sampleSize
     * @return bitmap or null if read fails
     */
    public static Bitmap getBitmap(final String imagePath, final int sampleSize) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inSampleSize = sampleSize;

        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(imagePath, "r");
            return BitmapFactory.decodeFileDescriptor(file.getFD(), null,
                    options);
        } catch (IOException e) {
            Timber.e(e, "Could not get cached bitmap.");
            return null;
        } finally {
            if (file != null)
                try {
                    file.close();
                } catch (IOException e) {
                    Timber.d(e, "Could not get cached bitmap.");
                }
        }
    }

    /**
     *
     * @param resources
     * @param resId
     * @param width
     * @param height
     * @return
     */
    public static Bitmap getBitmap(final Resources resources, int resId, int width, int height){

        BitmapFactory.Options bitmapOps = new BitmapFactory.Options();
        bitmapOps.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resId, bitmapOps);

        calculateInSampleSize(width, height, bitmapOps.outWidth, bitmapOps.outHeight, bitmapOps);

        Timber.d("getBitmap, resId=%s, width=%s, height=%s, owidth=%s, oheight=%s sampleSize=%s",
                resId, width, height, bitmapOps.outWidth, bitmapOps.outHeight,  bitmapOps.inSampleSize);

        Bitmap bitmap = BitmapFactory.decodeResource(resources, resId, bitmapOps);
        return bitmap;
    }

    /**
     *
     * @param resources
     * @param resId
     * @return
     */
    public static Bitmap getBitmap(final Resources resources, int resId, int sampleSize){

        BitmapFactory.Options bitmapOps = new BitmapFactory.Options();
        bitmapOps.inJustDecodeBounds = false;
        bitmapOps.inSampleSize = sampleSize;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resId, bitmapOps);
        return bitmap;
    }

    /**
     *
     * inputStream = context.getContentResolver().openInputStream(uri);
     *
     * @param inputStream
     * @param sampleSize
     * @return
     */
    public static Bitmap getBitmap(final InputStream inputStream, final int sampleSize){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inSampleSize = sampleSize;
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        return bitmap;
    }

    /**
     * Get size of image
     *
     * @param imagePath
     * @return size
     */
    public static Point getSize(final String imagePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(imagePath, "r");
            BitmapFactory.decodeFileDescriptor(file.getFD(), null, options);
            return new Point(options.outWidth, options.outHeight);
        } catch (final IOException e) {
            Timber.e(e, "Could not get size.");
            return null;
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (final IOException e) {
                    Timber.d(e, "Could not get size.");
                }
            }
        }
    }

    /**
     * 获取本地图片
     * @param inputStream
     * @return
     * @throws FileNotFoundException
     */
    public static Point getSize(final InputStream inputStream) throws FileNotFoundException {

        //InputStream inputStream = context.getContentResolver().openInputStream(uri);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        try {
            BitmapFactory.decodeStream(inputStream, null, options);
            return new Point(options.outWidth, options.outHeight);
        } catch (final Exception e) {
            Timber.e(e, "Could not get size.");
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    Timber.d(e, "Could not get size.");
                }
            }
        }
    }

    /**
     * Get bitmap with maximum height or width
     *
     * @param imagePath
     * @param width
     * @param height
     * @return image
     */
    public static Bitmap getBitmap(final String imagePath, final int width, final int height) {
        return getBitmap(new File(imagePath), width, height);
    }

    /**
     * Get bitmap with maximum height or width
     *
     * @param image
     * @param width
     * @param height
     * @return image
     */
    public static Bitmap getBitmap(final File image, final int width, final int height) {

        BitmapFactory.Options bitmapOps = new BitmapFactory.Options();
        bitmapOps.inJustDecodeBounds = true;
        decodeBitmap(image, bitmapOps);

        calculateInSampleSize(width, height, bitmapOps.outWidth, bitmapOps.outHeight, bitmapOps);

        return getBitmap(image.getAbsolutePath(), bitmapOps.inSampleSize);
    }

    /**
     * Get a bitmap from the image file
     *
     * @param image
     * @return bitmap or null if read fails
     */
    public static Bitmap getBitmap(final File image) {
        return getBitmap(image.getAbsolutePath());
    }

    /**
     * Load a {@link Bitmap} from the given path and set it on the given
     * {@link android.widget.ImageView}
     *
     * @param imagePath
     * @param view
     */
    public static void setImage(final String imagePath, final ImageView view) {
        setImage(new File(imagePath), view);
    }

    /**
     * Load a {@link Bitmap} from the given {@link File} and set it on the given
     * {@link ImageView}
     *
     * @param image
     * @param view
     */
    public static void setImage(final File image, final ImageView view) {
        final Bitmap bitmap = getBitmap(image);
        if (bitmap != null) {
            view.setImageBitmap(bitmap);
        }
    }

    /**
     * Round the corners of a {@link Bitmap}
     *
     * @param source
     * @param radius
     * @return rounded corner bitmap
     */
    public static Bitmap roundCorners(final Bitmap source, final float radius) {
        final int width = source.getWidth();
        final int height = source.getHeight();

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(WHITE);

        final Bitmap clipped = Bitmap.createBitmap(width, height, ARGB_8888);
        Canvas canvas = new Canvas(clipped);
        canvas.drawRoundRect(new RectF(0, 0, width, height), radius, radius,
                paint);
        paint.setXfermode(new PorterDuffXfermode(DST_IN));

        final Bitmap rounded = Bitmap.createBitmap(width, height, ARGB_8888);
        canvas = new Canvas(rounded);
        canvas.drawBitmap(source, 0, 0, null);
        canvas.drawBitmap(clipped, 0, 0, paint);

        source.recycle();
        clipped.recycle();

        return rounded;
    }

    // 将Bitmap转换成InputStream
    public static InputStream bitmap2InputStream(Bitmap bm) {
        //final int byteCount = bm.getByteCount();
        //Timber.d("bitmap save0, length=%s bytes", byteCount);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        final byte[] buf = baos.toByteArray();
        InputStream is = new ByteArrayInputStream(buf);
        //Timber.d("bitmap save1, length=%s bytes", buf.length);
        return is;
    }

    // 将Bitmap转换成InputStream
    public static InputStream bitmap2InputStream(Bitmap bm, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;
    }

    /**
     *
     * @return
     */
    public static File getImageSavePath(){
        String name = System.currentTimeMillis() / 1000 + TYPE_NAME;
        if (IMAGE_ROOT_FILE == null){
            IMAGE_ROOT_FILE = new File(SDCardUtils.ROOT_FOLDER, "image");
            if (!IMAGE_ROOT_FILE.exists()){
                IMAGE_ROOT_FILE.mkdirs();
            }
        }
        File file = new File(IMAGE_ROOT_FILE, name);
        return file;
    }

    static File IMAGE_ROOT_FILE = null;
    /**
     * 保存Bitmap到临时目录
     * @param source
     * @return
     */
    public static File saveImageTemp(final InputStream source, final String type){
        String name = System.currentTimeMillis() / 1000 + type;
        if (IMAGE_ROOT_FILE == null){
            IMAGE_ROOT_FILE = new File(SDCardUtils.ROOT_FOLDER, "image");
            if (!IMAGE_ROOT_FILE.exists()){
                IMAGE_ROOT_FILE.mkdirs();
            }
        }
        File file = new File(IMAGE_ROOT_FILE, name);
        BufferedSink sink = null;
        try {
            sink = Okio.buffer(Okio.sink(file));
            long size = sink.writeAll(Okio.source(source));
            //Timber.d("file size: %d bytes, %s", size, file);
            return file;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            Timber.e(e, "saveImageTemp");
            return null;
        }finally {
            if (sink != null){
                try {
                    sink.flush();
                    sink.close();
                } catch (IOException e) {
                    Timber.e(e, "saveImageTemp");
                }
            }
        }
    }

    /**
     * 保存Bitmap到临时目录
     * @param context
     * @param bitmap
     * @return
     */
    public static File saveImageTemp(final Context context, final Bitmap bitmap){
        InputStream source = bitmap2InputStream(bitmap);
        return saveImageTemp(source, TYPE_NAME);
    }

    public static final int compressMaxSizeOut = 500 * 1024; // 500k
    public static final int compressStep = 5;
    public static final int compressGag = 50 * 1024;

    /**
     * 最大大小为500K
     * @param file
     * @param width
     * @param quality
     * @return
     */
    public static Bitmap compress(final File file, int width, int quality){
        return compress(file, width, quality, compressMaxSizeOut);
    }

    /**
     * 压缩20%
     * 最大大小为500K
     * @param file
     * @param width
     * @return
     */
    public static Bitmap compress(final File file, int width){
        return compress(file, width, 80, compressMaxSizeOut);
    }

    /**
     * 压缩图片
     * @param file
     * @param maxSize
     * @param quality
     * @return
     */
    public static Bitmap compress(final File file, int width, int quality, int maxSize){
        int osize = (int)(file.length());
        if (osize <= maxSize){
            return null;
        }

        if (osize - maxSize <= compressGag || maxSize - osize <= compressGag){
            //在50K范围内可以不压缩
            return null;
        }

        Timber.d("ImageUtils compress file: %s", file);
        BitmapFactory.Options bitmapOps = new BitmapFactory.Options();
        bitmapOps.inJustDecodeBounds = true;
        decodeBitmap(file, bitmapOps);

        Timber.d("ImageUtils width: %d, height: %d", bitmapOps.outWidth, bitmapOps.outHeight);

        ByteArrayOutputStream os = new ByteArrayOutputStream(osize);
        try {

            calculateInSampleSize(width, width,
                    bitmapOps.outWidth, bitmapOps.outHeight,
                    bitmapOps);

            Bitmap bitmap = decodeBitmap(file, bitmapOps);
            if (bitmap == null) {
                return null;
            }

            Timber.d("ImageUtils bitmap size: %d bytes", bitmap.getByteCount());
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
            bitmap.recycle();

            final byte[] bytes = os.toByteArray();
            final int length = bytes.length;
            final int diff = length >= maxSize ? length - maxSize : maxSize - length;
            if (diff > compressGag) {
                os.close();
                return compress(file, maxSize, quality - compressStep, maxSize);
            } else {
                Timber.d("ImageUtils decode out size: %d bytes", length);
                Bitmap ret = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes));
                return ret;
            }

        } catch (OutOfMemoryError ignore) {
            return compress(file, maxSize, quality - compressStep, maxSize);
        } catch (IOException ignore) {
            Timber.d(ignore, "ImageUtils compress ignore error");
            return compress(file, maxSize, quality - compressStep, maxSize);
        } finally {
            try {
                os.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * 压缩图片
     * @param file
     * @param maxSize
     * @param quality
     * @return
     */
    public static File compressFile(final File file, int width, int height, int quality, int maxSize, boolean verbose){
        int osize = (int)(file.length());
        if (osize <= maxSize){
            return file;
        }

        int diff = osize > maxSize ? osize - maxSize : maxSize - osize;
        if (diff <= compressGag){
            //在50K范围内可以不压缩
            return file;
        }

        if (verbose) {
            Timber.d("ImageUtils compress file: %s", file);
        }
        BitmapFactory.Options bitmapOps = new BitmapFactory.Options();
        bitmapOps.inJustDecodeBounds = true;
        decodeBitmap(file, bitmapOps);

        if (verbose) {
            Timber.d("ImageUtils width: %d, height: %d", bitmapOps.outWidth, bitmapOps.outHeight);
        }

        Bitmap bitmap = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream(osize);
        try {

            calculateInSampleSize(width, height,
                    bitmapOps.outWidth, bitmapOps.outHeight,
                    bitmapOps);

            File outFile = null;

            bitmap = decodeBitmap(file, bitmapOps);
            if (bitmap == null) {
                Timber.e("ImageUtils compressFile, bitmap is NULL");
                return null;
            }

            while (outFile == null && quality > 0) {

                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
                //bitmap.recycle();

                final int length = os.size();
                diff = length - maxSize;

                if (verbose) {
                    Timber.d("ImageUtils bitmap size: %d bytes, quality: %d, diff: %d bytes", length, quality, diff);
                }

                if (diff > compressGag) {
                    os.reset();
                    quality = quality - compressStep;
                } else {
                    if (verbose) {
                        Timber.d("ImageUtils decode out size: %d bytes", length);
                    }
                    outFile = new File(file.getAbsolutePath() + ".500" + TYPE_NAME);
                    bytesToFile(outFile, os.toByteArray());
                }
            }

            return outFile;

        } catch (OutOfMemoryError ignore) {
            return compressFile(file, width, height, quality - compressStep, maxSize, verbose);
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

    public static byte[] getBytes(InputStream is) throws IOException {

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
        return buf;
    }

    /**
     * 压缩图片
     * @param uri
     * @param maxSize
     * @param quality
     * @return
     */
    public static File compressUri(final  Context context, final Uri uri, int width, int height, int quality, int maxSize, boolean verbose) throws IOException {
        File outFile = null;

        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        byte[] buffer = getBytes(inputStream);

        int size = buffer.length;

        if (size <= maxSize){
            InputStream is = new ByteArrayInputStream(buffer);
            outFile = getImageSavePath();
            inputStreamToFile(outFile, is);
            return outFile;
        }

        BitmapFactory.Options bitmapOps = new BitmapFactory.Options();
        bitmapOps.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(buffer, 0, size, bitmapOps);

        if (verbose) {
            Timber.d("ImageUtils size:%d width: %d, height: %d", size, bitmapOps.outWidth, bitmapOps.outHeight);
        }

        int diff = 0;
        Bitmap bitmap = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream(size);
        try {

            calculateInSampleSize(width, height,
                    bitmapOps.outWidth, bitmapOps.outHeight,
                    bitmapOps);

            bitmap = BitmapFactory.decodeByteArray(buffer, 0, size, bitmapOps);
            if (bitmap == null) {
                Timber.e("ImageUtils compressStream, bitmap is NULL, %s", outFile);
                return null;
            }

            boolean done = false;
            while (!done && quality > 0) {

                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
                //bitmap.recycle();

                final int length = os.size();
                diff = length - maxSize;

                if (verbose) {
                    Timber.d("ImageUtils bitmap size: %d bytes, quality: %d, diff: %d bytes", length, quality, diff);
                }

                if (diff > compressGag) {
                    os.reset();
                    quality = quality - compressStep;
                } else {
                    if (verbose) {
                        Timber.d("ImageUtils decode out size: %d bytes", length);
                    }
                    outFile = getImageSavePath();
                    bytesToFile(outFile, os.toByteArray());
                    done = true;
                }
            }

            return outFile;

        } catch (OutOfMemoryError ignore) {
            return compressUri(context, uri, width, height, quality - compressStep, maxSize, verbose);
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

    static Bitmap decodeBitmap(File file, BitmapFactory.Options bitmapOps) {
        return BitmapFactory.decodeFile(file.getPath(), bitmapOps);
    }

    static Bitmap decodeBitmap(InputStream inputStream, BitmapFactory.Options bitmapOps) {
        return BitmapFactory.decodeStream(inputStream, null, bitmapOps);
    }

    static Bitmap decodeBitmap(byte[] bytes, BitmapFactory.Options bitmapOps) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bitmapOps);
    }

    public static void calculateInSampleSize(int reqWidth, int reqHeight, int width, int height,
                                      BitmapFactory.Options options) {
        int sampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio;
            final int widthRatio;
            if (reqHeight == 0) {
                sampleSize = Math.round((float) width / (float) reqWidth + 0.5f);
            } else if (reqWidth == 0) {
                sampleSize = Math.round((float) height / (float) reqHeight + 0.5f);
            } else {
                heightRatio = Math.round((float) height / (float) reqHeight + 0.5f);
                widthRatio = Math.round((float) width / (float) reqWidth + 0.5f);
                sampleSize = Math.max(heightRatio, widthRatio);
            }
        }
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
    }

    public static void inputStreamToFile(File file, InputStream is) {
        try {
            OutputStream os = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
            os.flush();
            os.close();
        } catch (IOException e) {
            Timber.e(e, "inputStreamToFile Error. %s", file);
        } finally {
            try {
                is.close();
            } catch (IOException ignore) {
            }
        }
    }

    public static void bytesToFile(File file, byte[] datas) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            return;
        }

        try {
            os.write(datas);
            os.flush();
        } catch (IOException e) {
            Timber.e(e, "inputStreamToFile Error. %s", file);
        } finally {
            try {
                os.close();
            } catch (IOException ignore) {
            }
        }
    }

    //We just use 1/5 of the application's memory space, return in kb
    static int getAvailableMemorySize(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int ret = -1;
        boolean largeHeap = false;
        if (Build.VERSION.SDK_INT >= 11) {
            largeHeap = (context.getApplicationInfo().flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0;
            if (largeHeap)
                ret = am.getLargeMemoryClass() * 1024;
        }
        if (!largeHeap || Build.VERSION.SDK_INT < 11) {
            ret = am.getMemoryClass() * 1024;
        }
        return ret / 5;
    }

    static boolean checkMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    /**
     * 解析图片的真实路径
     * @param context
     * @param uri
     * @return
     */
    public static File resolveImagePath(Context context, Uri uri) {
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            return saveImageTemp(inputStream, TYPE_NAME);
        } catch (FileNotFoundException e) {
            Timber.e(e, "placeImage Error. %s", uri);
            return null;
        }finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Timber.e(e, "placeImage Error. %s", uri);
                }
            }
        }
    }

    /**
     * 显示选择的图片
     * @param context
     * @param data
     * @param imageView
     * @return
     */
    public static File placeImage(Context context, Intent data, ImageView imageView, int maxWidth) {
        Uri uri = data.getData();
        Bitmap bitmap = null;
        File photoFile = null;
        if (null == uri) {
            Timber.d("showHomeworkImage from bundle.");
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                bitmap = (Bitmap) bundle.get("data"); //get bitmap
                photoFile = saveImageTemp(context, bitmap);
                bitmap.recycle();
                bitmap = getBitmap(photoFile, maxWidth, maxWidth);
            }else{
                return null;
            }
        } else {

            Timber.d("uri=%s", uri.toString());
            InputStream inputStream = null;
            try {
                inputStream = context.getContentResolver().openInputStream(uri);
                photoFile = saveImageTemp(inputStream, TYPE_NAME);
                bitmap = getBitmap(photoFile, maxWidth, maxWidth);
            } catch (FileNotFoundException e) {
                Timber.e(e, "placeImage Error. %s", uri);
                return null;
            }finally {
                if (inputStream != null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Timber.e(e, "placeImage Error. %s", uri);
                    }
                }
            }
        }

        recycle(imageView);
        imageView.setImageBitmap(bitmap);

        return photoFile;
    }

    public static void recycle(ImageView imageView){
        if (imageView == null){
            return;
        }

        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            bitmap.recycle();
        }
    }

    public static void recycleAll(ViewGroup layout, boolean includeBackground) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            //获得该布局的所有子布局
            View subView = layout.getChildAt(i);
            //判断子布局属性，如果还是ViewGroup类型，递归回收
            if (subView instanceof ViewGroup) {
                //递归回收
                recycleAll((ViewGroup)subView, includeBackground);
            } else {
                //是Imageview的子例
                if (subView instanceof ImageRecyclable) {
                    //回收占用的Bitmap
                    recycle((ImageView) subView);
                    //如果flagWithBackgroud为true,则同时回收背景图
                    if (includeBackground) {
                        recycle((ImageView)subView);
                    }
                }
            }
        }
    }

}

