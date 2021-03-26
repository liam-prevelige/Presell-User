package com.example.presell.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.presell.R;
import com.example.presell.SnapScrollListener;
import com.example.presell.activities.CreateActivity;
import com.example.presell.activities.MainActivity;
import com.example.presell.adapters.PostRecyclerAdapter;
import com.example.presell.adapters.CategoryRecyclerViewAdapter;
import com.example.presell.models.GenAppInfo;
import com.example.presell.models.Login;
import com.example.presell.models.Post;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

public class HomeFragment extends Fragment implements CategoryRecyclerViewAdapter.ItemClickListener {
    private static final int MIN_TEXT_SIZE = 14;
    private static final int MAX_TEXT_SIZE = 20;
    private static final int MAX_ADDED_TEXT_SIZE = 6;

    public ArrayList<String> categoriesList;
    private String category;
    private GenAppInfo mInfo;
    private Login mLogin;
    private ArrayList<Post> mPosts;
    public PostRecyclerAdapter mAdapter;
    private HashSet<String> addedPostsIdSet;
    public SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton refreshButton;
    private Spinner exploreSpinner;
    private String exploreSelection;
    private boolean noSignIn;

    private RecyclerView mRecyclerView;
    private RecyclerView categoryRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categoriesList = instantiateCategories(new ArrayList<String>());
        mInfo = new GenAppInfo(requireContext().getApplicationContext());
        mLogin = new Login(requireContext().getApplicationContext());
        noSignIn = mLogin.getNoSignIn();

        addedPostsIdSet = new HashSet<String>();
        mPosts = new ArrayList<Post>();
        mAdapter = new PostRecyclerAdapter(requireContext(), new ArrayList<Post>());

        exploreSelection = "Favorites";
    }

    public static ArrayList<String> instantiateCategories(ArrayList<String> categoriesList){
        categoriesList.add("T-Shirts");
        categoriesList.add("Mugs");
        categoriesList.add("Coasters");
        categoriesList.add("Plates");
        categoriesList.add("Stickers");
        categoriesList.add("Airpods Skins");

        Collections.sort(categoriesList);
        return categoriesList;
    }

    public void setupCategoriesPager(){
        categoryRecyclerView = requireView().findViewById(R.id.recycler_categories);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false){
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                // force height of viewHolder here, this will override layout_height from xml
                lp.width = getWidth() / 3;
                return true;
            }
        });
        SnapScrollListener mScrollListener = new SnapScrollListener((MainActivity)requireActivity(), mRecyclerView, categoriesList);
        categoryRecyclerView.addOnScrollListener(mScrollListener);
        CategoryRecyclerViewAdapter adapter = new CategoryRecyclerViewAdapter(getContext(), categoriesList);
        adapter.setClickListener(this);
        categoryRecyclerView.setAdapter(adapter);

        categoryRecyclerView.scrollToPosition(4);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // mRecyclerView is accessed in categories pager
        mRecyclerView = view.findViewById(R.id.feed_recyclerview);
        LinearLayoutManager mLinLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLinLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        setupExploreSpinner();
        setupCategoriesPager();

//        refreshButton = view.findViewById(R.id.refresh_button);
//        refreshButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(requireContext(), "Refreshing Button", Toast.LENGTH_LONG).show();
//            }
//        });

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(requireContext(), "Refreshing Swipe", Toast.LENGTH_LONG).show();
                handleFeedChange(exploreSelection);
            }
        });

        // loadPosts();
    }

    public void setupExploreSpinner(){
        exploreSpinner = requireView().findViewById(R.id.explore_spinner);

        ArrayList<String> exploreAdapterOptions = new ArrayList<String>();

        exploreAdapterOptions.add("Recent");
        exploreAdapterOptions.add("Favorites");

        ArrayAdapter<String> mExploreAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_explore, exploreAdapterOptions);
        exploreSpinner.setAdapter(mExploreAdapter);
        exploreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (view != null) {
                    exploreSelection = ((TextView) view).getText().toString();
                    if (exploreSelection.equals("Favorites") && noSignIn) {
                        Toast.makeText(requireContext(), "Sign in to save & see your favorites!", Toast.LENGTH_SHORT).show();
                        exploreSpinner.setSelection(0);
                    } else {
                        handleFeedChange(exploreSelection);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void handleFeedChange(String selectedOption){
        Log.d("HomeFragmentFeedChange", "handleFeeChange() " + selectedOption);
        if(selectedOption.equals("Recent")){
            Log.d("HomeFragmentFeedChange", "inside recent feed change");

            ((MainActivity)requireActivity()).getRecentFeed();
        }
        else if(selectedOption.equals("Favorites")){
            Log.d("HomeFragmentFeedChange", "inside favorites feed change");

            ((MainActivity)requireActivity()).getFavoriteFeed();
        }
    }

    //TODO: Refactor into SearchCategoryFragment
    public int getCategoryPosition(String category){
        for(int i = 0; i < categoriesList.size(); i++){
            if(categoriesList.get(i).equals(category)){
                if(i==0){
                    return categoriesList.size()-1;
                }
                return i-1;
            }
        }
        return 3;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Log.d("HomeFragment", "onCreateOptionsMenu()");
        menu.clear();
        requireActivity().getMenuInflater().inflate(R.menu.home_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean returnVal = super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.create_button) {
            if(mLogin.getNoSignIn()){
                showAlertDialog();
            }
            else {
                startActivity(new Intent(getContext(), CreateActivity.class));
            }
        }
        return returnVal;
    }

    private void showAlertDialog(){
        new AlertDialog.Builder(requireContext()).setTitle("To sell items with your design, please sign in.")
                .setMessage("Would you like to return to the login or sign up page?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((MainActivity)requireActivity()).returnToSignIn();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        }).show();
    }

    private void loadPosts(){
        ArrayList<Post> posts = new ArrayList<>();

        Post post = emptyPost();
        post.setId("-1");
        posts.add(post);
        posts.add(post);
        posts.add(post);

//        mRecyclerView.setAdapter(new PostRecyclerAdapter(getContext(), posts));
    }

    public void setFeedPosts(ArrayList<Post> posts){
        mPosts.addAll(posts);
        mAdapter.notifyDataSetChanged();
    }

    public void addFeedPost(boolean clearPosts, Post post){
        Log.d("HomeFragment", post.getId() + " clear posts: " + clearPosts);
        if(clearPosts) {
            mAdapter.clearPosts();
            addedPostsIdSet.clear();
        }

        if(!addedPostsIdSet.contains(post.getId())) {
            Log.d("HomeFragment", post.getId() + " " + post.getTitle());

            addedPostsIdSet.add(post.getId());

            mAdapter.addPost(post);
//            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(final View view, final int position) {
        ((LinearLayoutManager) Objects.requireNonNull(categoryRecyclerView.getLayoutManager())).scrollToPositionWithOffset(position-1, 0);
    }

    /**
     * Default post value when there's nothing to show in feed
     */
    private Post emptyPost(){
        Post post = new Post();
        post.setTitle("Empty Post");
        post.setName("Admin");
        return post;
    }
}
