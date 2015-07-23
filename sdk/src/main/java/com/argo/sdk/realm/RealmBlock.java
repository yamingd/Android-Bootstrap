package com.argo.sdk.realm;

import io.realm.Realm;

/**
 * Created by user on 6/15/15.
 */
public interface RealmBlock {

    void execute(Realm realm);

}
