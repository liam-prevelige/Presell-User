//package com.example.presell.fragments;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.content.ContextCompat;
//import androidx.fragment.app.Fragment;
//
//import com.example.presell.R;
//import com.example.presell.models.Login;
//import com.example.presell.models.Post;
//
//import com.google.firebase.database.DatabaseReference;
//
//import java.io.InputStream;
//import java.util.HashMap;
//
///**
// * Fragment for individual post displayed in public/private feed
// */
//public class PostFragment extends Fragment {
//    public static final String POST_KEY = "send post";
//    public static final String IMAGES_KEY = "send images";
//    public static final String IS_PUBLIC_FEED_KEY = "is public feed";
//    public static final String EMAIL_KEY = "email";
//
//
//    ImageButton imageButton;
//    private DatabaseReference mRef;
//    private String emailKey;
//    private ProgressBar mProgressBar;
//    private Login currLogin;
//    private View view;
//    private Post post;
//    private String email;
//    private boolean isPublicFeed;
//    private ImageView heartIcon;
//    HashMap<String, String> mapURLStrings;
//
//    public PostFragment() {
//        // Default constructor
//    }
//
//    public PostFragment(Post post){
//        this.post = post;
//    }
//
//    /**
//     * Store the post info for loading, the email of the user viewing the post, and where the post
//     * is being loaded in
//     */
//    public PostFragment(Post post, String email, boolean isPublicFeed) {
//        this.post = post;
//        this.isPublicFeed = isPublicFeed;
//        this.email = email;
//    }
//
//    public static PostFragment newInstance(String position) {
//        return new PostFragment();
//    }
//
//    /**
//     * Initialize variables and get references to means of getting user data
//     */
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    /**
//     * Initialize relevant references to view and display progress bar while waiting for image to load
//     */
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.feed_post, container, false);
//        this.view = view;
////        mProgressBar = view.findViewById(R.id.progress_view);
//
//        heartIcon = view.findViewById(R.id.heart_icon);
//        heartIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(heartIcon.getContentDescription().equals("filled in")){
//                    heartIcon.setImageResource(R.drawable.ic_favorite_border_24px);
//                    heartIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.lighterRed), android.graphics.PorterDuff.Mode.SRC_IN);
//                    heartIcon.setContentDescription("outline");
//                }
//                else {
//                    heartIcon.setImageResource(R.drawable.ic_favorite_24px);
//                    heartIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.lighterRed), android.graphics.PorterDuff.Mode.SRC_IN);
//                    heartIcon.setContentDescription("filled in");
//                }
//            }
//        });
//
//        imageButton = view.findViewById(R.id.postImageButton);
//        imageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//            }
//        });
////        if (post.getId() == -1){
////            mProgressBar.setVisibility(View.INVISIBLE);
////            imageButton.setVisibility(View.VISIBLE);
////        }
//        return view;
//    }
//
//    /**
//     * If user clicks on image, start voting activity and pass relevant information via intent
//     */
//    private void handleClickToVote(){
//        if(email.length() > 0 && !email.equals(currLogin.getEmail())) {
////            Intent intent = new Intent(getActivity(), VotingActivity.class);
////            intent.putExtra(POST_KEY, post);
////            intent.putExtra(IMAGES_KEY, mapURLStrings);
////            intent.putExtra(IS_PUBLIC_FEED_KEY, isPublicFeed);
////            intent.putExtra(EMAIL_KEY, email);
////
////            if (mapURLStrings.size() > 0 && post != null) {
////                startActivity(new Intent(intent));
////            } else {
////                Toast.makeText(getContext(), "Error loading post. Please try again momentarily", Toast.LENGTH_SHORT).show();
////            }
//        }
//        else{
//            // If user requesting to vote has the same email as the person who posted, display error
//            Toast.makeText(getContext(), "No voting on your own post, sorry!", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        updateValues(post);
//    }
//
//    /**
//     * Update UI with values from passed Post object, with the exception of images that are loaded
//     * asynchronously
//     */
//    private void updateValues(Post post) {
//        ((TextView) view.findViewById(R.id.post_title_text)).setText(post.getTitle());
////        try {
////            for (String oneURL : mapURLStrings.values()) {
////                new DownloadImageTask((ImageButton) view.findViewById(R.id.postImageButton)).execute(oneURL);
////                break;
////            }
////        } catch (Exception e) {
////            Log.e("Exception", "Error setting cover photo");
////            e.printStackTrace();
////        }
//        ((TextView) view.findViewById(R.id.user_text)).setText(post.getName());
//    }
//
//    /**
//     * Asynchronous class to load image based on URL into post fragment
//     */
//    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
//        ImageButton bmImage;
//
//        /**
//         * Constructor needs reference to image being updated
//         */
//        public DownloadImageTask(ImageButton bmImage) {
//            this.bmImage = bmImage;
//        }
//
//        /**
//         * Load an image from URL async
//         */
//        protected Bitmap doInBackground(String... urls) {
//            String imageURL = urls[0];
//            Bitmap bm = null;
//            try {
//                InputStream in = new java.net.URL(imageURL).openStream();
//                bm = BitmapFactory.decodeStream(in);
//            } catch (Exception e) {
//                Log.e("Error", e.getMessage());
//                e.printStackTrace();
//            }
//            return bm;
//        }
//
//        /**
//         * Make the loaded bitmap visible and progress bar invisible
//         */
//        protected void onPostExecute(Bitmap result) {
//            mProgressBar.setVisibility(View.INVISIBLE);
//            imageButton.setVisibility(View.VISIBLE);
//            bmImage.setImageBitmap(result);
//        }
//    }
//}