package io.lundie.michael.viewcue;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;

/**
 * Recycler view with a 'set empty' view. Code from:
 * Recycler View with SetEmpty class: https://stackoverflow.com/a/30415582
 */
public class RecycleViewWithSetEmpty extends RecyclerView {
    private View emptyView;

    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {


        @Override
        public void onChanged() {
            Adapter<?> adapter =  getAdapter();
            if(adapter != null && emptyView != null) {
                if(adapter.getItemCount() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                    RecycleViewWithSetEmpty.this.setVisibility(View.GONE);
                }
                else {
                    emptyView.setVisibility(View.GONE);
                    RecycleViewWithSetEmpty.this.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    public RecycleViewWithSetEmpty(Context context) {
        super(context);
    }

    public RecycleViewWithSetEmpty(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecycleViewWithSetEmpty(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);

        if(adapter != null) {
            adapter.registerAdapterDataObserver(emptyObserver);
        }

        emptyObserver.onChanged();
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
    }
}
