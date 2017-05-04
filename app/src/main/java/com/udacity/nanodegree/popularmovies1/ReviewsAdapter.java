package com.udacity.nanodegree.popularmovies1;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.nanodegree.popularmovies1.Objects.ReviewsPojo;

/**
 * The RecyclerView Adapter for Reviews
 */
public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterViewHolder> {

    private ReviewsPojo reviewsData;

    /**
     * Constructor of the ReviewsAdapter
     */
    public ReviewsAdapter() {
    }

    @Override
    public ReviewsAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.reviews_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new ReviewsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ReviewsAdapterViewHolder reviewsAdapterViewHolder, int position) {
        String author = reviewsData.getResults().get(position).getAuthor();
        String content = reviewsData.getResults().get(position).getContent();
        reviewsAdapterViewHolder.mAuthorTextView.setText(author);
        reviewsAdapterViewHolder.mContentTextView.setText(content);
    }

    @Override
    public int getItemCount() {
        if (null == reviewsData) {
            return 0;
        }
        return reviewsData.getResults().size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /**
     * Updates the adapter with new reviews data
     *
     * @param reviews {@link ReviewsPojo} object with the new data
     */
    public void setReviewsData(ReviewsPojo reviews) {
        // Update the list if new ones arrive, otherwise initialize it with the fresh data
        if (reviews != null && reviews.getResults() != null && reviews.getResults().size() > 0) {
            reviewsData = reviews;
        }
        notifyDataSetChanged();
    }

    /**
     * Cache of the children views for a Review list item.
     */
    public class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder {

        public final TextView mAuthorTextView;
        public final TextView mContentTextView;

        public ReviewsAdapterViewHolder(View view) {
            super(view);
            mAuthorTextView = (TextView) view.findViewById(R.id.review_title);
            mContentTextView = (TextView) view.findViewById(R.id.review_content);
        }
    }

}
