package com.example.presell.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.presell.GlideApp;
import com.example.presell.R;
import com.example.presell.activities.BuyActivity;
import com.example.presell.models.Login;
import com.example.presell.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.PostViewHolder> {
    public static final String POST_KEY = "post key";

    private List<Post> posts;
    private Context context;
    private Post post;
    private Login mLogin;
    private boolean favoritesSetComplete, favoritesChanged;

    private DatabaseReference mRef;

    private Set<String> favoritesSet;
    private Queue<PostViewHolder> viewHolderQueue;

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView title, username;
        private ImageView profilePic;
        private ViewPager2 viewPager;
        private ImageView heartIcon;
        private String postId;
        private Post post;

        public PostViewHolder(View v) {
            super(v);

            title = (TextView) v.findViewById(R.id.post_title_text);
            profilePic = (ImageView) v.findViewById(R.id.user_headshot);
            username = (TextView) v.findViewById(R.id.user_text);
            viewPager = (ViewPager2)v.findViewById(R.id.postViewPager);
            heartIcon = (ImageView) v.findViewById(R.id.heart_icon);

            setupOnClickListeners();
        }

        public void setPostId(String postId){
            this.postId = postId;
        }

        public String getPostId(){
            return postId;
        }

        public void setPost(Post post){
            this.post = post;
        }

        public Post getPost(){
            return post;
        }

        private void setupOnClickListeners(){

        }
    }

    public PostRecyclerAdapter(Context context, List<Post> posts) {
        this.posts = posts;
        this.context = context;
        favoritesChanged = false;

        mLogin = new Login(context.getApplicationContext());
        viewHolderQueue = new ConcurrentLinkedQueue<>();

        getLikedPosts();
    }

    //TODO: PROBLEMS WHEN LIKED POSTS BECOMES VERY LARGE
    private void getLikedPosts(){
        mRef = FirebaseDatabase.getInstance().getReference()
                .child(mLogin.getUserId())
                .child("liked");
        favoritesSetComplete = false;
        favoritesSet = new HashSet<>();

        mRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d("PostRecyclerAdapterFavorite", "top of onDataChange()");

                        for(DataSnapshot idx : snapshot.getChildren()){
                            Log.d("PostRecyclerAdapterFavorite", "top of onDataChange(): inside loop");

                            for(DataSnapshot postSnapshot : idx.getChildren()){
                                //TODO:     com.google.firebase.database.DatabaseException: Can't convert object of type java.lang.Boolean to type com.example.presell.models.Post
                                Post post = postSnapshot.getValue(Post.class);
                                if(post!=null && !post.getIsDeleted()){
                                    Log.d("PostRecyclerAdapterFavorite", "is deleted: " + post.getIsDeleted() + " id: " + post.getId());
                                    favoritesSet.add(post.getId());
                                }

//                                for(DataSnapshot postInfo : post.getChildren()){
//                                    if(postInfo.getKey() != null && postInfo.getValue() != null){
//                                        // "deleted" always comes before "id" due to alphabetical organization
//                                        if(postInfo.getKey().equals("deleted")){
//                                            if(postInfo.getValue().equals("true")) break;
//                                        }
//                                        else if(postInfo.getKey().equals("id")) {
//                                            favoritesSet.add(postInfo.getValue().toString());
//                                        }
//                                    }
//                                }
                            }
                        }
                        Log.d("PostRecyclerAdapterFavorite", "bottom of onDataChange()");

                        if(!favoritesSetComplete){
                            favoritesSetComplete = true;
                            checkForFavorite(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkForFavorite(PostViewHolder mViewHolder){
        if(mViewHolder == null && favoritesSetComplete) {
            Log.d("PostRecyclerAdapterFavorite", "checkForFavorite(): viewHolder null");
            while(viewHolderQueue.size() > 0){
                PostViewHolder viewHolder = viewHolderQueue.remove();
                checkForFavorite(viewHolder);
            }
        }
        else if(favoritesSetComplete) {
            Log.d("PostRecyclerAdapterFavorite", "checkForFavorite(): viewHolder not null");

            if(favoritesSet.contains(mViewHolder.post.getId())){
                Log.d("PostRecyclerAdapterFavorite", "checkForFavorite(): viewHolder not null & found: " + mViewHolder.getPostId());

                if(mViewHolder.heartIcon.getContentDescription().toString().equals("outline")) {
                    mViewHolder.heartIcon.setImageResource(R.drawable.ic_favorite_24px);
                    mViewHolder.heartIcon.setColorFilter(ContextCompat.getColor(context, R.color.lighterRed), android.graphics.PorterDuff.Mode.SRC_IN);
                    mViewHolder.heartIcon.setContentDescription("filled in");
                }
            }
        }
        else{
            Log.d("PostRecyclerAdapterFavorite", "checkForFavorite(): favoritesSetComplete false");

            viewHolderQueue.add(mViewHolder);
        }
    }

    public void clearPosts(){
        posts.clear();
        notifyDataSetChanged();
    }

    public void addPost(Post post) {
        // Add the event at the beginning of the list
        posts.add(post);

        Log.d("PostRecyclerAdapter", "in addpost(), title at index zero: " + posts.get(0).getTitle() + " " + post.getTitle());
        notifyDataSetChanged();

//        if(posts.size() == 1){
//            notifyDataSetChanged();
//        }
//        else {
//            // Notify the insertion so the view can be refreshed
//            notifyItemInserted(0);
//        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

//    @NonNull
//    @Override
//    public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
//        Log.d("PostRecyclerAdapter", "view holder index: " + i);
//        View v = LayoutInflater.from(viewGroup.getContext())
//                .inflate(R.layout.feed_post, viewGroup, false);
//        post = posts.get(posts.size()-1);
//        Log.d("PostRecyclerAdapter", "post id: " + post.getId() + " " + posts.size() + " size. " + posts.toString());
//
//        return new PostViewHolder(v, context, post);
//    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_post, parent, false);
        return new PostViewHolder(v);
    }

    //TODO: CLEAN UP THIS SHITTY ORGANIZATION
    @Override
    public void onBindViewHolder(PostViewHolder viewHolder, int i) {
        post = posts.get(i);
        Log.d("PostRecyclerAdapter", "Post title at index 0: " + posts.get(0).getTitle() + " selected post title: " + post.getTitle());
        Log.d("PostRecyclerAdapter", "i value in onBindViewHolder() " + i);

        viewHolder.setPostId(post.getId());
        viewHolder.setPost(post);
        DesignsPagerAdapter mAdapter = new DesignsPagerAdapter(post, context);
        viewHolder.viewPager.setAdapter(mAdapter);
        viewHolder.viewPager.setCurrentItem(0);

        setupFavoriteClickListener(viewHolder);

        String profilePicUrl = post.getProfilePicUrl();
        if(!profilePicUrl.equals("empty")) {
            GlideApp.with(context)
                    .load(profilePicUrl)
                    .centerInside()
                    .circleCrop()
                    .into(viewHolder.profilePic);
        }
        viewHolder.title.setText(post.getTitle());
        viewHolder.username.setText(post.getName());
        viewHolder.heartIcon.setImageResource(R.drawable.ic_favorite_border_24px);
        viewHolder.heartIcon.setColorFilter(ContextCompat.getColor(context, R.color.lighterRed), android.graphics.PorterDuff.Mode.SRC_IN);
        viewHolder.heartIcon.setContentDescription("outline");

        viewHolder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBuyActivity(post);
            }
        });
        viewHolder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBuyActivity(post);
            }
        });
        viewHolder.viewPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBuyActivity(post);
            }
        });

        checkForFavorite(viewHolder);

//        viewHolder.data.setText(event.getData());
    }

    private void setupFavoriteClickListener(final PostViewHolder viewHolder){
        viewHolder.heartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewHolder.heartIcon.getContentDescription().equals("filled in")){
                    viewHolder.heartIcon.setImageResource(R.drawable.ic_favorite_border_24px);
                    viewHolder.heartIcon.setColorFilter(ContextCompat.getColor(context, R.color.lighterRed), android.graphics.PorterDuff.Mode.SRC_IN);
                    viewHolder.heartIcon.setContentDescription("outline");

                    if(!mLogin.getNoSignIn()){
                        removeLike(viewHolder.getPostId(), viewHolder.getPost());
                        favoritesSet.remove(viewHolder.getPostId());
                    }
                }
                else {
                    viewHolder.heartIcon.setImageResource(R.drawable.ic_favorite_24px);
                    viewHolder.heartIcon.setColorFilter(ContextCompat.getColor(context, R.color.lighterRed), android.graphics.PorterDuff.Mode.SRC_IN);
                    viewHolder.heartIcon.setContentDescription("filled in");

                    if(!mLogin.getNoSignIn()) addLike(viewHolder.getPostId(), viewHolder.getPost());
                }
            }
        });
    }

    private void addLike(String postId, Post post){
        Hashtable<String, Post> likedPostsTable = new Hashtable<String, Post>();

        int atIdx = postId.indexOf("@");
        String postIndex = "-" + postId.substring(0, atIdx);

        post.setIsDeleted(false);

        likedPostsTable.put(postId, post);

        mRef.child(postIndex).setValue(likedPostsTable).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
                favoritesChanged = true;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to add to favorites, please check internet connection!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeLike(String postId, Post post){
        Hashtable<String, Post> likedPostsTable = new Hashtable<String, Post>();

        int atIdx = postId.indexOf("@");
        String postIndex = "-" + postId.substring(0, atIdx);

        // 'deleted' in individual section of db represents whether a like is present
        post.setIsDeleted(true);

        likedPostsTable.put(post.getId(), post);

        mRef.child(postIndex).setValue(likedPostsTable).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
                favoritesChanged = true;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to remove from favorites, please check internet connection!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startBuyActivity(Post mPost){
        Intent intent = new Intent(context, BuyActivity.class);
        intent.putExtra(POST_KEY, mPost);
        Log.d("PostRecyclerAdapter", mPost.getDesignURL());
        context.startActivity(intent);
    }

    public boolean getFavoritesChanged(){
        return favoritesChanged;
    }
}