package com.example.presell.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.presell.GlideApp;
import com.example.presell.Presell;
import com.example.presell.R;
import com.example.presell.fragments.HomeFragment;
import com.example.presell.fragments.MyProfileFragment;
import com.example.presell.fragments.OrdersFragment;
import com.example.presell.fragments.SearchCategoryFragment;
import com.example.presell.fragments.SearchFragment;
import com.example.presell.models.GenAppInfo;
import com.example.presell.models.Login;
import com.example.presell.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private final HomeFragment fragment1 = new HomeFragment();
    private final SearchFragment fragment2 = new SearchFragment();
    private final OrdersFragment fragment3 = new OrdersFragment();
    private final MyProfileFragment fragment4 = new MyProfileFragment();
    private final FragmentManager fm = getSupportFragmentManager();

    private final SearchCategoryFragment fragment5 = new SearchCategoryFragment();

    private long endedPostIndex;
    private long endedPostCountInIndex;

    private ArrayList<Post> mFeedPosts;
    private Post mFeedPost;

    private BottomNavigationView bottomNavigationView;

    private DatabaseReference allRef;
    private Login mLogin;

    private String feedExploreString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate()");
        setContentView(R.layout.activity_main);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable((ContextCompat.getColor(getApplicationContext(), R.color.darkBackground))));

        allRef = FirebaseDatabase.getInstance().getReference().child("all posts");
        mLogin = new Login(getApplicationContext());
        preloadFeedImages();
        manageFragmentNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GenAppInfo mInfo = new GenAppInfo(getApplicationContext());
        Log.d("MainActivityResume", "onResume " + mInfo.getPostUploaded());

        //TODO: WHY CAN"T I REFRESH???????
//        if(mInfo.getPostUploaded()) {
////            fragment1.swipeRefreshLayout.setRefreshing(true);
//            updateFeedImages();
//            mInfo.setPostUploaded(false);
//        }
    }

    private void manageFragmentNavigation(){
        bottomNavigationView = findViewById(R.id.nav_view);

        fm.beginTransaction().add(R.id.fragment_container, fragment5, "5").hide(fragment5).commit();

        fm.beginTransaction().add(R.id.fragment_container, fragment4, "4").hide(fragment4).commit();
        fm.beginTransaction().add(R.id.fragment_container, fragment3, "3").hide(fragment3).commit();
        fm.beginTransaction().add(R.id.fragment_container, fragment2, "2").hide(fragment2).commit();
        fm.beginTransaction().add(R.id.fragment_container,fragment1, "1").commit();
        Objects.requireNonNull(getSupportActionBar()).setTitle("Feed");

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            Fragment active = fragment1;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.nav_home:
                        Log.d("MainActivity", "Selected nav_home " + active.toString());
                        if(fragment5.isVisible()) {
                            fm.beginTransaction().hide(fragment5).commit();
                            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
                        }

                        fm.beginTransaction().hide(active).show(fragment1).commit();
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Feed");
                        active = fragment1;
                        break;
                    case R.id.nav_search:
                        if(fragment5.isVisible()) {
                            fm.beginTransaction().hide(fragment5).commit();
                            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
                        }

                        fm.beginTransaction().hide(active).show(fragment2).commit();
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Search");
                        active = fragment2;
                        break;
                    case R.id.nav_orders:
                        if(fragment5.isVisible()) {
                            fm.beginTransaction().hide(fragment5).commit();
                            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
                        }

                        fm.beginTransaction().hide(active).show(fragment3).commit();
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Orders");
                        active = fragment3;
                        break;
                    case R.id.nav_about_me:
                        if(fragment5.isVisible()) {
                            fm.beginTransaction().hide(fragment5).commit();
                            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
                        }

                        fm.beginTransaction().hide(active).show(fragment4).commit();
                        Objects.requireNonNull(getSupportActionBar()).setTitle("My Profile");
                        active = fragment4;
                        break;
                }
                return true;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    public void navigateToFeedFragment(){
        fm.beginTransaction().hide(fragment2).show(fragment5).commit();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    public void returnToSearch(boolean favoritesChanged){
        if(favoritesChanged){
            switch (feedExploreString){
                case "Favorites":
                    getFavoriteFeed();
                default:
                    getRecentFeed();
            }
        }
        fm.beginTransaction().hide(fragment5).show(fragment2).commit();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
    }

    public void changeCategory(){
        fragment5.categoryUpdated();
    }

    public void returnToSignIn(){
        finishAffinity();
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void preloadFeedImages(){
        allRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("MainActivityMainActivity", "preloadFeedImages(), onDataChange()");
                fragment1.swipeRefreshLayout.setRefreshing(true);
                preloadHelper(false, fragment1, snapshot);
                allRef.removeEventListener(this);
                feedExploreString = "Recent";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getRecentFeed(){
        final DatabaseReference allRef = FirebaseDatabase.getInstance().getReference().child("all posts");

        allRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("CreateActivity", "uploadPost()");

                Log.d("MainActivityMainActivity", "updateFeedImages(), onDataChange()");
                preloadHelper(true, fragment1, snapshot);
                allRef.removeEventListener(this);
                feedExploreString = "Recent";
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getFavoriteFeed(){
        final DatabaseReference allRef = FirebaseDatabase.getInstance().getReference().child(mLogin.getUserId()).child("liked");
        Log.d("HomeFragmentFeedChange", "allRef instance " + allRef.toString());

        allRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                preloadHelper(true, fragment1, snapshot);
                allRef.removeEventListener(this);
                feedExploreString = "Favorites";
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getCategoryFeed(String postCategory){
        final DatabaseReference catRef = FirebaseDatabase.getInstance().getReference().child(postCategory);

        catRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                preloadHelper(true, fragment5, snapshot);
                catRef.removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void preloadHelper(boolean clearPosts, HomeFragment fragment, DataSnapshot snapshot){
        for(DataSnapshot postIndex : snapshot.getChildren()) {
            for (DataSnapshot post : postIndex.getChildren()) {
                final Post currentPost = post.getValue(Post.class);
                if (currentPost != null && fragment.isAdded()) {

                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                GlideApp.with(Presell.getAppContext())
                                        .downloadOnly()
                                        .diskCacheStrategy(DiskCacheStrategy.DATA) // Cache resource before it's decoded
                                        .load(currentPost.getDesignURL())
                                        .submit(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                                        .get(); // Called on background thread
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    if(!currentPost.getIsDeleted()) {
                        fragment.addFeedPost(clearPosts, currentPost);
                        if(clearPosts) clearPosts = false;
                        fragment.swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }
        }
    }
}
