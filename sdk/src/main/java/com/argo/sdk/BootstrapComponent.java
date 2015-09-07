package com.argo.sdk;

import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.view.inputmethod.InputMethodManager;

import com.argo.sdk.providers.DeadEventTracker;
import com.argo.sdk.providers.NetworkStatusProvider;
import com.argo.sdk.cache.CacheProvider;
import com.argo.sdk.providers.LocationStatusProvider;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.Component;

/**
 *
 * http://stackoverflow.com/questions/29587130/dagger-2-subcomponents-vs-component-dependencies
 *
 * Created by user on 7/11/15.
 */
@Singleton
@Component(modules = {
        AndroidModule.class,
        BootstrapModule.class
})
public interface BootstrapComponent {

    Bus bus();

    Context context();
    PackageInfo packageInfo();
    TelephonyManager telephonyManager();
    InputMethodManager inputMethodManager();
    ApplicationInfo applicationInfo();
    AccountManager accountManager();
    ClassLoader classLoader();
    NotificationManager notificationManager();
    ConnectivityManager connectivityManager();
    LocationManager locationManager();
    ActivityManager activityManager();
    WifiManager wifiManager();

    DeadEventTracker deadEventTracker();
    CacheProvider cacheProvider();
    NetworkStatusProvider networkStatusProvider();
    LocationStatusProvider locationStatusProvider();
    FlashBucket flashBucket();
    Picasso picasso();
}
