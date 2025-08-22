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

package com.musenkishi.wally.base;

import android.content.IntentFilter;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.musenkishi.wally.observers.FiltersChangeReceiver;
import com.musenkishi.wally.observers.FiltersChangeReceiver.OnFiltersChangeListener;

/**
 * A base class that handles registration of FiltersChangeReceiver.
 * Created by Akay.
 */
public abstract class BaseFilterFragment extends GridFragment implements OnFiltersChangeListener {

    private FiltersChangeReceiver filtersChangeReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filtersChangeReceiver = new FiltersChangeReceiver();
        filtersChangeReceiver.addListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register the receiver
        if (getActivity() != null) {
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(filtersChangeReceiver, 
                new IntentFilter(FiltersChangeReceiver.FILTERS_CHANGED));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the receiver
        if (getActivity() != null) {
            try {
                LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(filtersChangeReceiver);
            } catch (IllegalArgumentException e) {
                // Receiver not registered, ignore
            }
        }
    }
}
