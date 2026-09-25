package com.blrp.game.gui.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;

import com.blrp.game.R;

import kotlin.jvm.internal.Intrinsics;

public class CustomRecyclerView extends RecyclerView {
    private boolean mEnableScrolling = true;
    private float itemHeightPercent;
    private float itemWidthPercent;
    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("ResourceType")
    public CustomRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Intrinsics.checkNotNullParameter(context, "context");
        this.mEnableScrolling = true;
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.CustomRecyclerView);
        this.itemWidthPercent = typedArrayObtainStyledAttributes.getFloat(2, 0.0f);
        this.itemHeightPercent = typedArrayObtainStyledAttributes.getFloat(1, 0.0f);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (isEnableScrolling()) {
            return CustomRecyclerView.super.onInterceptTouchEvent(motionEvent);
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (isEnableScrolling()) {
            return CustomRecyclerView.super.onTouchEvent(motionEvent);
        }
        return false;
    }

    public int getScrollForRecycler() {
        return computeVerticalScrollOffset();
    }

    public boolean isEnableScrolling() {
        return this.mEnableScrolling;
    }

    public void setEnableScrolling(boolean z) {
        this.mEnableScrolling = z;
    }
}
