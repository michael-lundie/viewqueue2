package io.lundie.michael.viewcue;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ParallaxNestedScrollView extends NestedScrollView {
    private static final String LOG_TAG = ParallaxNestedScrollView.class.getSimpleName();

    public ParallaxNestedScrollView(@NonNull Context context) {
        super(context);
    }

    public ParallaxNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ParallaxNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        Log.i(LOG_TAG, "TEST: onStartNestedScroll called");
        return super.onStartNestedScroll(child, target, nestedScrollAxes);

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.i(LOG_TAG, "Touch event intercepted ");
        return super.onTouchEvent(ev);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.i(LOG_TAG, "TEST: onNestedScroll, dxConsumed: " + dxConsumed);
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

    }
}
