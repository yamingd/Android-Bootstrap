package com.argo.sdk.db;

import java.util.List;

/**
 * Created by user on 7/2/15.
 */
public interface DbQueryResultCallback<T> {

    /**
     *
     * @param list
     * @param total
     * @param maxCursorId
     */
    void call(List<T> list, int total, long maxCursorId);
}
