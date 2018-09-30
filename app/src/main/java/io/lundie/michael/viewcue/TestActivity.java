/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 29/09/18 10:25
 */

package io.lundie.michael.viewcue;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.datamodel.MovieRepository;
import io.lundie.michael.viewcue.utilities.MovieResultsViewAdapter;
import io.lundie.michael.viewcue.viewmodel.MoviesViewModel;

public class TestActivity extends AppCompatActivity {

    private MovieResultsViewAdapter mAdapter;
    private ArrayList<MovieItem> mList = new ArrayList<>();

    @BindView(R.id.list_empty)
    TextView mEmptyStateTextView;
    @BindView(R.id.movie_list) RecycleViewWithSetEmpty mRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // Bind view references with butterknife library.
        ButterKnife.bind(this);

        // Set up our toolbar/action bar
        setSupportActionBar(mToolbar);

        // Set up Recycler view
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setEmptyView(mEmptyStateTextView);


        // Initiate our new custom recycler adapter and set layout manager.
        mAdapter = new MovieResultsViewAdapter(mList, new MovieResultsViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieItem item) {
                // On click, create an intent and marshall necessary data using our parcelable
                // MovieItem object, and start our new activity.
                Intent openDetailIntent = new Intent(TestActivity.this, DetailActivity.class);
                openDetailIntent.putExtra("movie", item);
                startActivity(openDetailIntent);
            }
        });

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setAdapter(mAdapter);

        MovieRepository.getInstance();

        MoviesViewModel model = ViewModelProviders.of(this).get(MoviesViewModel.class);

        model.getMovies().observe(this, new Observer<ArrayList<MovieItem>>() {
            @Override
            public void onChanged(@Nullable ArrayList<MovieItem> movieItems) {
                Log.i("TEST", "TEST Observer changed" +movieItems);
                mAdapter.setMovieEntries(movieItems);
                mAdapter.notifyDataSetChanged();
            }
        });

    }

    private void testMethod() {

    }
}
