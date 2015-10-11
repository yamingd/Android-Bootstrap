package com.argo.sdk.event;

import com.argo.sdk.ApiError;

import java.io.Serializable;
import java.net.SocketTimeoutException;

/**
 * Created by user on 6/18/15.
 */
public abstract class AppBaseEvent implements Serializable {

    private Exception exception;
    private int errorCode = 0;
    private String message;
    private boolean dataFromCache = true;

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean hasError(){
        return exception != null || errorCode > 0;
    }

    public boolean isNetworkLost(){
        if (exception != null){
            if (exception instanceof SocketTimeoutException){
                return true;
            }
        }

        return false;
    }

    public boolean isDataFromCache() {
        return dataFromCache;
    }

    public void setDataFromCache(boolean dataFromCache) {
        this.dataFromCache = dataFromCache;
    }

    /**
     *
     * @param apiError
     */
    public void parseError(ApiError apiError){
        this.setErrorCode(apiError.getCode());
        this.setException(apiError.getEx());
        this.setMessage(apiError.getMessage());
    }

    public AppBaseEvent(ApiError apiError) {
        this.parseError(apiError);
    }

    public AppBaseEvent(Exception ex) {
        this.setErrorCode(500);
        this.setMessage(ex.getMessage());
        this.setException(ex);
    }

    public AppBaseEvent() {
    }

    @Override
    public String toString() {
        return "AppBaseEvent{" +
                "errorCode=" + errorCode +
                ", exception=" + exception +
                ", message='" + message + '\'' +
                '}';
    }
}
