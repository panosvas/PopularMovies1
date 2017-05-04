package com.udacity.nanodegree.popularmovies1.utilities;

import okhttp3.OkHttpClient;

/**
 * A Singleton Class for accessing the OkHttp object
 */
public class RestClient {

    private static OkHttpClient restClient;

    public synchronized static OkHttpClient getInstance() {

        if (restClient == null) {
            restClient = new OkHttpClient();
        }
        return restClient;
    }

}
