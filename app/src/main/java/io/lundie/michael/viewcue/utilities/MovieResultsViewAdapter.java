package io.lundie.michael.viewcue.utilities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.lundie.michael.viewcue.DetailActivity;
import io.lundie.michael.viewcue.MovieItem;
import io.lundie.michael.viewcue.R;

/**
 * RecycleView adapter linking our data to the recycler view and determining view holders required
 * for data display.
 */
public class MovieResultsViewAdapter extends RecyclerView.Adapter<MovieResultsViewAdapter.ViewHolder> {

    private static final String LOG_TAG = MovieResultsViewAdapter.class.getSimpleName();

    private Context mContext;
    private final ArrayList<MovieItem> mValues;
    private final int mPadding;

    /**
     * Simple constructor class for adapter.
     * @param items ArrayList of MovieItem objects.
     * @param context Current context.
     * @param padding Adapter padding.
     */
    public MovieResultsViewAdapter(ArrayList<MovieItem> items, Context context, int padding) {
        mValues = items;
        mContext = context;
        mPadding = padding;
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

        holder.mItem = mValues.get(position);

        // Create a final reference variable to our movie object for this item.
        // Used to access from onClick method.
        final MovieItem movie = holder.mItem;
        holder.mTitleView.setText(mValues.get(position).getTitle());

        // Use Glide library to load our poster image. Async and cache is automatically managed
        // by Glide.
        Glide.with(mContext)
                .load(mValues.get(position).getPosterURL())
                .apply(new RequestOptions().placeholder(R.drawable.light_solid).error(R.drawable.light_solid))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.mPosterView);

        // Create an onClickListener.
        holder.mPosterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On click, create an intent and marshall necessary data using our parcelable
                // MovieItem object, and start our new activity.
                Intent openDetailIntent = new Intent(mContext, DetailActivity.class);
                openDetailIntent.putExtra("movie", movie);
                mContext.startActivity(openDetailIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /**
     * Extended view holder class, allowing with references to view objects.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        MovieItem mItem;
        final View mView;
        @BindView(R.id.title) TextView mTitleView;
        @BindView(R.id.poster) ImageView mPosterView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);
        }
    }
}