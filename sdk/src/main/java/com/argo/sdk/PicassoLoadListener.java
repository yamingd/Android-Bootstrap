package com.argo.sdk;

import android.net.Uri;

import com.squareup.picasso.Picasso;

import timber.log.Timber;

/**
 * Created by user on 7/17/15.
 */
public class PicassoLoadListener implements Picasso.Listener {

    @Override
    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
        Timber.e(exception, "load Failed: %s", uri);
    }
}
