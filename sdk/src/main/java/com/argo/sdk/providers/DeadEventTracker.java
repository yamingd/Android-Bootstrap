package com.argo.sdk.providers;

import com.squareup.otto.Bus;
import com.squareup.otto.DeadEvent;
import com.squareup.otto.Subscribe;

import timber.log.Timber;

/**
 * Created by user on 6/24/15.
 */
public class DeadEventTracker {

    private Bus bus;

    public DeadEventTracker(Bus bus){
        this.bus = bus;
        bus.register(this);
    }

    @Subscribe
    public void onDeadEvent(DeadEvent deadEvent){
        Timber.d("DeadEvent: %s, source: %s", deadEvent.event, deadEvent.source);
    }
}
