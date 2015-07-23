

package com.argo.sdk;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;

import com.argo.sdk.event.ApplicationTerminateEvent;
import com.argo.sdk.providers.DeadEventTracker;
import com.argo.sdk.providers.NetworkStatusProvider;
import com.squareup.otto.Bus;

import java.io.IOException;
import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Bootstrap application
 */
public abstract class BootstrapApplication extends Application {

    private static BootstrapApplication instance;

    /**
     * Create main application
     */
    public BootstrapApplication() {
    }

    /**
     * Create main application
     *
     * @param context
     */
    public BootstrapApplication(final Context context) {
        this();
        attachBaseContext(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        Timber.i("Thread: %s, Locale: %s", Thread.currentThread(), Locale.getDefault());

        initBeforeInject();

        //Injector.init(this.getComponentClass(), this);

        Timber.d("appSession: %s", this.getAppSession());

        onAfterInjection();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Timber.i("Application Terminate");
        try {
            getAppSession().close();
        } catch (IOException e) {
            Timber.e(e, "AppSession Close");
        }

        try {
            getNetworkStatusProvider().close();
        } catch (IOException e) {

        }

        getBus().post(new ApplicationTerminateEvent());
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Inject
    Bus bus;

    @Inject
    NetworkStatusProvider networkStatusProvider;

    @Inject
    DeadEventTracker deadEventTracker;

    /**
     * Create main application
     *
     * @param instrumentation
     */
    public BootstrapApplication(final Instrumentation instrumentation) {
        this();
        attachBaseContext(instrumentation.getTargetContext());
    }

    /**
     * 注入成功后，初始化代码
     */
    protected void onAfterInjection(){

    }

    /**
     * 在初始化Modules前执行的操作
     */
    protected void initBeforeInject(){

    }


    protected AppSession getAppSession(){
        return null;
    }

    protected Bus getBus(){
        return null;
    }

    protected NetworkStatusProvider getNetworkStatusProvider(){
        return null;
    }

    public static BootstrapApplication getInstance() {
        return instance;
    }
}
