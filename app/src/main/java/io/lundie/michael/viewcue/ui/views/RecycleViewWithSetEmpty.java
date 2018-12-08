/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 22/09/18 12:59
 */

package io.lundie.michael.viewcue.ui.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Recycler view with a 'set empty' view. Code from:
 * Recycler View with SetEmpty class: http://alexzh.com/tutorials/how-to-setemptyview-to-recyclerview/
 */
public class RecycleViewWithSetEmpty extends RecyclerView {
    private View mEmptyView;
    public RecycleViewWithSetEmpty(Context context) {
        super(context);
    }
    public RecycleViewWithSetEmpty(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public RecycleViewWithSetEmpty(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    private void initEmptyView() {
        if (mEmptyView != null) {
            mEmptyView.setVisibility(
                    getAdapter() == null || getAdapter().getItemCount() == 0 ? VISIBLE : GONE);
            RecycleViewWithSetEmpty.this.setVisibility(
                    getAdapter() == null || getAdapter().getItemCount() == 0 ? GONE : VISIBLE);
        }
    }
    final AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            initEmptyView();
        }
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            initEmptyView();
        }
        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            initEmptyView();
        }
    };
    @Override
    public void setAdapter(Adapter adapter) {
        Adapter oldAdapter = getAdapter();
        super.setAdapter(adapter);
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }
    }
    public void setEmptyView(View view) {
        this.mEmptyView = view;
        initEmptyView();
    }
}