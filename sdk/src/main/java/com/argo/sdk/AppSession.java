package com.argo.sdk;

import com.argo.sdk.protobuf.PAppSession;

import java.io.Closeable;
import java.util.Map;

import javax.inject.Provider;

/**
 * Created by user on 6/25/15.
 */
public interface AppSession extends Closeable, Provider<PAppSession> {

    /**
     *
     * @return
     */
    Map<String, String> getHttpHeader();
    /**
     *
     * @return
     */
    byte[] getSalt();

    /**
     *
     * @param userId
     * @param userName
     * @param realName
     * @param userKind
     * @param profileImageUrl
     */
    void remember(long userId, String userName, String realName, int userKind, String profileImageUrl, boolean demo);

    /**
     *
     */
    void clear();

    /**
     *
     */
    void save();

    /**
     *
     * @return
     */
    boolean isSignIn();

    /**
     *
     * @return
     */
    boolean isAnonymous();

    /**
     * 加密请求
     * @param url
     * @return
     */
    String signRequest(String url);

    /**
     *
     * @param key
     * @param defaultValue
     * @param <T>
     * @return
     */
    <T> T getConfigValue(String key, T defaultValue);

    String dump();
}
