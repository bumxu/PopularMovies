package com.bumxu.android.popularmovies.app.ui;

import android.content.Context;
import android.content.res.Resources;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.bumxu.android.popularmovies.app.R;

/**
 * Let the background see through a toolbar when there is nothing under it.
 */
public class ToolbarFadeBehavior extends CoordinatorLayout.Behavior<RelativeLayout> implements NestedScrollView.OnScrollChangeListener {
    private static final double MIN_ALPHA = 0.5;

    private View mToolbarHolder;
    private View mToolbarBackground;

    public ToolbarFadeBehavior() {
        super();
    }

    public ToolbarFadeBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, final RelativeLayout container, View dependency) {
        if (mToolbarBackground == null) {
            saveToolbarInstance(container);
        }

        if (dependency instanceof NestedScrollView) {
            ((NestedScrollView) dependency).setOnScrollChangeListener(this);
        }

        return false;
    }

    private void saveToolbarInstance(View container) {
        mToolbarHolder = container;
        mToolbarBackground = container.findViewById(R.id.toolbar_bg);

        if (mToolbarBackground == null) {
            throw new Resources.NotFoundException("Toolbar background view was not found in provided container.");
        }

        mToolbarBackground.setAlpha((float) 0.4);
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        applyAlpha(scrollY);
    }

    private void applyAlpha(int scroll) {
        double offset = Math.min((double) scroll / mToolbarHolder.getHeight(), 1);
        double alpha = MIN_ALPHA + (1 - MIN_ALPHA) * offset;

        mToolbarBackground.setAlpha((float) alpha);
    }
}
