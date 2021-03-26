package com.example.presell;

import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.presell.activities.MainActivity;
import com.example.presell.adapters.PostRecyclerAdapter;

import java.util.List;

public class SnapScrollListener extends RecyclerView.OnScrollListener {
    private List<String> categoriesList;
    private RecyclerView mFeedRecyclerView;
    private MainActivity mActivity;
    private int oldPosition;

    public SnapScrollListener(MainActivity activity, RecyclerView feedRecyclerView, List<String> categoriesList){
        this.categoriesList = categoriesList;
        mActivity = activity;
        mFeedRecyclerView = feedRecyclerView;
        oldPosition = -1;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView mRecyclerView, int newState) {
//        this.mRecyclerView = mRecyclerView;
        if (RecyclerView.SCROLL_STATE_IDLE == newState) {
            final int scrollDistance = getScrollDistanceOfColumnClosestToLeft(mRecyclerView);
            if (scrollDistance != 0) {
                mRecyclerView.smoothScrollBy(scrollDistance, 0);
            }
            else {
                //TODO: ISSUE WITH COASTER INDEX, FIX
                LinearLayoutManager mManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                if (mManager != null) {
                    int position = mManager.findFirstCompletelyVisibleItemPosition();
                    if (oldPosition != position) {
                        PostRecyclerAdapter feedRecyclerAdapter = (PostRecyclerAdapter) mFeedRecyclerView.getAdapter();
                        if (feedRecyclerAdapter != null) feedRecyclerAdapter.clearPosts();
                        mActivity.getCategoryFeed(categoriesList.get((position + 1) % categoriesList.size()));
                        oldPosition = position;
                    }
                }
            }
        }
        mRecyclerView.addOnItemTouchListener(mTouchListener);
    }

    private int getScrollDistanceOfColumnClosestToLeft(final RecyclerView recyclerView) {
        final LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
        final RecyclerView.ViewHolder firstVisibleColumnViewHolder = recyclerView.findViewHolderForAdapterPosition(manager.findFirstVisibleItemPosition());
        if (firstVisibleColumnViewHolder == null) {
            return 0;
        }
        final int columnWidth = firstVisibleColumnViewHolder.itemView.getMeasuredWidth();
        final int left = firstVisibleColumnViewHolder.itemView.getLeft();
        final int absoluteLeft = Math.abs(left);
        return absoluteLeft <= (columnWidth / 2) ? left : columnWidth - absoluteLeft;
    }


    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        Log.d("SnapScrollListener", "onScrolled()");
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
        int firstItemVisible = linearLayoutManager.findFirstVisibleItemPosition();
        if (firstItemVisible != 1 && firstItemVisible % categoriesList.size() == 1) {
            linearLayoutManager.scrollToPosition(1);
        }
        int firstCompletelyItemVisible = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
        if (firstCompletelyItemVisible == 0) {
            linearLayoutManager.scrollToPositionWithOffset(categoriesList.size(), 0);
        }
    }

    private final RecyclerView.OnItemTouchListener mTouchListener = new RecyclerView.OnItemTouchListener() {

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    };

}