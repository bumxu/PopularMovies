package com.bumxu.android.lab;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.bumxu.android.popularmovies.app.R;

@SuppressWarnings("unused")
public class UniformFrameLayout extends FrameLayout {
    private static final int CONSTANT_WIDTH = 0;
    private static final int CONSTANT_HEIGHT = 1;

    private double mWidthRatio;
    private double mHeightRatio;
    private int mConstant;

    public UniformFrameLayout(@NonNull Context context) {
        super(context);
        applyAttributes(null);
    }

    public UniformFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        applyAttributes(attrs);
    }

    public UniformFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyAttributes(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UniformFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        applyAttributes(attrs);
    }

    private void applyAttributes(AttributeSet attrs) {
        if (attrs == null) {
            mWidthRatio = 1;
            mHeightRatio = 1;

            mConstant = CONSTANT_WIDTH;
        } else {
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.UniformFrameLayout);

            mWidthRatio = array.getFloat(R.styleable.UniformFrameLayout_ufl_widthRatio, 1);
            mHeightRatio = array.getFloat(R.styleable.UniformFrameLayout_ufl_heightRatio, 1);

            mConstant = array.getInt(R.styleable.UniformFrameLayout_ufl_constant, 0);

            array.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Pair<Integer, Integer> dim;

        if (mConstant == CONSTANT_WIDTH) {
            dim = measureWithConstantWidth(widthMeasureSpec);
        } else {
            dim = measureWithConstantHeight(heightMeasureSpec);
        }

        super.setMeasuredDimension(dim.first, dim.second);

        super.onMeasure(dim.first, dim.second);
    }

    private Pair<Integer, Integer> measureWithConstantWidth(int widthMeasureSpec) {
        int vPadding = getPaddingTop() + getPaddingBottom();
        int hPadding = getPaddingLeft() + getPaddingRight();

        int widthSize = MeasureSpec.getSize(widthMeasureSpec) - hPadding;
        int heightSize = (int) (mHeightRatio / mWidthRatio * widthSize) + vPadding;

        int newHeightSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);

        return new Pair<>(widthMeasureSpec, newHeightSpec);
    }

    private Pair<Integer, Integer> measureWithConstantHeight(int heightMeasureSpec) {
        int vPadding = getPaddingTop() + getPaddingBottom();
        int hPadding = getPaddingLeft() + getPaddingRight();

        int heightSize = MeasureSpec.getSize(heightMeasureSpec) - vPadding;
        int widthSize = (int) (mHeightRatio / mWidthRatio * heightSize) + hPadding;

        int newWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);

        return new Pair<>(newWidthSpec, heightMeasureSpec);
    }
}
