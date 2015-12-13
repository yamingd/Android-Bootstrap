package com.argo.sdk.image;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.argo.sdk.util.SDCardUtils;

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

/**
 * 加载、保存
 * Created by user on 12/13/15.
 */
public class BitmapHandler {

    public static final String TYPE_NAME = ".jpeg";

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
     *
     * @param imageView
     */
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

    /**
     *
     * @param layout
     * @param includeBackground
     */
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

    /**
     *
     * @param file
     * @param bitmapOps
     * @return
     */
    public static Bitmap decodeBitmap(File file, BitmapFactory.Options bitmapOps) {
        return BitmapFactory.decodeFile(file.getPath(), bitmapOps);
    }

    /**
     *
     * @param inputStream
     * @param bitmapOps
     * @return
     */
    public static Bitmap decodeBitmap(InputStream inputStream, BitmapFactory.Options bitmapOps) {
        return BitmapFactory.decodeStream(inputStream, null, bitmapOps);
    }

    /**
     *
     * @param bytes
     * @param bitmapOps
     * @return
     */
    public static Bitmap decodeBitmap(byte[] bytes, BitmapFactory.Options bitmapOps) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bitmapOps);
    }

    /**
     *
     * @param reqWidth
     * @param reqHeight
     * @param width
     * @param height
     * @param options
     */
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

    /**
     *
     * @param file
     * @param is
     */
    public static void save(File file, InputStream is) {
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

    /**
     *
     * @param file
     * @param datas
     */
    public static void save(File file, byte[] datas) {
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
            return save(inputStream, TYPE_NAME);
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
                photoFile = save(context, bitmap);
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
                photoFile = save(inputStream, TYPE_NAME);
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

    /**
     * 将Bitmap转换成InputStream
     * @param bm
     * @return
     */
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

    /**
     * 将Bitmap转换成InputStream
     * @param bm
     * @param quality
     * @return
     */
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
    public static File getSavePath(){
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
    public static File save(final InputStream source, final String type){
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
    public static File save(final Context context, final Bitmap bitmap){
        InputStream source = bitmap2InputStream(bitmap);
        return save(source, TYPE_NAME);
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
}
