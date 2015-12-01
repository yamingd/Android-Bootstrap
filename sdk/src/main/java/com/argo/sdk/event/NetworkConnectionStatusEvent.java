package com.argo.sdk.event;

/**
 * Created by user on 6/29/15.
 */
public class NetworkConnectionStatusEvent extends AppBaseEvent {

    private boolean available = false;
    private boolean wifi = false;

    public NetworkConnectionStatusEvent(boolean available) {
        this.available = available;
    }

    public boolean isWifi() {
        return wifi;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public boolean isAvailable() {
        return available;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("NetworkConnectionStatusEvent{");
        sb.append("available=").append(available);
        sb.append('}');
        return sb.toString();
    }
}
