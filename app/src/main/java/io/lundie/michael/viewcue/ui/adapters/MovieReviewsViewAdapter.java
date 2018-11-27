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

public class MovieReviewsViewAdapter extends RecyclerView.Adapter<MovieReviewsViewAdapter.ViewHolder> {

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

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.review_author_tv) TextView mReviewAuthorTv;
        @BindView(R.id.review_content_tv) TextView mReviewContentTv;
        @BindView(R.id.read_more_btn) TextView mReadMoreBtn;

        boolean isFullTextVisible = false;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
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
                while (matcher.find()) {
                    splitCharacterIndex = matcher.end();
                    break;
                }
                final String condensedText = reviewText.substring(0, splitCharacterIndex);
                final String fullText = condensedText + reviewText.substring(splitCharacterIndex);

                mReviewContentTv.setText(condensedText);
                mReadMoreBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!isFullTextVisible) {
                            mReviewContentTv.setText(fullText);
                            mReadMoreBtn.setText(textHide);
                            isFullTextVisible = true;
                        } else {
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