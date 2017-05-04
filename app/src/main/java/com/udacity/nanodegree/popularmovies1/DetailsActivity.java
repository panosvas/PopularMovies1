package com.udacity.nanodegree.popularmovies1;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.udacity.nanodegree.popularmovies1.Objects.MovieDetailsPojo;
import com.udacity.nanodegree.popularmovies1.Objects.TrailerPojo;
import com.udacity.nanodegree.popularmovies1.Objects.TrailersPojo;
import com.udacity.nanodegree.popularmovies1.data.MovieColumns;
import com.udacity.nanodegree.popularmovies1.data.MovieProvider;
import com.udacity.nanodegree.popularmovies1.utilities.FetcherTask;
import com.udacity.nanodegree.popularmovies1.utilities.GsonClient;
import com.udacity.nanodegree.popularmovies1.utilities.NetworkUtils;

import org.parceler.Parcels;

import java.net.URL;
import java.util.LinkedHashSet;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Responsible for handling and displaying the Movie Details view
 */
public class DetailsActivity extends AppCompatActivity implements TrailersAdapter.TrailersAdapterOnClickHandler, ViewTask{

    // Current format: YYYY-MM-dd, Example 2016-05-04
    private final String DATE_DELIMETER = "-";
    // We want the 1/3 of the screen size
    private final int THUMBNAIL_SCREEN_DIVIDER = 3;

    private static final String MOVIE_DETAILS_STATE = "movieDetails";
    private static final String TRAILERS_STATE = "trailers";
    private static final String FAVORITE_STATE = "favorite";
    private static final String LOCAL_DB_ID_STATE = "dbId";
    private static final String TRAILERS_URL_PATH = "videos";

    private static final String YOUTUBE_BASE_URL = "http://www.youtube.com/watch?v=";

    @BindView(R.id.movie_title)
    TextView mTitle;

    @BindView(R.id.movie_plot)
    TextView mSynopsis;

    @BindView(R.id.movie_date)
    TextView mDate;

    @BindView(R.id.movie_rating)
    TextView mRating;

    @BindView(R.id.movie_thumbnail)
    ImageView mThumbnail;

    @BindView(R.id.star)
    ImageView mStar;

    @BindView(R.id.reviews)
    Button mReviews;

    @BindView(R.id.recyclerview_trailers)
    RecyclerView mRecyclerView;

    private LinearLayoutManager layoutManager;
    private TrailersAdapter mTrailersAdapter;
    private MovieDetailsPojo movieDetails;
    private TrailersPojo mTrailersData;

    private boolean mIsFavorite;
    private long mLocalMovieId;

    private FetcherTask fetcherTask;

    /**
     * A transformation to match the desired width inside the details view for the thumbnail
     */
    final Transformation thumbnailTransformation = new Transformation() {

        @Override
        public Bitmap transform(Bitmap source) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int targetWidth = size.x / THUMBNAIL_SCREEN_DIVIDER;
            double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
            int targetHeight = (int) (targetWidth * aspectRatio);
            Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
            if (result != source) {
                source.recycle();
            }
            return result;
        }

        @Override
        public String key() {
            return "transformation" + " thumbnail";
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ButterKnife.bind(this);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setFocusable(false);

        mTrailersAdapter = new TrailersAdapter(this);
        mRecyclerView.setAdapter(mTrailersAdapter);
        mRecyclerView.setLayoutManager(layoutManager);

        Intent intentThatStartedThisActivity = getIntent();

        if (savedInstanceState != null) {

            retrieveDataFromSavedInstance(savedInstanceState);

        } else if (intentThatStartedThisActivity != null && intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {

            retrieveDataFromIntent(intentThatStartedThisActivity);

        }

    }

    /**
     * Retrieves the data from the saved instance bundle
     * @param savedInstanceState {@link Bundle} the given saved instance state bundle
     */
    private void retrieveDataFromSavedInstance(Bundle savedInstanceState) {

        if (savedInstanceState.containsKey(MOVIE_DETAILS_STATE)) {
            movieDetails = Parcels.unwrap(savedInstanceState.getParcelable(MOVIE_DETAILS_STATE));
            setMovieDetails(movieDetails);
        }

        if (savedInstanceState.containsKey(TRAILERS_STATE)) {
            mTrailersData = Parcels.unwrap(savedInstanceState.getParcelable(TRAILERS_STATE));
            mTrailersAdapter.setTrailersData(mTrailersData);
        }

        if (savedInstanceState.containsKey(FAVORITE_STATE)) {
            mIsFavorite = savedInstanceState.getBoolean(FAVORITE_STATE, false);
        }

        if (savedInstanceState.containsKey(LOCAL_DB_ID_STATE)) {
            mLocalMovieId = savedInstanceState.getLong(LOCAL_DB_ID_STATE);
        }

    }

    /**
     * Retrieves the data from the started intent
     * @param intentThatStartedThisActivity {@link Intent} the given started intent
     */
    private void retrieveDataFromIntent(Intent intentThatStartedThisActivity) {

        // Retrieve the json object and deserialize it back to object
        String movieDetailsJson = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT);

        if (movieDetailsJson != null && !movieDetailsJson.equals("")) {
            movieDetails = GsonClient.getInstance().fromJson(movieDetailsJson, MovieDetailsPojo.class);
            setMovieDetails(movieDetails);

            if (intentThatStartedThisActivity.hasExtra(MoviesActivity.FAVORITE_STATUS)) {
                mIsFavorite = intentThatStartedThisActivity.getBooleanExtra(MoviesActivity.FAVORITE_STATUS, false);
                mLocalMovieId = intentThatStartedThisActivity.getIntExtra(MoviesActivity.LOCAL_DB_ID, -1);
                mStar.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.filled_star));
            } else {
                mIsFavorite = false;
            }

            fetcherTask = new FetcherTask();
            fetcherTask.mView = this;
            fetcherTask.execute();

        } else {
            Toast.makeText(this, getString(R.string.no_available_details), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Responsible for handling the retrieved data and display them to the view
     *
     * @param movie {@link MovieDetailsPojo} object with the movie details data
     */
    private void setMovieDetails(MovieDetailsPojo movie) {

        if (movie != null) {

            if (movie.getThumbnail() != null) {
                Picasso.with(this).load(movie.getThumbnail()).transform(thumbnailTransformation).placeholder(R.drawable.progress_animation).into(mThumbnail);
            }

            mTitle.setText(movie.getTitle() != null ? movie.getTitle() : getString(R.string.unknown));
            mSynopsis.setText(movie.getSynopsis() != null ? movie.getSynopsis() : getString(R.string.unknown));
            mRating.setText(movie.getRating() != null ? movie.getRating() : getString(R.string.unknown));

            setReleaseYear(movie.getDate());

        }

    }

    /**
     * Takes a formatted date string and exports the Year
     *
     * @param releaseDate {@link String} object with the formatted date information
     */
    private void setReleaseYear(String releaseDate) {

        String year = getString(R.string.unknown);
        if (releaseDate != null) {
            String[] dateComponents = releaseDate.split(DATE_DELIMETER);
            if (dateComponents.length == 3) {
                year = dateComponents[0];
            }
        }

        mDate.setText(year);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putParcelable(MOVIE_DETAILS_STATE, Parcels.wrap(movieDetails));
        outState.putParcelable(TRAILERS_STATE, Parcels.wrap(mTrailersData));
        outState.putBoolean(FAVORITE_STATE, mIsFavorite);
        outState.putLong(LOCAL_DB_ID_STATE, mLocalMovieId);
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onClick(TrailerPojo trailer) {

        if (trailer != null) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_BASE_URL + trailer.getKey())));
        }
    }

    /**
     * Prepares the trailers URL for TMDb
     * @return {@link URL} with the requested URL
     */
    @Override
    public URL getFetcherUrl() {

        LinkedHashSet<String> paths = new LinkedHashSet<>();
        paths.add(String.valueOf(movieDetails.getId()));
        paths.add(TRAILERS_URL_PATH);
        return NetworkUtils.buildUrl(paths, null);

    }

    /**
     * Send to generic Async Task the desired Pojo
     * @return {@link Class} the Trailers Pojo
     */
    @Override
    public Class getFetchingClass() {
        return TrailersPojo.class;
    }

    /**
     * Updates the UI with the fetched data
     * @param data {@link Object} with the returned data from Async Task
     */
    @Override
    public void updateFetchingData(Object data) {

        if (data != null && TrailersPojo.class.isInstance(data)) {
            mTrailersData = (TrailersPojo) data;
            mTrailersAdapter.setTrailersData(mTrailersData);
        }
    }

    /**
     * No action before fetching the data
     */
    @Override
    public void onPreFetchingData() {

    }

    /**
     * Add or Remove a Movie from Favorites using Content Resolver
     * @param view {@link View} the View of Click event
     */
    public void onStarClick(View view) {

        // if it is not favorite add it, otherwise remove it
        if (!mIsFavorite) {

            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MovieColumns.MOVIE_ID, movieDetails.getId());
                contentValues.put(MovieColumns.TITLE, movieDetails.getTitle());
                contentValues.put(MovieColumns.DATE, movieDetails.getDate());
                contentValues.put(MovieColumns.RATING, movieDetails.getRating());
                contentValues.put(MovieColumns.SYNOPSIS, movieDetails.getSynopsis());
                contentValues.put(MovieColumns.THUMBNAIL, movieDetails.getPoster());
                Uri insertedMovieUri = getContentResolver().insert(MovieProvider.Movies.CONTENT_URI, contentValues);
                mLocalMovieId = ContentUris.parseId(insertedMovieUri);
                mIsFavorite = true;
                mStar.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.filled_star));
                Toast.makeText(this, getString(R.string.added_to_favorites), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                getContentResolver().delete(MovieProvider.Movies.withId(mLocalMovieId), null, null);
                mIsFavorite = false;
                mStar.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.empty_star));
                Toast.makeText(this, getString(R.string.removed_from_favorites), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Go to reviews Activity
     * @param view {@link View} the View of Click event
     */
    public void onReviewsClick(View view) {

        if (movieDetails != null) {

            Context context = DetailsActivity.this;
            Class destinationClass = ReviewsActivity.class;
            Intent intentToStartReviewsActivity = new Intent(context, destinationClass);

            // Send a json object as text parameter in the intent extras
            String movieId = String.valueOf(movieDetails.getId());
            intentToStartReviewsActivity.putExtra(Intent.EXTRA_TEXT, movieId);
            startActivity(intentToStartReviewsActivity);
        }

    }
}
