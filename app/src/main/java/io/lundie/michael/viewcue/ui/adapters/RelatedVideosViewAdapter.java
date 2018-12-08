package io.lundie.michael.viewcue.ui.adapters;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.lundie.michael.viewcue.R;
import io.lundie.michael.viewcue.datamodel.models.item.MovieItem;
import io.lundie.michael.viewcue.datamodel.models.videos.RelatedVideos;
import io.lundie.michael.viewcue.ui.views.RecycleViewWithSetEmpty;

public class RelatedVideosViewAdapter extends RecycleViewWithSetEmpty.Adapter<RelatedVideosViewAdapter.ViewHolder>{

    private static final String LOG_TAG = RelatedVideosViewAdapter.class.getName();

    public interface OnItemClickListener { void onItemClick(String key); }

    private ArrayList<RelatedVideos> mValues;
    private final OnItemClickListener mListener;

    public RelatedVideosViewAdapter(ArrayList<RelatedVideos> items, OnItemClickListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedVideosViewAdapter.ViewHolder holder, int position) {
        holder.bind(mValues.get(position), mListener);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.related_videos_holder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        Log.v(LOG_TAG, "RELATED size: " + mValues.size());
        return mValues.size();
    }

    public class ViewHolder extends RecycleViewWithSetEmpty.ViewHolder {

        final View mView;
        @BindView(R.id.video_link) Button mVideoLink;

        ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);
        }
        void bind(final RelatedVideos item, final OnItemClickListener listener) {
            mVideoLink.setText(item.getName());

            //Set up our onClickListener interface up.
            mVideoLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(LOG_TAG, "TEST: adapter clicked");
                    //Create our listener using the url value generated above
                    listener.onItemClick(item.getKey());
                }
            });
        }
    }
}
