package com.argo.sdk;

import android.content.Context;
import android.net.ConnectivityManager;

import com.argo.sdk.event.EventBus;
import com.argo.sdk.providers.DeadEventTracker;
import com.argo.sdk.providers.NetworkStatusProvider;
import com.argo.sdk.cache.CacheProvider;
import com.argo.sdk.providers.LocationStatusProvider;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for setting up provides statements.
 * Register all of your entry points below.
 */
@Module
public class BootstrapModule {

    @Singleton
    @Provides
    Bus provideOttoBus() {
        return new EventBus();
    }

    @Singleton
    @Provides
    DeadEventTracker provideDeadEventTracker(Bus bus){
        return new DeadEventTracker(bus);
    }

    @Singleton
    @Provides
    CacheProvider provideCacheProvider(final Context context){
        return new CacheProvider(context);
    }

    @Singleton
    @Provides
    NetworkStatusProvider provideNetworkStatusProvider(final Context context, ConnectivityManager connectivityManager, Bus bus){
        return new NetworkStatusProvider(context, connectivityManager, bus);
    }

    @Singleton
    @Provides
    LocationStatusProvider provideLocationStatusProvider(final Context context, Bus bus){
        return new LocationStatusProvider(context, bus);
    }

    @Singleton
    @Provides
    Picasso providePicasso(final Context context){
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.listener(new PicassoLoadListener());
        Picasso picasso =  builder.build();
        Picasso.setSingletonInstance(picasso);
        return picasso;
    }

    @Singleton
    @Provides
    FlashBucket provideFlashBucket(final Context context){
        return new FlashBucket(context);
    }
}
