package com.argo.sdk.realm;

import java.util.List;

/**
 * Created by user on 7/2/15.
 */
public interface RealmQueryResultCallback<T> {

    /**
     *
     * @param list
     * @param total
     * @param maxCursorId
     */
    void call(List<T> list, int total, long maxCursorId);
}
