package com.udacity.nanodegree.popularmovies1;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.udacity.nanodegree.popularmovies1.Objects.MovieDetailsPojo;
import com.udacity.nanodegree.popularmovies1.Objects.MoviesPojo;
import com.udacity.nanodegree.popularmovies1.Objects.Result;
import com.udacity.nanodegree.popularmovies1.data.MovieColumns;
import com.udacity.nanodegree.popularmovies1.data.MovieProvider;
import com.udacity.nanodegree.popularmovies1.utilities.FetcherTask;
import com.udacity.nanodegree.popularmovies1.utilities.GsonClient;
import com.udacity.nanodegree.popularmovies1.utilities.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * The Main Activity responsible for displaying movies in a grid
 */
public class MoviesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, MoviesAdapter.MoviesAdapterOnClickHandler, ViewTask {

    private static final String TAG = MoviesActivity.class.getSimpleName();
    private static final int ID_TOTAL_MOVIES_LOADER = 353;
    private static final int ID_FAVORITE_MOVIES_LOADER = 363;

    private static final String SORTING_STATE = "sorting";
    public static final String FAVORITE_STATUS = "favorite";
    public static final String LOCAL_DB_ID = "dbId";
    private static final String PAGE_PARAM = "page";

    public static boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;

    @BindView(R.id.recyclerview_movies)
    RecyclerView mRecyclerView;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    @BindView(R.id.error_message_display)
    TextView mErrorMessageDisplay;

    private GridLayoutManager layoutManager;
    private MoviesAdapter mMoviesAdapter;
    private int next_page;
    private int max_pages;
    private SORTING sorting;
    private String[] fetchingParams = new String[2];
    private HashMap<String, Integer> mExistingMoviesInDb;
    private FetcherTask fetcherTask;

    /**
     * Listener for defining the end of the grid while scrolling,
     * in order to fetch new data from server to provide an endless scrolling feature
     */
    RecyclerView.OnScrollListener bottomReachListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dy > 0) //check for scroll down
            {
                visibleItemCount = layoutManager.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                if (!loading && sorting != SORTING.FAVORITES) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = true;
                        Log.v(TAG, "Last Item of Grid View. Refreshing...");

                        fetchingParams[1] = String.valueOf(next_page);
                        loadMoviesData();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        ButterKnife.bind(this);

        // Two columns grid layout for Smartphones and Four for Tablets
        Resources res = getResources();
        int noOfColumns = res.getInteger(R.integer.columns);
        layoutManager = new GridLayoutManager(this, noOfColumns);
        layoutManager.setSmoothScrollbarEnabled(true);
        mRecyclerView.setLayoutManager(layoutManager);

        // Endless scrolling, so, the size is not known a priori
        mRecyclerView.setHasFixedSize(false);

        mMoviesAdapter = new MoviesAdapter(this);
        mRecyclerView.setAdapter(mMoviesAdapter);

        // Currently not fetching any data
        loading = false;

        // Detect end of list to fetch new data
        mRecyclerView.addOnScrollListener(bottomReachListener);

        // Default values for movies retrieval
        max_pages = 1;

        // Set the storing preference
        if (savedInstanceState != null && savedInstanceState.containsKey(SORTING_STATE)) {
            sorting = SORTING.values()[savedInstanceState.getInt(SORTING_STATE)];
        } else {
            sorting = SORTING.POPULAR;
        }

        // Get from Local DB or from API request
        if (sorting == SORTING.FAVORITES) {
            getSupportLoaderManager().initLoader(ID_FAVORITE_MOVIES_LOADER, null, this);
        } else {
            getSupportLoaderManager().initLoader(ID_TOTAL_MOVIES_LOADER, null, this);

            // Default sorting is the Popular
            prepareFetchingDataBasedOnSorting(SORTING.POPULAR);
            loadMoviesData();
        }
    }

    /**
     * In case of new fetch for popular or top_rated movies,
     * initialize all the needed parameters
     *
     * @param selectedSorting {@link SORTING} defining the chosen sorting preference from user
     */
    private void prepareFetchingDataBasedOnSorting(SORTING selectedSorting) {

        sorting = selectedSorting;
        mMoviesAdapter.setMoviesData(null);
        next_page = 1;
        fetchingParams[0] = sorting.toString().toLowerCase();
        fetchingParams[1] = String.valueOf(next_page);

    }

    /**
     * Displays the data view and decides if there are more pages to fetch from server
     *
     */
    private void loadMoviesData() {

        showMoviesDataView();

        if (next_page <= max_pages) {

            fetcherTask = new FetcherTask();
            fetcherTask.mView = this;
            fetcherTask.execute();
        }

    }

    /**
     * Makes visible the Data view and hides the error message view
     */
    private void showMoviesDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the movie data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Makes visible the Error message and hides the Data view
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(MovieDetailsPojo movie) {

        if (movie != null) {

            Context context = this;
            Class destinationClass = DetailsActivity.class;
            Intent intentToStartDetailActivity = new Intent(context, destinationClass);

            // Send a json object as text parameter in the intent extras
            String movieJson = GsonClient.getInstance().toJson(movie);
            intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, movieJson);
            if (mExistingMoviesInDb.containsKey(movie.getTitle())) {
                intentToStartDetailActivity.putExtra(FAVORITE_STATUS, true);
                intentToStartDetailActivity.putExtra(LOCAL_DB_ID, mExistingMoviesInDb.get(movie.getTitle()));
            }
            startActivity(intentToStartDetailActivity);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.sorting, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_popular) {

            // Fetch based on most popular criteria
            prepareFetchingDataBasedOnSorting(SORTING.POPULAR);
            loadMoviesData();

            return true;

        } else if (id == R.id.action_top_rated) {

            // Fetch based on top rated criteria
            prepareFetchingDataBasedOnSorting(SORTING.TOP_RATED);
            loadMoviesData();

            return true;
        } else if (id == R.id.action_favorites) {

            // Fetch based on favorites criteria
            sorting = SORTING.FAVORITES;
            getSupportLoaderManager().initLoader(ID_FAVORITE_MOVIES_LOADER, null, this);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {

        if (loaderId == ID_TOTAL_MOVIES_LOADER || loaderId == ID_FAVORITE_MOVIES_LOADER) {
            return new CursorLoader(this,
                    MovieProvider.Movies.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
        } else {
            throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putInt(SORTING_STATE, sorting.ordinal());

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (loader.getId() == ID_TOTAL_MOVIES_LOADER) {

            // Retrieve the favorites IDs in order to display star on not and proceed accordingly
            mExistingMoviesInDb = new HashMap<>();
            if (data != null && data.getCount() > 0) {
                while (data.moveToNext()) {
                    mExistingMoviesInDb.put(data.getString(data.getColumnIndex(MovieColumns.TITLE)),
                            data.getInt(data.getColumnIndex(MovieColumns.ID)));
                }
            }
            Log.v(TAG, "Finished retrieving favorite movies ids");
        } else if (loader.getId() == ID_FAVORITE_MOVIES_LOADER) {

            // Retrieve the favorites IDs and build POJO from Cursor in order to display the data
            mExistingMoviesInDb = new HashMap<>();
            if (data != null && data.getCount() > 0) {

                MoviesPojo moviesData = new MoviesPojo();
                List<Result> results = new ArrayList<>();
                moviesData.setPage(1);
                moviesData.setTotalPages(1);
                moviesData.setTotalResults(data.getCount());
                Result movie;
                if (data.getCount() > 0 && data.getPosition() != -1) {
                    data.moveToPosition(-1);
                }
                while (data.moveToNext()) {
                    mExistingMoviesInDb.put(data.getString(data.getColumnIndex(MovieColumns.TITLE)), data.getInt(data.getColumnIndex(MovieColumns.ID)));
                    movie = new Result();
                    movie.setId(Integer.parseInt(data.getString(data.getColumnIndex(MovieColumns.MOVIE_ID))));
                    movie.setTitle(data.getString(data.getColumnIndex(MovieColumns.TITLE)));
                    movie.setReleaseDate(data.getString(data.getColumnIndex(MovieColumns.DATE)));
                    movie.setVoteAverage(Double.parseDouble(data.getString(data.getColumnIndex(MovieColumns.RATING))));
                    movie.setOverview(data.getString(data.getColumnIndex(MovieColumns.SYNOPSIS)));
                    movie.setPosterPath(data.getString(data.getColumnIndex(MovieColumns.THUMBNAIL)));
                    results.add(movie);
                }
                moviesData.setResults(results);
                showMoviesDataView();
                mMoviesAdapter.setMoviesData(null);
                mMoviesAdapter.setMoviesData(moviesData);
            }
            Log.v(TAG, "Finished retrieving favorite movies and convert to POJO");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Prepares the movies URL for TMDb
     * @return {@link URL} with the requested URL
     */
    @Override
    public URL getFetcherUrl() {

        String sorting = fetchingParams[0];
        String page = fetchingParams[1];

        LinkedHashSet<String> paths = new LinkedHashSet<>();
        paths.add(sorting);
        HashMap<String, String> urlParams = new HashMap<>();
        urlParams.put(PAGE_PARAM, page);

        return NetworkUtils.buildUrl(paths, urlParams);
    }

    /**
     * Send to generic Async Task the desired Pojo
     * @return {@link Class} the Movies Pojo
     */
    @Override
    public Class getFetchingClass() {
        return MoviesPojo.class;
    }

    /**
     * Updates the UI with the fetched data
     * @param data {@link Object} with the returned data from Async Task
     */
    @Override
    public void updateFetchingData(Object data) {

        if (data != null && MoviesPojo.class.isInstance(data)) {

            MoviesPojo moviesData = (MoviesPojo) data;
            mLoadingIndicator.setVisibility(View.INVISIBLE);

            // Update the total pages in order to know when to stop endless scrolling
            max_pages = moviesData.getTotalPages();

            showMoviesDataView();
            mMoviesAdapter.setMoviesData(moviesData);
            next_page = (next_page < moviesData.getTotalPages()) ? (next_page + 1) : next_page;

        } else {
            showErrorMessage();
        }

    }

    /**
     * Show the loading indicator when fetching data
     */
    @Override
    public void onPreFetchingData() {
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    /**
     * Enumeration holding the sorting options
     */
    public enum SORTING {
        POPULAR, TOP_RATED, FAVORITES;
    }

}
