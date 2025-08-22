/*
 * Copyright (C) 2014 Freddie (Musenkishi) Lust-Hed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.musenkishi.wally.muzei;

import android.content.Intent;
import android.util.Log;

import com.google.android.apps.muzei.api.provider.Artwork;
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider;
import com.musenkishi.wally.base.WallyApplication;
import com.musenkishi.wally.dataprovider.NetworkDataProvider;
import com.musenkishi.wally.models.Image;
import com.musenkishi.wally.models.ImagePage;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * A very basic Muzei extension, providing the top number of wallpapers from the toplist.
 * It's based on the user's filter preferences.
 *
 * Created by Freddie (Musenkishi) Lust-Hed on 2014-08-23.
 */
public abstract class WallyArtSource extends MuzeiArtProvider {

    public static final String TAG = "WallyArtSource";

    private static final int ROTATE_TIME_MILLIS = 3 * 60 * 60 * 1000; // rotate every 3 hours


    @Override
    public boolean onCreate() {
        super.onCreate();
        return false;
    }

    protected void onLoadRequested(int reason) {
        final ArrayList<Image> images = WallyApplication.getDataProviderInstance().getImagesSync(NetworkDataProvider.PATH_TOPLIST, 1, WallyApplication.getFilterSettings());
        if (images != null) {
            if (images.isEmpty()) {
                return;
            }
            pickImage(images);
        } else {
            Log.e(TAG, "images was null");
        }
    }

    private void pickImage(ArrayList<Image> images) {
        final Random random = new Random();
        final Image image = images.get(random.nextInt(images.size()));
        final String newToken = image.imageId();
        getImageAndPublish(image, newToken);
    }

    private void getImageAndPublish(final Image image, final String newToken) {
        ImagePage imagePage = WallyApplication.getDataProviderInstance().getPageDataSync(image.imagePageURL());

        if (imagePage != null) {
            Artwork artwork = new Artwork(
                    newToken,                        // token
                    imagePage.title(),               // title
                    image.imagePageURL(),            // byline (source or page URL)
                    null,                            // attribution (optional string)
                    imagePage.imagePath(),                            // persistentUri (not needed here)
                    image.generateWallyUri(),        // webUri (Uri for click-through)
                    null
            );

            setArtwork(artwork);
        }
    }

}

