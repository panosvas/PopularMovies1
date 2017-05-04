package com.udacity.nanodegree.popularmovies1;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.nanodegree.popularmovies1.Objects.TrailerPojo;
import com.udacity.nanodegree.popularmovies1.Objects.TrailersPojo;

/**
 * The RecyclerView Adapter for Trailers
 */
public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailersAdapterViewHolder> {

    private final TrailersAdapterOnClickHandler mClickHandler;
    private TrailersPojo trailersData;

    /**
     * Constructor of the TrailersAdapter with click handler initialization
     *
     * @param clickHandler {@link TrailersAdapterOnClickHandler} for registering click events
     */
    public TrailersAdapter(TrailersAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @Override
    public TrailersAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.trailers_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new TrailersAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TrailersAdapterViewHolder trailersAdapterViewHolder, int position) {
        String trailerName = trailersData.getResults().get(position).getName();
        trailersAdapterViewHolder.mNameTextView.setText(trailerName);
    }

    @Override
    public int getItemCount() {
        if (null == trailersData) {
            return 0;
        }
        return trailersData.getResults().size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /**
     * Updates the adapter with new trailers data
     *
     * @param trailers {@link TrailersPojo} object with the new data
     */
    public void setTrailersData(TrailersPojo trailers) {
        // Update the list if new ones arrive
        if (trailers != null && trailers.getResults() != null && trailers.getResults().size() > 0) {
            trailersData = trailers;
        }
        notifyDataSetChanged();
    }

    /**
     * The interface that receives onClick messages.
     */
    public interface TrailersAdapterOnClickHandler {
        void onClick(TrailerPojo trailer);
    }

    /**
     * Cache of the children views for a Trailer item.
     */
    public class TrailersAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView mNameTextView;

        public TrailersAdapterViewHolder(View view) {
            super(view);
            mNameTextView = (TextView) view.findViewById(R.id.trailer_title);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();

            TrailerPojo trailer = trailersData.getResults().get(adapterPosition);

            mClickHandler.onClick(trailer);
        }
    }

}
