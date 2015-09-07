package com.argo.sdk.providers;

import android.content.Context;

import com.argo.sdk.AppSession;
import com.argo.sdk.BootConstants;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateConfig;

import java.util.Map;

import timber.log.Timber;

/**
 * Created by user on 6/22/15.
 */
public class UmengTrackProvider {

    public static UmengTrackProvider instance = null;

    private Context context;
    private AppSession appSession;
    private String appId;
    private String channel;
    private boolean enabled;

    public UmengTrackProvider(Context context, AppSession appSession) {
        this.context = context;
        this.appSession = appSession;
        this.init();
        instance = this;
    }

    private void init(){
//        enabled = appSession.getConfigValue("AppUMengEnable", false);
        enabled = true;
        if (enabled){
            appId = appSession.getConfigValue("AppUMengId", "");
            channel = appSession.getConfigValue("Channel", "");

            AnalyticsConfig.setAppkey(appId);
            AnalyticsConfig.setChannel(channel);

            MobclickAgent.setCatchUncaughtExceptions(true);
            MobclickAgent.setDebugMode(BootConstants.DEBUG);
            MobclickAgent.updateOnlineConfig(this.context);
        }

        Timber.i("Umeng init. enabled=%s, channel=%s", enabled, channel);
    }

    public void startAutoUpdate(Context view){
        UmengUpdateAgent.setUpdateAutoPopup(true);
        UmengUpdateAgent.setAppkey(appId);
        UmengUpdateAgent.setChannel(channel);
        UpdateConfig.setDebug(true);

        UmengUpdateAgent.update(view);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void resume(){
        if (this.enabled){
            MobclickAgent.onResume(this.context);
        }
    }

    public void pause(){
        if (this.enabled){
            MobclickAgent.onPause(this.context);
        }
    }

    public void pageStart(String tag){
        if (this.enabled){
            MobclickAgent.onPageStart(tag);
        }
    }

    public void pageEnd(String tag){
        if (this.enabled){
            MobclickAgent.onPageEnd(tag);
        }
    }

    public void log(String eventId){
        if (this.enabled) {
            MobclickAgent.onEvent(this.context, eventId);
        }
    }

    public void log(String eventId, Map<String, String> params){
        if (this.enabled) {
            MobclickAgent.onEvent(this.context, eventId, params);
        }
    }
}
