package com.argo.sdk.event;

/**
 * Created by user on 8/26/15.
 */
public class HttpCallStopEvent extends AppBaseEvent {

    private String[] filterUrl = null;

    public HttpCallStopEvent(String[] filterUrl) {
        this.filterUrl = filterUrl;
    }

    /**
     *
     * @param url
     * @return
     */
    public boolean isFilter(String url){
        for (int i = 0; i < filterUrl.length; i++) {
            if (url.startsWith(filterUrl[i])){
                return true;
            }
        }

        return false;
    }
}
