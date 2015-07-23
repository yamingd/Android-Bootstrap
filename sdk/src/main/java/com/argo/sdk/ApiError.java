package com.argo.sdk;


import timber.log.Timber;

public class ApiError extends Exception {

    private int code;
    private String error;
    private Exception ex;
    private String url;

    public ApiError(int code, Exception ex) {
        super(ex);
        this.code = code;
        this.ex = ex;
        if (ex != null) {
            this.error = ex.getMessage();
        }
    }

    public ApiError(int code, String error) {
        super(error);
        this.code = code;
        this.error = error;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Exception getEx() {
        return ex;
    }

    public void setEx(Exception ex) {
        this.ex = ex;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void printout(){
        if (this.getEx() != null){
            Timber.e(this.getEx(), "%s\nError: %s", url, error);
        }else{
            Timber.e(this.getMessage(), "%s\nError: %s", url, error);
        }
    }

    @Override
    public String toString() {
        return "ApiError{" +
                "code=" + code +
                ", error='" + error + '\'' +
                ", ex=" + ex +
                '}';
    }
}
