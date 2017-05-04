package com.udacity.nanodegree.popularmovies1;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.udacity.nanodegree.popularmovies1.Objects.MovieDetailsPojo;
import com.udacity.nanodegree.popularmovies1.Objects.MoviesPojo;
import com.udacity.nanodegree.popularmovies1.Objects.Result;

/**
 * The RecyclerView Adapter for Movies
 */
public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {

    private static final String TAG = MoviesAdapter.class.getSimpleName();
    private static final String TMDB_IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String TMDB_IMAGE_SIZE = "w185/";

    private final MoviesAdapterOnClickHandler mClickHandler;
    private MoviesPojo moviesData;

    /**
     * Constructor of the MoviesAdapter with click handler initialization
     *
     * @param clickHandler {@link MoviesAdapterOnClickHandler} for registering click events
     */
    public MoviesAdapter(MoviesAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movies_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new MoviesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MoviesAdapterViewHolder moviesAdapterViewHolder, int position) {

        String imageUrl = TMDB_IMAGE_BASE_URL + TMDB_IMAGE_SIZE + moviesData.getResults().get(position).getPosterPath();
        Picasso.with(moviesAdapterViewHolder.itemView.getContext()).load(imageUrl).placeholder(R.drawable.progress_animation).into(moviesAdapterViewHolder.mImageView);
    }

    @Override
    public int getItemCount() {
        if (null == moviesData) {
            return 0;
        }
        return moviesData.getResults().size();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    /**
     * Updates the adapter with new movies data
     *
     * @param movies {@link MoviesPojo} object with the new data
     */
    public void setMoviesData(MoviesPojo movies) {

        // Update the list if new ones arrive, otherwise initialize it with the fresh data
        if (movies != null && moviesData != null && moviesData.getResults() != null && moviesData.getResults().size() > 0) {
            for (int i = 0; i < movies.getResults().size(); i++) {
                moviesData.getResults().add(movies.getResults().get(i));
            }
        } else {
            moviesData = movies;

        }
        notifyDataSetChanged();

        // Declare that the loading is finished
        MoviesActivity.loading = false;
    }

    /**
     * The interface that receives onClick messages.
     */
    public interface MoviesAdapterOnClickHandler {
        void onClick(MovieDetailsPojo movie);
    }

    /**
     * Cache of the children views for a Movie Poster list item.
     */
    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final ImageView mImageView;

        public MoviesAdapterViewHolder(View view) {
            super(view);
            mImageView = (ImageView) view.findViewById(R.id.movie_image);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();

            Result movie = moviesData.getResults().get(adapterPosition);
            MovieDetailsPojo movieDetails = buildMovieDetails(movie);

            mClickHandler.onClick(movieDetails);
        }
    }

    /**
     * Builds a minified version of Movie Object with necessary parameters
     * in order to reduce the transferred data load
     *
     * @param movie {@link Result} object to be minified
     * @return {@link MovieDetailsPojo} object with the necessary information
     */
    private MovieDetailsPojo buildMovieDetails(Result movie) {

        MovieDetailsPojo movieDetails = null;

        if (movie != null) {
            movieDetails = new MovieDetailsPojo();
            movieDetails.setId(movie.getId());
            movieDetails.setTitle(movie.getTitle());
            movieDetails.setDate(movie.getReleaseDate());
            movieDetails.setRating(String.valueOf(movie.getVoteAverage()));
            movieDetails.setSynopsis(movie.getOverview());
            movieDetails.setPoster(movie.getPosterPath());
            movieDetails.setThumbnail(TMDB_IMAGE_BASE_URL + TMDB_IMAGE_SIZE + movie.getPosterPath());
        }

        return movieDetails;
    }

}
