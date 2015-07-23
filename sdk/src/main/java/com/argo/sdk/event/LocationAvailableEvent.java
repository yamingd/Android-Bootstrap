package com.argo.sdk.event;


import android.location.Location;

/**
 * Created by user on 6/23/15.
 */
public class LocationAvailableEvent extends AppBaseEvent{

    private Location location;

    public LocationAvailableEvent(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
