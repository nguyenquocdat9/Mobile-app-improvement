package com.example.myapplication.ui.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.material.appbar.AppBarLayout;

public class NonSwipeableAppBarLayout extends AppBarLayout {

    public NonSwipeableAppBarLayout(Context context) {
        super(context);
    }

    public NonSwipeableAppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Không intercept touch event: ngăn người dùng vuốt trực tiếp trên AppBar để collapse
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Không xử lý touch event
        return true;
    }
}

