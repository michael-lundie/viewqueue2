package io.lundie.michael.viewcue.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.lundie.michael.viewcue.R;

/**
 * Provides an empty placeholder fragment for the detail view
 */
public class EmptyDetailFragment extends Fragment {

    public EmptyDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_empty_detail, container, false);
    }
}
