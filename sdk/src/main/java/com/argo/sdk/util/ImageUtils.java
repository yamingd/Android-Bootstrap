package com.argo.sdk.util;


import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.ImageView;

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

    public static final String JPEG = ".jpeg";

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
     * Get bitmap with maximum height or width
     *
     * @param imagePath
     * @param width
     * @param height
     * @return image
     */
    public static Bitmap getBitmap(final String imagePath, final int width, final int height) {
        final Point size = getSize(imagePath);
        int currWidth = size.x;
        int currHeight = size.y;

        int scale = 1;
        while (currWidth >= width || currHeight >= height) {
            currWidth /= 2;
            currHeight /= 2;
            scale *= 2;
        }

        return getBitmap(imagePath, scale);
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
        return getBitmap(image.getAbsolutePath(), width, height);
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;
    }

    // 将Bitmap转换成InputStream
    public static InputStream bitmap2InputStream(Bitmap bm, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, quality, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;
    }

    static File IMAGE_ROOT_FILE = null;
    /**
     * 保存Bitmap到临时目录
     * @param source
     * @return
     */
    public static File saveImageTemp(final Context context, final InputStream source, final String type){
        String name = System.currentTimeMillis() / 1000 + type;
        if (IMAGE_ROOT_FILE == null){
            IMAGE_ROOT_FILE = new File(context.getFilesDir(), "image");
        }
        File file = new File(IMAGE_ROOT_FILE, name);
        BufferedSink sink = null;
        try {
            sink = Okio.buffer(Okio.sink(file));
            sink.writeAll(Okio.source(source));
            Timber.d("file size: %d kb", file.length() / 1024);
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
        return saveImageTemp(context, source, JPEG);
    }

    public static final int compressMaxSizeOut = 500; // 500k
    public static final int compressStep = 5;

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

        BitmapFactory.Options bitmapOps = new BitmapFactory.Options();
        bitmapOps.inJustDecodeBounds = true;
        decodeBitmap(file, bitmapOps);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {

            calculateInSampleSize(width, width,
                    bitmapOps.outWidth, bitmapOps.outHeight,
                    bitmapOps);

            Bitmap bitmap = decodeBitmap(file, bitmapOps);
            if (bitmap == null) {
                return null;
            }

            Timber.d("bitmap size: %d kb", bitmap.getByteCount() / 1024);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
            bitmap.recycle();

            if (os.toByteArray().length / 1024 > maxSize) {
                os.close();
                return compress(file, maxSize, quality - compressStep, maxSize);
            } else {
                Timber.d("decode out size: %d kb", os.toByteArray().length / 1024);
                Bitmap ret = BitmapFactory.decodeStream(new ByteArrayInputStream(os.toByteArray()));
                return ret;
            }

        } catch (OutOfMemoryError ignore) {
            return compress(file, maxSize, quality - compressStep, maxSize);
        } catch (IOException ignore) {
            Timber.d(ignore, "compress ignore error");
            return compress(file, maxSize, quality - compressStep, maxSize);
        } finally {
            try {
                os.close();
            } catch (IOException ignore) {
            }
        }
    }

    static Bitmap decodeBitmap(File file, BitmapFactory.Options bitmapOps) {
        return BitmapFactory.decodeFile(file.getPath(), bitmapOps);
    }

    static void calculateInSampleSize(int reqWidth, int reqHeight, int width, int height,
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

    static void inputStreamToFile(File file, InputStream is) {
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
    public static String resolveImagePath(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        String photoPath = null;
        if (cursor != null) {
            cursor.moveToFirst();
            photoPath = cursor.getString(1); // 图片文件路径
        }
        return photoPath;
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
            }else{
                return null;
            }
        } else {
            Timber.d("uri=%s", uri.toString());
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                photoFile = new File(cursor.getString(1)); // 图片文件路径
            }else{
                return null;
            }
        }

        imageView.destroyDrawingCache();

        /* 将Bitmap设定到ImageView */
        bitmap = getBitmap(photoFile, maxWidth, maxWidth);
        imageView.setImageBitmap(bitmap);

        return photoFile;
    }
}

