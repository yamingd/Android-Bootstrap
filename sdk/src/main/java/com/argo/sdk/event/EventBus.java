package com.argo.sdk.event;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import timber.log.Timber;

/**
 * This message bus allows you to post a message from any thread and it will get handled and then
 * posted to the main thread for you.
 */
public class EventBus extends Bus
{
    public static EventBus instance = null;

    public EventBus()
    {
        super(ThreadEnforcer.MAIN, "EventBus");
        instance = this;
    }

    @Override
    public void post(final Object event)
    {
        Timber.d("Bus post event 0: %s", event);

        if (Looper.myLooper() != Looper.getMainLooper())
        {
            // We're not in the main loop, so we need to get into it.
            (new Handler(Looper.getMainLooper())).post(new Runnable()
            {
                @Override
                public void run()
                {
                    // We're now in the main loop, we can post now
                    Timber.d("Bus post event 2: %s", event);

                    EventBus.super.post(event);
                }
            });
        }
        else
        {
            Timber.d("Bus post event 1: %s", event);

            super.post(event);
        }
    }

    @Override
    public void unregister(final Object object)
    {
        //  Lots of edge cases with register/unregister that sometimes throw.
        try
        {
            super.unregister(object);
        }
        catch (IllegalArgumentException e)
        {
            // TODO: use Crashlytics unhandled exception logging
            Timber.e(e, e.getMessage());
        }
    }

}
