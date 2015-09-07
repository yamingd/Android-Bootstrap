package com.argo.sdk.providers;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.argo.sdk.event.LocationAvailableEvent;
import com.squareup.otto.Bus;

import java.io.Closeable;
import java.io.IOException;

import timber.log.Timber;

/**
 * Created by user on 6/19/15.
 */
public class LocationStatusProvider implements AMapLocationListener, Closeable {

    public static final int DEFAULT_TRY_MAX = 10;
    public static LocationStatusProvider instance = null;

    private Context context;
    public LocationManagerProxy mLocationManagerProxy;
    private Location lastLocation;

    private Bus bus;

    private int tryMax = DEFAULT_TRY_MAX;
    private int stage = -1;
    private long failureAt = 0;

    public LocationStatusProvider(Context context, Bus bus) {
        this.context = context;
        this.bus = bus;

        mLocationManagerProxy = LocationManagerProxy.getInstance(this.context);
        mLocationManagerProxy.setGpsEnable(false);

        instance = this;
    }

    public synchronized void start(){
        Timber.d("%s, start locating...", this);
        if (stage == 0){
            return;
        }

        stage = 0; // 定位中
        //此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        //注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
        //在定位结束后，在合适的生命周期调用destroy()方法
        //其中如果间隔时间为-1，则定位只定一次
        mLocationManagerProxy.requestLocationData(
                LocationProviderProxy.AMapNetwork, 60 * 1000, 15, this);
        Timber.d("%s, Location Client Start.", this);
    }

    public synchronized void stop(){
        mLocationManagerProxy.removeUpdates(this);
        Timber.d("%s, Location Client Stop.", this);
    }

    public void restart(){
        long ts = System.currentTimeMillis() - failureAt;
        ts = ts / 1000 / 60;
        if (ts >= 1){
            start();
        }
    }

    public boolean isFailure(){
        return stage == 2;
    }

    public Location getLastLocation(){
        return lastLocation;
    }

    public void gotoSettingActivity(){
        final Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void test(){

    }

    @Override
    public void close() throws IOException {
        if (mLocationManagerProxy != null) {
            mLocationManagerProxy.removeUpdates(this);
            mLocationManagerProxy.destroy();
        }
        mLocationManagerProxy = null;
    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

        if(aMapLocation != null && aMapLocation.getAMapException().getErrorCode() == 0){
            tryMax = DEFAULT_TRY_MAX;
            stage = 1;
            //获取位置信息
            this.lastLocation = aMapLocation;
            Timber.d("%s, location found: %s", this, aMapLocation);
            this.bus.post(new LocationAvailableEvent(aMapLocation));

        }else{
            tryMax -- ;
            if (tryMax == 0){
                stage = 2;
                failureAt = System.currentTimeMillis();
                stop();
            }
            Timber.e("%s, Location Error. code=%s, msg=%s",this,
                    aMapLocation.getAMapException().getErrorCode(),
                    aMapLocation.getAMapException().getErrorMessage());

        }

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Timber.d("%s, onStatusChanged, provider=%s, status=%s, extras=%s", this, provider, status, extras);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Timber.d("%s, onProviderEnabled, provider=%s", this, provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Timber.d("%s, onProviderDisabled, provider=%s",this, provider);
    }
}
