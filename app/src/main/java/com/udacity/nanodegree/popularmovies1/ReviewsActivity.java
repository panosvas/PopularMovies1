package com.udacity.nanodegree.popularmovies1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.nanodegree.popularmovies1.Objects.ReviewsPojo;
import com.udacity.nanodegree.popularmovies1.utilities.FetcherTask;
import com.udacity.nanodegree.popularmovies1.utilities.NetworkUtils;

import org.parceler.Parcels;

import java.net.URL;
import java.util.LinkedHashSet;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * The Movies Reviews Activity
 */
public class ReviewsActivity extends AppCompatActivity implements ViewTask {

    private static final String REVIEWS_STATE = "reviews";
    private static final String REVIEWS_URL_PATH = "reviews";

    @BindView(R.id.recyclerview_reviews)
    RecyclerView mRecyclerView;

    @BindView(R.id.no_reviews)
    TextView mNoReviews;

    private LinearLayoutManager layoutManager;
    private ReviewsAdapter mReviewsAdapter;
    private ReviewsPojo mReviews;
    private String mMovieId;

    private FetcherTask fetcherTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        ButterKnife.bind(this);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        // Endless scrolling, so, the size is not known a priori
        mRecyclerView.setHasFixedSize(false);

        mReviewsAdapter = new ReviewsAdapter();
        mRecyclerView.setAdapter(mReviewsAdapter);
        mRecyclerView.setLayoutManager(layoutManager);

        Intent intentThatStartedThisActivity = getIntent();

        if (savedInstanceState != null && savedInstanceState.containsKey(REVIEWS_STATE)) {
            retrieveDataFromSavedInstance(savedInstanceState);
        }
        // Check for data existence within the extras
        else if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {

                retrieveDataFromIntent(intentThatStartedThisActivity);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putParcelable(REVIEWS_STATE, Parcels.wrap(mReviews));

        super.onSaveInstanceState(outState);
    }

    /**
     * Retrieves the data from the saved instance bundle
     * @param savedInstanceState {@link Bundle} the given saved instance state bundle
     */
    private void retrieveDataFromSavedInstance(Bundle savedInstanceState) {
        mReviews = Parcels.unwrap(savedInstanceState.getParcelable(REVIEWS_STATE));
        mReviewsAdapter.setReviewsData(mReviews);
    }

    /**
     * Retrieves the data from the started intent
     * @param intentThatStartedThisActivity {@link Intent} the given started intent
     */
    private void retrieveDataFromIntent(Intent intentThatStartedThisActivity) {
        // Retrieve the json object and deserialize it back to object
        mMovieId = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT);

        if (mMovieId != null && !mMovieId.equals("")) {

            fetcherTask = new FetcherTask();
            fetcherTask.mView = this;
            fetcherTask.execute();

        } else {
            Toast.makeText(this, getString(R.string.no_available_reviews), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Shows the recycler view and hides the error message
     */
    private void showContent() {

        mRecyclerView.setVisibility(View.VISIBLE);
        mNoReviews.setVisibility(View.INVISIBLE);
    }

    /**
     * Shows the error message and hides the recycler view
     */
    private void showNoAvailableContent() {

        mRecyclerView.setVisibility(View.INVISIBLE);
        mNoReviews.setVisibility(View.VISIBLE);
    }

    /**
     * Prepares the requested URL for the Reviews for Async Task
     * @return {@link URL} the request URL
     */
    @Override
    public URL getFetcherUrl() {

        LinkedHashSet<String> paths = new LinkedHashSet<>();
        paths.add(mMovieId);
        paths.add(REVIEWS_URL_PATH);
        return NetworkUtils.buildUrl(paths, null);

    }

    /**
     * For generic implementation the class of Pojo is needed
     * @return {@link Class} the Reviews Object class
     */
    @Override
    public Class getFetchingClass() {
        return ReviewsPojo.class;
    }

    @Override
    public void updateFetchingData(Object data) {

        if (data != null && ReviewsPojo.class.isInstance(data)) {
            showContent();
            mReviews = (ReviewsPojo) data;
            if (mReviews.getTotal_results() <= 0) {
                showNoAvailableContent();
            }
            mReviewsAdapter.setReviewsData(mReviews);
        } else {
            showNoAvailableContent();
        }

    }

    /**
     * No action before fetching the data
     */
    @Override
    public void onPreFetchingData() {

    }

}
