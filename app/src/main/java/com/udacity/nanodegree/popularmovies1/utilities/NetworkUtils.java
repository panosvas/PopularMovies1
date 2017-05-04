package com.udacity.nanodegree.popularmovies1.utilities;

import android.net.Uri;
import android.util.Log;

import com.udacity.nanodegree.popularmovies1.BuildConfig;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.Request;
import okhttp3.Response;

/**
 * These utilities will be used to communicate with the TMDb servers.
 */
public final class NetworkUtils {

    final static String API_KEY_PARAM = "api_key";
    private static final String TAG = NetworkUtils.class.getSimpleName();

    // The key is retrieved from the gradle.properties file
    private static final String THEMOVIESDB_API_KEY = BuildConfig.THEMOVIESDB_API_KEY;
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/movie";

    /**
     * This method is responsible for building the requested URL
     * @param paths {@link Set} with the desired paths in order
     * @param parameters {@link HashMap} the parameters key value pairs
     * @return {@link URL} the built URL
     */
    public static URL buildUrl(Set<String> paths, HashMap<String, String> parameters) {

        Uri.Builder uriBuilder = Uri.parse(TMDB_BASE_URL).buildUpon();

        if (paths != null && paths.size() > 0) {
            for (String path : paths) {
                uriBuilder.appendPath(path);
            }
        }

        if (parameters != null && parameters.size() > 0) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                String param = entry.getKey();
                String value = entry.getValue();
                uriBuilder.appendQueryParameter(param, value);
            }
        }

        uriBuilder.appendQueryParameter(API_KEY_PARAM, THEMOVIESDB_API_KEY);
        Uri builtUri = uriBuilder.build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI: " + url);

        return url;
    }

    /**
     * This method is responsible for Fetching data given a URL and deserialize them given a Class
     * @param url {@link URL} the requested URL
     * @param cls {@link Class} the deserialization class
     * @return {@link T} the deserialized object after data retrieval
     */
    public static <T> T fetchDataFromUrl(URL url, Class<T> cls) {

        // Build the request based on the given url
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        T movies = null;
        try {

            // Execute the request
            Response response = RestClient.getInstance().newCall(request).execute();

            // Check if it was successful or not and then deserialize the response using Gson
            if (response.isSuccessful()) {
                Log.v(TAG, "Successfully Fetched URL: " + url);
                movies = GsonClient.getInstance().fromJson(response.body().charStream(), cls);
            } else {
                Log.w(TAG, "Unable to Fetch URL: " + url);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return movies;
    }

}