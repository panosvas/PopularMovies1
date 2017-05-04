package com.udacity.nanodegree.popularmovies1.utilities;

import android.os.AsyncTask;

import com.udacity.nanodegree.popularmovies1.ViewTask;

import java.net.URL;

/**
 * The AsyncTask responsible for the background data fetching
 */
public class FetcherTask extends AsyncTask<Void, Void, Object> {

    public ViewTask mView;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Do view specific actions on pre execute
        mView.onPreFetchingData();
    }

    @Override
    protected Object doInBackground(Void... params) {

        try {

            // Get the URL
            URL fetchUrl = mView.getFetcherUrl();

            Object result = null;
            if (fetchUrl != null) {
                result = NetworkUtils.fetchDataFromUrl(fetchUrl, mView.getFetchingClass());
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Object data) {

        mView.updateFetchingData(data);
    }
}
