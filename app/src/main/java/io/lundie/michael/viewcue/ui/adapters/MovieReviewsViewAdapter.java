package io.lundie.michael.viewcue.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.lundie.michael.viewcue.R;
import io.lundie.michael.viewcue.datamodel.models.review.MovieReviewItem;
import io.lundie.michael.viewcue.ui.views.RecycleViewWithSetEmpty;

/**
 * View adapter for binding review data with the UI
 */
public class MovieReviewsViewAdapter extends RecycleViewWithSetEmpty.Adapter<MovieReviewsViewAdapter.ViewHolder> {

    private static final String LOG_TAG = MovieReviewsViewAdapter.class.getName();

    private ArrayList<MovieReviewItem> mValues;
    private String textReadMore;
    private String textHide;

    public MovieReviewsViewAdapter(ArrayList<MovieReviewItem> items, String textReadMore, String textHide) {
        this.mValues = items;
        this.textReadMore = textReadMore;
        this.textHide = textHide;
    }

    @Override
    public void onBindViewHolder(@NonNull MovieReviewsViewAdapter.ViewHolder holder, int position) {
        holder.bind(mValues.get(position));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.review_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecycleViewWithSetEmpty.ViewHolder {

        // Bind views using butterknife
        @BindView(R.id.review_author_tv) TextView mReviewAuthorTv;
        @BindView(R.id.review_content_tv) TextView mReviewContentTv;
        @BindView(R.id.read_more_btn) TextView mReadMoreBtn;

        boolean isFullTextVisible = false;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
        /**
         * Method used to bind data to our view
         * @param item the review item to be bound
         */
        void bind(final MovieReviewItem item) {

            mReviewAuthorTv.setText(item.getAuthor());
            mReviewContentTv.setText(item.getContent());
            String reviewText = item.getContent();

            // Let's check for ridiculously long review text.
            if(reviewText.length() > 300) {
                //Our text exceeds our limit, so let's create a read more link.
                mReadMoreBtn.setVisibility(View.VISIBLE);
                mReadMoreBtn.setText(textReadMore);

                // Regular Expression pattern to check where we can safely split any long reviews.
                Pattern pattern = Pattern.compile("\\b.{1," + 299 + "}\\b\\W?");
                Matcher matcher = pattern.matcher(reviewText);
                int splitCharacterIndex = 0;

                // Use pattern matcher to find an appropriate place to split review data.
                while (matcher.find()) {
                    splitCharacterIndex = matcher.end();
                    break;
                }

                final String condensedText = reviewText.substring(0, splitCharacterIndex);
                final String fullText = condensedText + reviewText.substring(splitCharacterIndex);

                mReviewContentTv.setText(condensedText);

                // Set-up click listener allowing user to display fulltext results of review.
                mReadMoreBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!isFullTextVisible) {
                            // If the full review text is visible, hide it.
                            mReviewContentTv.setText(fullText);
                            mReadMoreBtn.setText(textHide);
                            isFullTextVisible = true;
                        } else {
                            // Show the complete review text
                            mReviewContentTv.setText(condensedText);
                            mReadMoreBtn.setText(textReadMore);
                            isFullTextVisible = false;
                        }
                    }
                });
            } else {
                mReviewContentTv.setText(item.getContent());
            }
        }
    }
}