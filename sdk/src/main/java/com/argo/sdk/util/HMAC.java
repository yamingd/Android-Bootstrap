package com.argo.sdk.util;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import timber.log.Timber;

/**
 * Created by user on 11/18/14.
 */
public class HMAC {

    public static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final char[] hexDigits = "0123456789abcdef".toCharArray();

    public static String md5(String val){
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Timber.e(e, "md5 Not Found");
            return null;
        }
        md5.update(val.getBytes(UTF_8));
        byte[] m = md5.digest();//加密
        return getString(m);
    }

    public static String sha256(String val){
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("SHA256");
        } catch (NoSuchAlgorithmException e) {
            Timber.e(e, "SHA256 Not Found");
            return null;
        }
        md5.update(val.getBytes(UTF_8));
        byte[] m = md5.digest();//加密
        return getString(m);
    }

    private static String getString(byte[] bytes){
        StringBuilder sb = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            sb.append(hexDigits[(b >> 4) & 0xf]).append(hexDigits[b & 0xf]);
        }
        return sb.toString();
    }

    public static String md5(String msg, String key){
        return hmacDigest(msg, key, "MD5");
    }

    public static String sha256(String msg, String key){
        return hmacDigest(msg, key, "SHA256");
    }

    public static String hmacSHA256(String msg, String key){
        return hmacDigest(msg, key, "HmacSHA256");
    }

    public static String hmacDigest(String msg, String keyString, String algo) {
        String digest = null;
        try {
            SecretKeySpec key = new SecretKeySpec((keyString).getBytes(UTF_8), algo);
            Mac mac = Mac.getInstance(algo);
            mac.init(key);

            byte[] bytes = mac.doFinal(msg.getBytes(UTF_8));

            StringBuffer hash = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            digest = hash.toString();
        } catch (InvalidKeyException e) {
            Timber.e(e, e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Timber.e(e, e.getMessage());
        }
        return digest;
    }
}
