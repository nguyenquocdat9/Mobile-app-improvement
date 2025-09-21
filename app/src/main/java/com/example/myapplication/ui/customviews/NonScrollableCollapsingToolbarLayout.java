package com.example.myapplication.ui.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.material.appbar.CollapsingToolbarLayout;

public class NonScrollableCollapsingToolbarLayout extends CollapsingToolbarLayout {

    public NonScrollableCollapsingToolbarLayout(Context context) {
        super(context);
    }

    public NonScrollableCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonScrollableCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Không cho phép intercept sự kiện chạm
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Không xử lý bất kỳ sự kiện chạm nào
        return false;
    }
}