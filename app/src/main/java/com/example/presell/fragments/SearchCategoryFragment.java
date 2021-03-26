package com.example.presell.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.presell.R;
import com.example.presell.activities.MainActivity;
import com.example.presell.adapters.PostRecyclerAdapter;
import com.example.presell.models.GenAppInfo;

import java.util.Objects;

public class SearchCategoryFragment extends HomeFragment {
    private String category;
    private RecyclerView categoryRecyclerView;
    private PostRecyclerAdapter feedRecyclerAdapter;
    private boolean favoritesChanged;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        favoritesChanged = false;

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(requireContext(), "Refreshing Swipe", Toast.LENGTH_LONG).show();
                if(category!=null) ((MainActivity)requireActivity()).getCategoryFeed(category);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Don't add a custom options menu
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        ((MainActivity)requireActivity()).returnToSearch(feedRecyclerAdapter.getFavoritesChanged());
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setupExploreSpinner() {
        final Spinner exploreSpinner = requireView().findViewById(R.id.explore_spinner);
        TextView exploreText = requireView().findViewById(R.id.explore_text);

        exploreSpinner.setVisibility(View.INVISIBLE);
        exploreText.setVisibility(View.INVISIBLE);
    }

    public void categoryUpdated(){
        GenAppInfo mInfo = new GenAppInfo(requireContext().getApplicationContext());
        category = mInfo.getSelectedCategory();
        int categoryPosition = getCategoryPosition(category);

        ((LinearLayoutManager) Objects.requireNonNull(categoryRecyclerView.getLayoutManager())).scrollToPositionWithOffset(categoryPosition, 0);
        ((MainActivity)requireActivity()).getCategoryFeed(category);

        mInfo.setSelectedCategory("none");
    }

    @Override
    public void setupCategoriesPager() {
        super.setupCategoriesPager();
        categoryRecyclerView = requireView().findViewById(R.id.recycler_categories);
        TextView underlineTextView = requireView().findViewById(R.id.underline);
        underlineTextView.setVisibility(View.VISIBLE);
        categoryRecyclerView.setVisibility(View.VISIBLE);
    }

    // TODO: FIX LEFT DIRECTION CLICK
    @Override
    public void onItemClick(View view, int position) {
        if(position != 0) {
            String mCategory = categoriesList.get(position % categoriesList.size());
            Log.d("SearchCategoryFragment", "onClick selected Category: " + mCategory + " at position: " + position);
            swipeRefreshLayout.setRefreshing(true);
            RecyclerView mFeedRecyclerView = (requireView()).findViewById(R.id.feed_recyclerview);
            feedRecyclerAdapter = (PostRecyclerAdapter) mFeedRecyclerView.getAdapter();
            if (feedRecyclerAdapter != null) feedRecyclerAdapter.clearPosts();

            ((MainActivity) requireActivity()).getCategoryFeed(mCategory);

            ((LinearLayoutManager) Objects.requireNonNull(categoryRecyclerView.getLayoutManager())).scrollToPositionWithOffset(position - 1, 0);
        }
    }
}
