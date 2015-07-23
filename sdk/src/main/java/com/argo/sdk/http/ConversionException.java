package com.argo.sdk.http;

/**
 * Created by user on 6/15/15.
 */
public class ConversionException extends Exception {

    public ConversionException() {
        super();
    }

    public ConversionException(String detailMessage) {
        super(detailMessage);
    }

    public ConversionException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
