package com.argo.sdk.providers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import com.argo.sdk.event.NetworkConnectionStatusEvent;
import com.squareup.otto.Bus;

import java.io.Closeable;
import java.io.IOException;

import timber.log.Timber;

/**
 * Created by user on 6/19/15.
 */
public class NetworkStatusProvider extends BroadcastReceiver implements Closeable {

    public static NetworkStatusProvider instance = null;

    private ConnectivityManager connectivityManager = null;
    private Context context = null;
    private boolean networkAvailable = false;
    private boolean connectionAvailable = false;
    private boolean wifiAvailable = false;
    private Bus bus;

    public NetworkStatusProvider(Context context, ConnectivityManager connectivityManager, Bus bus){
        this.connectivityManager = connectivityManager;
        this.context = context;
        this.bus = bus;

        initNetworkMonitor();
        instance = this;
    }

    public boolean isConnectionAvailable() {
        return connectionAvailable;
    }

    public boolean isNetworkAvailable() {
        return networkAvailable;
    }

    public boolean isWifiAvailable() {
        return wifiAvailable;
    }

    /**
     * 去Wifi设置界面
     */
    public void gotoWifiServiceSettings() {
        final Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.context.startActivity(intent);
    }

    /**
     * http://blog.csdn.net/kesenhoo/article/details/7057448
     * 初始化网络状态监控
     */
    private void initNetworkMonitor(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); //为BroadcastReceiver指定action，即要监听的消息名字。
        this.context.registerReceiver(this, intentFilter);
    }

    /**
     *
     * @return
     */
    private void refreshNetworkStatus() {
        NetworkInfo mWifi = this.connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mMobile = this.connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (mWifi != null && mWifi.isAvailable()) {
            networkAvailable = true;
            wifiAvailable = true;
        }if (mMobile != null && mMobile.isAvailable()) {
            wifiAvailable = false;
            networkAvailable = true;
        }else {
            wifiAvailable = false;
            networkAvailable = false;
        }
    }

    /**
     *
     * @return
     */
    private void refreshConnectionStatus() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            connectionAvailable = true;
        } else {
            connectionAvailable = false;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean lastStatus = this.connectionAvailable;
        this.refreshNetworkStatus();
        this.refreshConnectionStatus();

        if (lastStatus && !this.connectionAvailable){
            Timber.e("Connection Lost");
            bus.post(new NetworkConnectionStatusEvent(false));
            return;
        }

        if (!lastStatus && this.connectionAvailable){
            Timber.e("Connection Done.");
            final NetworkConnectionStatusEvent event = new NetworkConnectionStatusEvent(true);
            event.setWifi(this.isWifiAvailable());
            bus.post(event);
        }
    }



    @Override
    public void close() throws IOException {
        this.context.unregisterReceiver(this);
    }
}
