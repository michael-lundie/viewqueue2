package io.lundie.michael.viewcue.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.R;

/**
 * RecycleView adapter linking our data to the recycler view and determining view holders required
 * for data display.
 */
public class MovieResultsViewAdapter extends RecyclerView.Adapter<MovieResultsViewAdapter.ViewHolder> {

    private static final String LOG_TAG = MovieResultsViewAdapter.class.getSimpleName();

    /**
     * Used tutorial at https://antonioleiva.com/recyclerview-listener/ for setting up interface.
     */
    public interface OnItemClickListener {
        void onItemClick(MovieItem item);
    }

    private ArrayList<MovieItem> mValues;
    private final OnItemClickListener mListener;

    /**
     * Simple constructor class for adapter.
     * @param items ArrayList of MovieItem objects.
     * @param listener Reference to our listener object.
     */
    public MovieResultsViewAdapter(ArrayList<MovieItem> items, OnItemClickListener listener) {
        mValues = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate the layout for our view holder.
        View view = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.movie_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieResultsViewAdapter.ViewHolder holder, int position) {
        holder.bind(mValues.get(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setMovieEntries(ArrayList<MovieItem> movieEntries) {
        mValues = movieEntries;
        notifyDataSetChanged();
    }

    /**
     * Extended view holder class, allowing with references to view objects.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        final View mView;
        @BindView(R.id.title) TextView mTitleView;
        @BindView(R.id.poster) ImageView mPosterView;
        @BindView(R.id.favicon) ImageView mFavicon;

        ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);
        }

        void bind(final MovieItem item, final OnItemClickListener listener) {
            mTitleView.setText(item.getTitle());

            if(item.getFavorite() == 1) {
                mFavicon.setImageResource(R.drawable.ic_star_filled);
            } else {
                // Even though this is the default card view favicon, we programmatically set the
                // resource or recycler view won't display the correct icon when recycling views.
                mFavicon.setImageResource(R.drawable.ic_star);
            }

            Picasso.get()
                    .load(item.getPosterURL())
                   .into(mPosterView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Hide our progress bar view on completion of image download.

                    }
                    @Override
                       public void onError(Exception e) {

                    }
                   });

            //Set up our onClickListener interface up.
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}