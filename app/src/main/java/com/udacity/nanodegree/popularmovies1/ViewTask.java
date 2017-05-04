package com.udacity.nanodegree.popularmovies1;

import java.net.URL;

/**
 * Interface with required methods for Async Task Fetcher generic implementation
 */
public interface ViewTask {

    URL getFetcherUrl();
    Class getFetchingClass();
    void updateFetchingData(Object data);
    void onPreFetchingData();
}
