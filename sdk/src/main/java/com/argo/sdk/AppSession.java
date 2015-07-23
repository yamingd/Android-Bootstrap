package com.argo.sdk;

import android.location.Location;

import com.argo.sdk.protobuf.PAppSession;

import java.io.Closeable;
import java.util.Map;

import javax.inject.Provider;

/**
 * Created by user on 6/25/15.
 */
public interface AppSession extends Closeable, Provider<PAppSession> {

    boolean isDebug();
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
     * @param location
     */
    void rememberLocation(Location location);

    /**
     *
     */
    void clear();

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
     *
     * @param key
     * @param defaultValue
     * @param <T>
     * @return
     */
    <T> T getConfigValue(String key, T defaultValue);
}
