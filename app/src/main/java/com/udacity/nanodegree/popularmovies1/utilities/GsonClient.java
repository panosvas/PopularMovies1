package com.udacity.nanodegree.popularmovies1.utilities;

import com.google.gson.Gson;

/**
 * A Singleton Class for accessing the Gson object
 */
public class GsonClient {

    private static Gson gsonClient;

    public synchronized static Gson getInstance() {

        if (gsonClient == null) {
            gsonClient = new Gson();
        }
        return gsonClient;
    }

}
