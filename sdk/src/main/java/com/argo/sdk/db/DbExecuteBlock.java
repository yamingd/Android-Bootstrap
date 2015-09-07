package com.argo.sdk.db;

/**
 * Created by user on 8/13/15.
 */
public interface DbExecuteBlock<T> {

    /**
     *
     * @param engine
     */
    void execute(T engine);

}
