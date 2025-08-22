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

package com.musenkishi.wally.dataprovider;

import android.net.Uri;

import com.musenkishi.wally.dataprovider.models.DataProviderError;
import com.musenkishi.wally.models.filters.FilterAspectRatioKeys;
import com.musenkishi.wally.models.filters.FilterBoardsKeys;
import com.musenkishi.wally.models.filters.FilterGroupsStructure;
import com.musenkishi.wally.models.filters.FilterPurityKeys;
import com.musenkishi.wally.models.filters.FilterResolutionKeys;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Musenkishi on 2014-03-01 15:31.
 * No threading shall take place in this class.
 */
public class NetworkDataProvider {

    public static final String WALLHAVEN_BASE_URL = "https://wallhaven.cc/";
    public static final String PATH_TOPLIST = "toplist";
    public static final String PATH_RANDOM = "random";
    public static final String PATH_SEARCH = "search";
    public static final String PATH_LATEST = "latest";
    public static final int THUMBS_PER_PAGE = 24;

    public NetworkDataProvider() {
    }

    private Uri buildWallhavenUrl(int page, String path, FilterGroupsStructure filterGroupsStructure) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https").authority("wallhaven.cc");

        if (PATH_TOPLIST.equalsIgnoreCase(path)) {
            // For toplist, use the direct path with just page number and filters
            builder.appendEncodedPath(path)
                  .appendQueryParameter(FilterBoardsKeys.PARAMETER_KEY, filterGroupsStructure.getBoardsFilter())
                  .appendQueryParameter(FilterPurityKeys.PARAMETER_KEY, filterGroupsStructure.getPurityFilter())
                  .appendQueryParameter(FilterResolutionKeys.PARAMETER_KEY, filterGroupsStructure.getResolutionFilter().getValue())
                  .appendQueryParameter(FilterAspectRatioKeys.PARAMETER_KEY, filterGroupsStructure.getAspectRatioFilter().getValue())
                  .appendQueryParameter("page", page+"");
        } else {
            // For other paths (search, random, latest), use the search endpoint
            String sorting = "views";
            if (PATH_SEARCH.equalsIgnoreCase(path)) {
                sorting = "relevance";
            } else if (PATH_RANDOM.equalsIgnoreCase(path)) {
                sorting = "random";
            } else if (PATH_LATEST.equalsIgnoreCase(path)) {
                sorting = "date_added";
            }

            builder.appendEncodedPath("search")
                  .appendQueryParameter(FilterBoardsKeys.PARAMETER_KEY, filterGroupsStructure.getBoardsFilter())
                  .appendQueryParameter(FilterResolutionKeys.PARAMETER_KEY, filterGroupsStructure.getResolutionFilter().getValue())
                  .appendQueryParameter(FilterPurityKeys.PARAMETER_KEY, filterGroupsStructure.getPurityFilter())
                  .appendQueryParameter(FilterAspectRatioKeys.PARAMETER_KEY, filterGroupsStructure.getAspectRatioFilter().getValue())
                  .appendQueryParameter("sorting", sorting)
                  .appendQueryParameter("order", "desc")
                  .appendQueryParameter("page", page+"");
        }

        return builder.build();
    }

    public Uri buildWallhavenApiUrl(int page, String path, FilterGroupsStructure filterGroupsStructure, String query, String color) {
        String sorting = "views";
        if (PATH_SEARCH.equalsIgnoreCase(path)) {
            sorting = "relevance";
        } else if (PATH_RANDOM.equalsIgnoreCase(path)) {
            sorting = "random";
        } else if (PATH_LATEST.equalsIgnoreCase(path)) {
            sorting = "date_added";
        } else if (PATH_TOPLIST.equalsIgnoreCase(path)) {
            sorting = "toplist";
        }

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https").authority("wallhaven.cc")
                .appendEncodedPath("api/v1/search")
                .appendQueryParameter(FilterBoardsKeys.PARAMETER_KEY, filterGroupsStructure.getBoardsFilter())
                .appendQueryParameter(FilterPurityKeys.PARAMETER_KEY, filterGroupsStructure.getPurityFilter())
                .appendQueryParameter(FilterResolutionKeys.PARAMETER_KEY, filterGroupsStructure.getResolutionFilter().getValue())
                .appendQueryParameter(FilterAspectRatioKeys.PARAMETER_KEY, filterGroupsStructure.getAspectRatioFilter().getValue())
                .appendQueryParameter("sorting", sorting)
                .appendQueryParameter("order", "desc")
                .appendQueryParameter("page", page+"");

        if (query != null) {
            builder.appendQueryParameter("q", query);
        }
        if (color != null) {
            builder.appendQueryParameter("color", color);
        }

        return builder.build();
    }

    public String getDataSync(String path, int index, FilterGroupsStructure filterGroupsStructure){
        Uri uri = buildWallhavenUrl(index, path, filterGroupsStructure);
        String url = uri.toString();
        return getDataSync(url, null);
    }

    public String getDataSync(String path, int index, FilterGroupsStructure filterGroupsStructure, String apiKey){
        Uri uri = buildWallhavenUrl(index, path, filterGroupsStructure);
        String url = uri.toString();
        return getDataSync(url, apiKey);
    }

    public String getDataSync(String url){
        return getDataSync(url, null);
    }

    public String getDataSync(String url, String apiKey){
        try {
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(10, TimeUnit.SECONDS);
            client.setReadTimeout(10, TimeUnit.SECONDS);

            Request.Builder requestBuilder = new Request.Builder()
                    .url(url);

            if (apiKey != null && apiKey.length() > 0) {
                requestBuilder.addHeader("X-API-Key", apiKey);
            }

            Request request = requestBuilder.build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void getData(String path, int index, FilterGroupsStructure filterGroupsStructure, final OnDataReceivedListener onDataReceivedListener) {
        Uri uri = buildWallhavenUrl(index, path, filterGroupsStructure);
        String url = uri.toString();
        getData(url, onDataReceivedListener);
    }

    public void getData(String path, int index, FilterGroupsStructure filterGroupsStructure, String apiKey, final OnDataReceivedListener onDataReceivedListener) {
        Uri uri = buildWallhavenUrl(index, path, filterGroupsStructure);
        String url = uri.toString();
        getData(url, apiKey, onDataReceivedListener);
    }

    public void getDataApi(String path, int index, FilterGroupsStructure filterGroupsStructure, String apiKey, final OnDataReceivedListener onDataReceivedListener) {
        Uri uri = buildWallhavenApiUrl(index, path, filterGroupsStructure, null, null);
        String url = uri.toString();
        getData(url, apiKey, onDataReceivedListener);
    }

    public void getData(String url, final OnDataReceivedListener onDataReceivedListener) {
        getData(url, null, onDataReceivedListener);
    }

    public void getData(String url, String apiKey, final OnDataReceivedListener onDataReceivedListener) {
        try {
            OkHttpClient client = new OkHttpClient();

            client.setConnectTimeout(10, TimeUnit.SECONDS);
            client.setReadTimeout(10, TimeUnit.SECONDS);

            Request.Builder requestBuilder = new Request.Builder()
                    .url(url);

            if (apiKey != null && apiKey.length() > 0) {
                requestBuilder.addHeader("X-API-Key", apiKey);
            }

            Request request = requestBuilder.build();
            Response response = client.newCall(request).execute();
            if (onDataReceivedListener != null) {
                onDataReceivedListener.onData(response.body().string(), url);
            }
        } catch (MalformedURLException e) {
            if (onDataReceivedListener != null) {
                DataProviderError dataProviderError = new DataProviderError(
                        DataProviderError.Type.NETWORK,
                        400,
                        "MalformedURLException");
                onDataReceivedListener.onError(dataProviderError);
            }
            e.printStackTrace();
        } catch (IOException e) {
            if (onDataReceivedListener != null) {
                DataProviderError dataProviderError = new DataProviderError(
                        DataProviderError.Type.NETWORK,
                        400,
                        "IOException");
                onDataReceivedListener.onError(dataProviderError);
            }
            e.printStackTrace();
        }
    }

    public void getData(String path, String query, String color, int index, FilterGroupsStructure filterGroupsStructure, OnDataReceivedListener onDataReceivedListener) {
        Uri uri = buildWallhavenUrl(index, path, filterGroupsStructure);
        if (color != null) {
            uri = uri.buildUpon().appendQueryParameter("color", color).build();
        }
        uri = uri.buildUpon().appendQueryParameter("q", query).build();
        String url = uri.toString();
        getData(url, onDataReceivedListener);
    }

    public void getData(String path, String query, String color, int index, FilterGroupsStructure filterGroupsStructure, String apiKey, OnDataReceivedListener onDataReceivedListener) {
        Uri uri = buildWallhavenUrl(index, path, filterGroupsStructure);
        if (color != null) {
            uri = uri.buildUpon().appendQueryParameter("color", color).build();
        }
        uri = uri.buildUpon().appendQueryParameter("q", query).build();
        String url = uri.toString();
        getData(url, apiKey, onDataReceivedListener);
    }

    public void getDataApi(String path, String query, String color, int index, FilterGroupsStructure filterGroupsStructure, String apiKey, OnDataReceivedListener onDataReceivedListener) {
        Uri uri = buildWallhavenApiUrl(index, path, filterGroupsStructure, query, color);
        String url = uri.toString();
        getData(url, apiKey, onDataReceivedListener);
    }

    public void getDataApiForWallpaper(String wallpaperId, String apiKey, OnDataReceivedListener onDataReceivedListener) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https").authority("wallhaven.cc")
                .appendEncodedPath("api/v1/w")
                .appendEncodedPath(wallpaperId);
        
        String url = builder.build().toString();
        getData(url, apiKey, onDataReceivedListener);
    }

    public interface OnDataReceivedListener {
        void onData(String data, String url);

        void onError(DataProviderError error);
    }
}
