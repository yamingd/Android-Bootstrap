package com.argo.sdk.event;

/**
 * Created by user on 6/29/15.
 */
public class NetworkConnectionStatusEvent extends AppBaseEvent {

    private boolean available = false;

    public NetworkConnectionStatusEvent(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }
}