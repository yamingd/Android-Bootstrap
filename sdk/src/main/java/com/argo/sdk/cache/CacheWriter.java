package com.argo.sdk.cache;

import com.jakewharton.disklrucache.DiskLruCache;

/**
 * Created by user on 6/26/15.
 */
public interface CacheWriter {

    /**
     *
     * @param cacheKey
     * @param editor
     */
    boolean write(String cacheKey, DiskLruCache.Editor editor);

}
