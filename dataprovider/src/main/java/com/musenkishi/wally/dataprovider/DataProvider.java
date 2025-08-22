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

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import androidx.core.content.FileProvider;

import com.musenkishi.wally.dataprovider.models.DataProviderError;
import com.musenkishi.wally.dataprovider.models.SaveImageRequest;
import com.musenkishi.wally.dataprovider.util.Parser;
import com.musenkishi.wally.models.ExceptionReporter;
import com.musenkishi.wally.models.Filter;
import com.musenkishi.wally.models.Image;
import com.musenkishi.wally.models.ImagePage;
import com.musenkishi.wally.models.filters.FilterGroupsStructure;
import com.musenkishi.wally.models.filters.FilterPurityKeys;

import java.io.File;
import java.util.ArrayList;

import static com.musenkishi.wally.dataprovider.NetworkDataProvider.OnDataReceivedListener;

/**
 * <strong>No threading shall take place here.</strong>
 * Use this class to get and set data.
 * Created by Musenkishi on 2014-02-28.
 */
public class DataProvider {

    private static final String TAG = "DataProvider";
    private final Context context;
    private final SharedPreferencesDataProvider sharedPreferencesDataProvider;
    private final DownloadManager downloadManager;
    private final Parser parser;

    public interface OnImagesReceivedListener {
        void onImagesReceived(ArrayList<Image> images);

        void onError(DataProviderError dataProviderError);
    }

    public interface OnPageReceivedListener {
        void onPageReceived(ImagePage imagePage);

        void onError(DataProviderError dataProviderError);
    }

    public DataProvider(Context context, ExceptionReporter.OnReportListener onReportListener) {
        sharedPreferencesDataProvider = new SharedPreferencesDataProvider(context);
        parser = new Parser(onReportListener);
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        this.context = context;
    }

    public SharedPreferencesDataProvider getSharedPreferencesDataProviderInstance() {
        return sharedPreferencesDataProvider;
    }

    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    public void getImages(String path, String query, String color, int index, FilterGroupsStructure filterGroupsStructure, final OnImagesReceivedListener onImagesReceivedListener) {
        String apiKey = sharedPreferencesDataProvider.getWallhavenApiKey();
        boolean wantsNsfw = sharedPreferencesDataProvider.getPurity(FilterPurityKeys.PARAMETER_KEY).length() >= 3 && sharedPreferencesDataProvider.getPurity(FilterPurityKeys.PARAMETER_KEY).charAt(2) == '1';
        if (wantsNsfw) {
            // Check if API key is available when NSFW is enabled
            if (apiKey == null || apiKey.length() == 0) {
                if (onImagesReceivedListener != null) {
                    DataProviderError apiKeyError = new DataProviderError(DataProviderError.Type.LOCAL, 401, "API key required for NSFW content");
                    onImagesReceivedListener.onError(apiKeyError);
                }
                return;
            }
            
            new NetworkDataProvider().getDataApi(path, query, color, index, filterGroupsStructure, apiKey, new OnDataReceivedListener() {
                @Override
                public void onData(String data, String url) {
                    ArrayList<Image> images = parser.parseImagesFromApi(data);
                    if (onImagesReceivedListener != null) {
                        onImagesReceivedListener.onImagesReceived(images);
                    }
                }

                @Override
                public void onError(DataProviderError dataProviderError) {
                    if (onImagesReceivedListener != null) {
                        onImagesReceivedListener.onError(dataProviderError);
                    }
                }
            });
            return;
        }
        new NetworkDataProvider().getData(path, query, color, index, filterGroupsStructure, apiKey, new OnDataReceivedListener() {
            @Override
            public void onData(String data, String url) {
                ArrayList<Image> images = parser.parseImages(data);
                if (onImagesReceivedListener != null) {
                    onImagesReceivedListener.onImagesReceived(images);
                }
            }

            @Override
            public void onError(DataProviderError dataProviderError) {
                if (onImagesReceivedListener != null) {
                    onImagesReceivedListener.onError(dataProviderError);
                }
            }
        });
    }

    public ArrayList<Image> getImagesSync(String path, int index, FilterGroupsStructure filterGroupsStructure) {
        String apiKey = sharedPreferencesDataProvider.getWallhavenApiKey();
        boolean wantsNsfw = sharedPreferencesDataProvider.getPurity(FilterPurityKeys.PARAMETER_KEY).length() >= 3 && sharedPreferencesDataProvider.getPurity(FilterPurityKeys.PARAMETER_KEY).charAt(2) == '1';
        String data;
        if (wantsNsfw) {
            // Check if API key is available when NSFW is enabled
            if (apiKey == null || apiKey.length() == 0) {
                // Return null to indicate failure - caller should handle this
                return null;
            }
            
            Uri uri = new NetworkDataProvider().buildWallhavenApiUrl(index, path, filterGroupsStructure, null, null);
            data = new NetworkDataProvider().getDataSync(uri.toString(), apiKey);
            if (data != null) {
                return parser.parseImagesFromApi(data);
            }
            return null;
        } else {
            data = new NetworkDataProvider().getDataSync(path, index, filterGroupsStructure, apiKey);
        }
        if (data != null) {
            return parser.parseImages(data);
        } else {
            return null;
        }
    }

    /**
     */
    public void getImages(String path, int index, FilterGroupsStructure filterGroupsStructure, final OnImagesReceivedListener onImagesReceivedListener) {

        String apiKey = sharedPreferencesDataProvider.getWallhavenApiKey();
        boolean wantsNsfw = sharedPreferencesDataProvider.getPurity(FilterPurityKeys.PARAMETER_KEY).length() >= 3 && sharedPreferencesDataProvider.getPurity(FilterPurityKeys.PARAMETER_KEY).charAt(2) == '1';
        if (wantsNsfw) {
            // Check if API key is available when NSFW is enabled
            if (apiKey == null || apiKey.length() == 0) {
                if (onImagesReceivedListener != null) {
                    DataProviderError apiKeyError = new DataProviderError(DataProviderError.Type.LOCAL, 401, "API key required for NSFW content");
                    onImagesReceivedListener.onError(apiKeyError);
                }
                return;
            }
            
            new NetworkDataProvider().getDataApi(path, index, filterGroupsStructure, apiKey, new OnDataReceivedListener() {
                @Override
                public void onData(String data, String url) {
                    ArrayList<Image> images = parser.parseImagesFromApi(data);
                    if (onImagesReceivedListener != null) {
                        if (!images.isEmpty()) {
                            onImagesReceivedListener.onImagesReceived(images);
                        } else {
                            DataProviderError noImagesError = new DataProviderError(DataProviderError.Type.LOCAL, 204, "No images");
                            onImagesReceivedListener.onError(noImagesError);
                        }
                    }
                }

                @Override
                public void onError(DataProviderError dataProviderError) {
                    if (onImagesReceivedListener != null) {
                        onImagesReceivedListener.onError(dataProviderError);
                    }
                }
            });
            return;
        }
        new NetworkDataProvider().getData(path, index, filterGroupsStructure, apiKey, new OnDataReceivedListener() {
            @Override
            public void onData(String data, String url) {
                ArrayList<Image> images = parser.parseImages(data);
                if (onImagesReceivedListener != null) {
                    if (!images.isEmpty()) {
                        onImagesReceivedListener.onImagesReceived(images);
                    } else {
                        DataProviderError noImagesError = new DataProviderError(DataProviderError.Type.LOCAL, 204, "No images");
                        onImagesReceivedListener.onError(noImagesError);
                    }
                }
            }

            @Override
            public void onError(DataProviderError dataProviderError) {
                if (onImagesReceivedListener != null) {
                    onImagesReceivedListener.onError(dataProviderError);
                }
            }
        });
    }

    public ImagePage getPageDataSync(String imagePageUrl) {
        String apiKey = sharedPreferencesDataProvider.getWallhavenApiKey();
        boolean wantsNsfw = sharedPreferencesDataProvider.getPurity(FilterPurityKeys.PARAMETER_KEY).length() >= 3 && sharedPreferencesDataProvider.getPurity(FilterPurityKeys.PARAMETER_KEY).charAt(2) == '1';
        
        if (wantsNsfw && apiKey != null && apiKey.length() > 0) {
            // Extract wallpaper ID from URL (e.g., "https://wallhaven.cc/w/123456" -> "123456")
            String wallpaperId = extractWallpaperId(imagePageUrl);
            if (wallpaperId != null) {
                String data = new NetworkDataProvider().getDataSync("https://wallhaven.cc/api/v1/w/" + wallpaperId, apiKey);
                if (data != null) {
                    return parser.parseImagePageFromApi(data, imagePageUrl);
                }
            }
        }
        
        // Fallback to regular HTML parsing
        String data = new NetworkDataProvider().getDataSync(imagePageUrl);
        return parser.parseImagePage(data, imagePageUrl);
    }

    public void getPageData(String imagePageUrl, final OnPageReceivedListener onPageReceivedListener) {
        String apiKey = sharedPreferencesDataProvider.getWallhavenApiKey();
        boolean wantsNsfw = sharedPreferencesDataProvider.getPurity(FilterPurityKeys.PARAMETER_KEY).length() >= 3 && sharedPreferencesDataProvider.getPurity(FilterPurityKeys.PARAMETER_KEY).charAt(2) == '1';
        
        if (wantsNsfw && apiKey != null && apiKey.length() > 0) {
            // Extract wallpaper ID from URL (e.g., "https://wallhaven.cc/w/123456" -> "123456")
            String wallpaperId = extractWallpaperId(imagePageUrl);
            if (wallpaperId != null) {
                new NetworkDataProvider().getData("https://wallhaven.cc/api/v1/w/" + wallpaperId, apiKey, new OnDataReceivedListener() {
                    @Override
                    public void onData(String data, String url) {
                        ImagePage imagePage = parser.parseImagePageFromApi(data, imagePageUrl);
                        if (onPageReceivedListener != null) {
                            onPageReceivedListener.onPageReceived(imagePage);
                        }
                    }

                    @Override
                    public void onError(DataProviderError error) {
                        if (onPageReceivedListener != null) {
                            onPageReceivedListener.onError(error);
                        }
                    }
                });
                return;
            }
        }
        
        // Fallback to regular HTML parsing
        new NetworkDataProvider().getData(imagePageUrl, new OnDataReceivedListener() {
            @Override
            public void onData(String data, String url) {
                ImagePage imagePage = parser.parseImagePage(data, url);

                if (onPageReceivedListener != null) {
                    onPageReceivedListener.onPageReceived(imagePage);
                }
            }

            @Override
            public void onError(DataProviderError error) {
                if (onPageReceivedListener != null) {
                    onPageReceivedListener.onError(error);
                }
            }
        });
    }

    public void setTimeSpan(String tag, Filter<String, String> timespan) {
        sharedPreferencesDataProvider.setTimespan(tag, timespan);
    }

    public Filter<String, String> getTimespan(String tag) {
        return sharedPreferencesDataProvider.getTimespan(tag);
    }

    public void setBoards(String tag, String paramValue) {
        sharedPreferencesDataProvider.setBoards(tag, paramValue);
    }

    public String getBoards(String tag) {
        return sharedPreferencesDataProvider.getBoards(tag);
    }

    public void setPurity(String tag, String paramValue) {
        sharedPreferencesDataProvider.setPurity(tag, paramValue);
    }

    public String getPurity(String tag) {
        return sharedPreferencesDataProvider.getPurity(tag);
    }

    public void setAspectRatio(String tag, Filter<String, String> aspectRatio) {
        sharedPreferencesDataProvider.setAspectRatio(tag, aspectRatio);
    }

    public Filter<String, String> getAspectRatio(String tag) {
        return sharedPreferencesDataProvider.getAspectRatio(tag);
    }

    public void setResolutionOption(String tag, String paramValue) {
        sharedPreferencesDataProvider.setResolutionOption(tag, paramValue);
    }

    public String getResolutionOption(String tag) {
        return sharedPreferencesDataProvider.getResolutionOption(tag);
    }

    public void setResolution(String tag, Filter<String, String> resolution) {
        sharedPreferencesDataProvider.setResolution(tag, resolution);
    }

    public Filter<String, String> getResolution(String tag) {
        return sharedPreferencesDataProvider.getResolution(tag);
    }

    private String extractWallpaperId(String imagePageUrl) {
        try {
            // Extract wallpaper ID from URL (e.g., "https://wallhaven.cc/w/123456" -> "123456")
            if (imagePageUrl != null && imagePageUrl.contains("/w/")) {
                String[] parts = imagePageUrl.split("/w/");
                if (parts.length > 1) {
                    String idPart = parts[1];
                    // Remove any query parameters or fragments
                    if (idPart.contains("?")) {
                        idPart = idPart.split("\\?")[0];
                    }
                    if (idPart.contains("#")) {
                        idPart = idPart.split("#")[0];
                    }
                    return idPart;
                }
            }
        } catch (Exception e) {
            // If extraction fails, return null to fallback to HTML parsing
        }
        return null;
    }

    /**
     * Checks if NSFW is enabled and validates that an API key is available
     * @return true if NSFW is enabled and API key is valid, false otherwise
     */
    public boolean isNsfwEnabledWithValidApiKey() {
        boolean wantsNsfw = sharedPreferencesDataProvider.getPurity(FilterPurityKeys.PARAMETER_KEY).length() >= 3 && 
                           sharedPreferencesDataProvider.getPurity(FilterPurityKeys.PARAMETER_KEY).charAt(2) == '1';
        
        if (!wantsNsfw) {
            return false;
        }
        
        String apiKey = sharedPreferencesDataProvider.getWallhavenApiKey();
        return apiKey != null && apiKey.length() > 0;
    }

    /**
     * Gets the current API key if available
     * @return the API key or null if not set
     */
    public String getApiKey() {
        return sharedPreferencesDataProvider.getWallhavenApiKey();
    }

    /**
     * Checks if NSFW is enabled but API key is missing
     * @return true if NSFW is enabled but no API key is set
     */
    public boolean isNsfwEnabledButMissingApiKey() {
        boolean wantsNsfw = sharedPreferencesDataProvider.getPurity(FilterPurityKeys.PARAMETER_KEY).length() >= 3 && 
                           sharedPreferencesDataProvider.getPurity(FilterPurityKeys.PARAMETER_KEY).charAt(2) == '1';
        
        if (!wantsNsfw) {
            return false;
        }
        
        String apiKey = sharedPreferencesDataProvider.getWallhavenApiKey();
        return apiKey == null || apiKey.length() == 0;
    }

    /**
     * Checks if the current filter settings require an API key
     * @return true if NSFW is enabled (regardless of API key status)
     */
    public boolean isApiKeyRequired() {
        return sharedPreferencesDataProvider.getPurity(FilterPurityKeys.PARAMETER_KEY).length() >= 3 && 
               sharedPreferencesDataProvider.getPurity(FilterPurityKeys.PARAMETER_KEY).charAt(2) == '1';
    }

    /**
     * Validates if the provided API key has a valid format
     * @param apiKey the API key to validate
     * @return true if the API key format is valid
     */
    public boolean isValidApiKeyFormat(String apiKey) {
        if (apiKey == null || apiKey.length() == 0) {
            return false;
        }
        // Wallhaven API keys are typically alphanumeric and at least 32 characters
        return apiKey.matches("^[a-zA-Z0-9]{32,}$");
    }

    /**
     * Gets the current API key validation status
     * @return true if API key is present and has valid format
     */
    public boolean hasValidApiKey() {
        String apiKey = getApiKey();
        return isValidApiKeyFormat(apiKey);
    }

    /**
     * Gets a user-friendly error message for API key issues
     * @return error message or null if no issues
     */
    public String getApiKeyErrorMessage() {
        if (!isApiKeyRequired()) {
            return null; // No API key required
        }
        
        if (isNsfwEnabledButMissingApiKey()) {
            return "NSFW content requires a valid Wallhaven API key. Please add your API key in the filter settings.";
        }
        
        if (!hasValidApiKey()) {
            return "Invalid API key format. Please check your Wallhaven API key in the filter settings.";
        }
        
        return null; // No issues
    }

    public SaveImageRequest downloadImageIfNeeded(Uri path, String filename, String notificationTitle) {
        FileManager fileManager = new FileManager();

        if (fileManager.fileExists(filename)) {
            File file = fileManager.getFile(filename);
            if (file != null) {
                Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
                return new SaveImageRequest(fileUri);
            }
        }

        String type = ".png"; //fallback to ".png"
        if (path.toString().lastIndexOf(".") != -1) { //-1 means there are no punctuations in the path
            type = path.toString().substring(path.toString().lastIndexOf("."));
        }

        try {
            DownloadManager.Request request = new DownloadManager.Request(path);
            request.setTitle(notificationTitle);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            
            // Use MediaStore for Android Q and above, fallback to legacy method for older versions
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "Wally/" + filename + type);
            } else {
                // Create the Wally directory if it doesn't exist (legacy approach)
                File wallsDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "Wally");
                if (!wallsDir.exists()) {
                    wallsDir.mkdirs();
                }
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "Wally/" + filename + type);
            }
            long downloadId = downloadManager.enqueue(request);
            
            // Double check if the download was actually queued
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            if (downloadManager.query(query).getCount() > 0) {
                return new SaveImageRequest(downloadId);
            } else {
                return new SaveImageRequest((Long)null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new SaveImageRequest((Long)null);
        }
    }

    public Uri getFilePath(String filename) {
        FileManager fileManager = new FileManager();
        if (fileManager.fileExists(filename)) {
            File file = fileManager.getFile(filename);
            return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        }
        return null;
    }
}
