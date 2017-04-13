package com.bumxu.android.popularmovies.app.ui;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import it.sephiroth.android.library.bottomnavigation.BottomNavigation;


/**
 * Gives to a snackbar container the right translation relative to a BottomNavigation.
 */
public class SnackbarHolderBehavior extends CoordinatorLayout.Behavior<CoordinatorLayout> {
    public SnackbarHolderBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, CoordinatorLayout child, View dependency) {
        return dependency instanceof BottomNavigation;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, CoordinatorLayout child, View dependency) {
        if (dependency instanceof BottomNavigation) {
            child.setTranslationY(-((BottomNavigation) dependency).getNavigationHeight() + dependency.getTranslationY());

            return true;
        }
        return false;
    }
}