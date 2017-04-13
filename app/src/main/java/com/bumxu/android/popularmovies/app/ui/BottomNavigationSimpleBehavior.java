package com.bumxu.android.popularmovies.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import it.sephiroth.android.library.bottomnavigation.BottomNavigation;


/**
 * Shows/hides the BottomNavigation, nothing more.
 */
public class BottomNavigationSimpleBehavior extends CoordinatorLayout.Behavior<BottomNavigation> {
    private static final Interpolator INTERPOLATOR = new LinearOutSlowInInterpolator();

    private boolean mVisible = true;

    public BottomNavigationSimpleBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, BottomNavigation child, View directTargetChild, View target, int nestedScrollAxes) {
        return target instanceof RecyclerView;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, BottomNavigation child, View target, int dx, int dy, int[] consumed) {
        if (dy < 0 && !mVisible) {
            showBottomNav(child);
        } else if (dy > 0 && mVisible) {
            hideBottomNav(child);
        }
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, BottomNavigation child) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("visible", mVisible);

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, BottomNavigation child, Parcelable state) {
        if (((Bundle) state).getBoolean("visible", true)) {
            showBottomNavNow(child);
        } else {
            hideBottomNavNow(child);
        }
    }

    private void showBottomNav(BottomNavigation bn) {
        mVisible = true;
        bn.animate().setInterpolator(INTERPOLATOR).translationY(0);
    }

    private void hideBottomNav(BottomNavigation bn) {
        mVisible = false;
        bn.animate().setInterpolator(INTERPOLATOR).translationY(bn.getNavigationHeight());
    }

    private void showBottomNavNow(BottomNavigation bn) {
        mVisible = true;
        bn.setTranslationY(0);
    }

    private void hideBottomNavNow(BottomNavigation bn) {
        mVisible = false;
        bn.setTranslationY(bn.getNavigationHeight());
    }
}