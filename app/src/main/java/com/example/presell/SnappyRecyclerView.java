package com.example.presell;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public final class SnappyRecyclerView extends RecyclerView {
    private long eventDuration;

    public SnappyRecyclerView(Context context) {
        super(context);
    }

    public SnappyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SnappyRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        final LayoutManager lm = getLayoutManager();

        if (lm instanceof ISnappyLayoutManager) {
            super.smoothScrollToPosition(((ISnappyLayoutManager) getLayoutManager())
                    .getPositionForVelocity(velocityX, velocityY));
            return true;
        }
        return super.fling(velocityX, velocityY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // We want the parent to handle all touch events--there's a lot going on there,
        // and there is no reason to overwrite that functionality--bad things will happen.
        final boolean ret = super.onTouchEvent(e);
        final LayoutManager lm = getLayoutManager();
        Log.d("SnappyRecyclerView", "onTouchEvent() neither block");

        eventDuration = e.getEventTime()-e.getDownTime();
        if(e.getAction() == MotionEvent.ACTION_UP) {
            if(eventDuration > 2000) {
                if (lm instanceof ISnappyLayoutManager
                        && (e.getAction() == MotionEvent.ACTION_UP ||
                        e.getAction() == MotionEvent.ACTION_CANCEL)
                        && getScrollState() == SCROLL_STATE_IDLE) {
                    Log.d("SnappyRecyclerView", "onTouchEvent() if block " + ((ISnappyLayoutManager) lm).getFixScrollPos());
                    smoothScrollToPosition(((ISnappyLayoutManager) lm).getFixScrollPos());
                }
            }
            else{
                if(lm instanceof ISnappyLayoutManager){
                    Log.d("SnappyRecyclerView", "onTouchEvent() else block " + ((ISnappyLayoutManager) lm).getFixScrollPos());
                    scrollToPosition(((ISnappyLayoutManager) lm).getFixScrollPos());
                }
            }
        }

        return ret;
    }
}