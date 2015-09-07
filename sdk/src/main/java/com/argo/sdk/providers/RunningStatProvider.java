package com.argo.sdk.providers;

import android.app.ActivityManager;
import android.content.Context;

import timber.log.Timber;

/**
 * Created by user on 9/3/15.
 */
public class RunningStatProvider {

    public static RunningStatProvider instance;

    private ActivityManager activityManager;
    private long lastAvailableMegs = -1l;

    public RunningStatProvider(Context context) {
        this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);;
        instance = this;
        printMemoryUsage("init");
    }

    public void printMemoryUsage(String tag){
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;
        //Percentage can be calculated for API 16+
        //long percentAvail = mi.availMem / mi.totalMem * 100;

        Timber.d("%s memory. at %s available %d mb", this, tag, availableMegs);
    }
}
