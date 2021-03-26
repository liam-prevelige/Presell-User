package com.example.presell.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.presell.GlideApp;
import com.example.presell.R;
import com.example.presell.adapters.DesignsPagerAdapter;
import com.example.presell.adapters.PostRecyclerAdapter;
import com.example.presell.models.Login;
import com.example.presell.models.Order;
import com.example.presell.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stripe.android.PaymentSession;
import com.stripe.android.PaymentSessionConfig;
import com.stripe.android.PaymentSessionData;
import com.stripe.android.model.Address;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.ShippingInformation;
import com.stripe.android.model.ShippingMethod;
import com.stripe.android.view.ShippingInfoWidget;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static java.util.Arrays.asList;

public class BuyActivity extends AppCompatActivity {
    public static final String POST_KEY = "post_key";
    public static final String ORDER_KEY = "order_key";

    private TextView mTitle, mCreatorName, mCategory, mTimeText, mPriceText;
    private ViewPager2 viewPager;
    private ImageView mCreatorProfilePic, heartIcon;
    private SeekBar mSeekBar;
    private DatabaseReference mRef;
    private Post mPost;
    private Login mLogin;
    private EditText mQuantityEditText;
    private Button mOrderButton;
    private NumberFormat priceFormat;

    private Map<String,ArrayList<Double>> priceMap;
    private Map<String,ArrayList<String>> ttsMap;

    private PaymentSession paymentSession;

    private boolean orderButtonClicked;
    private String currentItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable((ContextCompat.getColor(getApplicationContext(), R.color.darkBackground))));

        orderButtonClicked = false;

        mLogin = new Login(getApplicationContext());
        mRef = FirebaseDatabase.getInstance().getReference()
                .child(mLogin.getUserId())
                .child("liked");

        mPost = (Post) Objects.requireNonNull(getIntent().getExtras()).get(PostRecyclerAdapter.POST_KEY);


        priceMap = new HashMap<String, ArrayList<Double>>();
        ArrayList<Double> defaultPrices = new ArrayList<Double>();
        defaultPrices.add(5.00);
        defaultPrices.add(10.00);
        defaultPrices.add(15.00);
        defaultPrices.add(20.00);
        defaultPrices.add(25.00);
        defaultPrices.add(30.00);
        priceMap.put("Testing", defaultPrices);

        ttsMap = new HashMap<String, ArrayList<String>>();
        ArrayList<String> ttsEstimates = new ArrayList<>();
        ttsEstimates.add(">2");
        ttsEstimates.add("2");
        ttsEstimates.add("1.5");
        ttsEstimates.add("1.0");
        ttsEstimates.add("0.5");
        ttsMap.put("Testing", ttsEstimates);

        if(mPost == null){
            Toast.makeText(this, "Error retrieving post, please exit and try again.", Toast.LENGTH_SHORT).show();
        }
        else {
            setupViews();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void setupViews(){
//        mDesignPhoto = findViewById(R.id.design_image);
        viewPager = findViewById(R.id.postViewPager);
        viewPager.setClickable(false);

        mQuantityEditText = findViewById(R.id.quantity_edit_text);
        mQuantityEditText.setText("1");

        mTitle = findViewById(R.id.title_text);
        mCreatorName = findViewById(R.id.user_text);
        mCreatorProfilePic = findViewById(R.id.user_headshot);
        heartIcon = findViewById(R.id.heart_icon);
        mCategory = findViewById(R.id.category_text);
        mOrderButton = findViewById(R.id.order_button);
        mSeekBar = findViewById(R.id.seek_bar);

        mTimeText = findViewById(R.id.time_estimate_text);
        mTimeText.setText("Est time to sale: " + ttsMap.get("Testing").get(2) + " days");

        mPriceText = findViewById(R.id.price_text);

        priceFormat = NumberFormat.getCurrencyInstance();
        priceFormat.setMaximumFractionDigits(0);
        priceFormat.setCurrency(Currency.getInstance("USD"));

        setupOrderClickListener();
        setupSeekBarListener();
        setupFavoriteClickListener();
        initValues();
    }

    private void setupOrderClickListener(){
        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mQuantityEditText.getText().toString().equals("") || mQuantityEditText.getText().toString().equals("0")){
//                    Toast.makeText(getBaseContext(), "Please enter a valid quantity", Toast.LENGTH_LONG).show();
                    mQuantityEditText.setError("Invalid quantity");
                }
                else{
                    Intent intent = new Intent(BuyActivity.this, ConfirmOrderActivity.class);
                    intent.putExtra(POST_KEY, mPost);
                    intent.putExtra(ORDER_KEY, createOrder());
                    startActivity(intent);
                }
            }
        });
    }

    private Order createOrder(){
        Order mOrder = new Order();
        mOrder.setItemType(currentItem);
        mOrder.setSeekBarProgress(mSeekBar.getProgress()+"");
        mOrder.setItemPrice(mPriceText.getText().toString());
        mOrder.setItemTimeRemaining(mTimeText.getText().toString());
        mOrder.setQuantity(mQuantityEditText.getText().toString());
        mOrder.setIsInstantOrder(mSeekBar.getProgress()==mSeekBar.getMax());
        mOrder.setSellerId(mPost.getId().substring(mPost.getId().indexOf('@')+1));
        mOrder.setDesignUrl(mPost.getDesignURL());

        return mOrder;
    }

    private void setupSeekBarListener(){
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.d("BuyActivitySeekBar", "i: " + i + " this seek bar top "+ seekBar.getMax() + " mSeekBar top " + mSeekBar.getMax());
                if(i==seekBar.getMax()){
                    mTimeText.setText("instant purchase - ship now!");
                }
                else{
                    mTimeText.setText("Est time to sale: " + ttsMap.get("Testing").get(i) + " days");
                }

                ArrayList<Double> currentItemPrices = priceMap.get("Testing");
                if(currentItemPrices !=null && currentItemPrices.get(i)!=null) {
                    String priceString = priceFormat.format(currentItemPrices.get(i)) + "";
                    mPriceText.setText(priceString);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initValues(){
        final DesignsPagerAdapter mAdapter = new DesignsPagerAdapter(mPost, this);
        viewPager.setAdapter(mAdapter);
        viewPager.setCurrentItem(0);

        final ArrayList<String> selectedCategories = mPost.getSelectedCategories();
        currentItem = selectedCategories.get(viewPager.getCurrentItem());

        //TODO: FIGURE OUT WHY SOMETIMES IS JUST AIRPODS, NOT AIRPODS SKINS
        viewPager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                   @Override
                   public void onPageSelected(int position) {
                       super.onPageSelected(position);
                       currentItem = selectedCategories.get(position);
                       mCategory.setText(currentItem.substring(0, currentItem.length()-1));
                   }
               });

//        GlideApp.with(this)
//                .load(mPost.getDesignURL())
//                .centerInside()
//                .into(mDesignPhoto);

        mTitle.setText(mPost.getTitle());
        mCreatorName.setText(mPost.getName());

        String profilePicUrl = mPost.getProfilePicUrl();
        if(!profilePicUrl.equals("empty")) {
            GlideApp.with(this)
                    .load(profilePicUrl)
                    .centerInside()
                    .circleCrop()
                    .into(mCreatorProfilePic);
        }
    }

    private void setupFavoriteClickListener(){
        heartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(heartIcon.getContentDescription().equals("filled in")){
                    heartIcon.setImageResource(R.drawable.ic_favorite_border_24px);
                    heartIcon.setColorFilter(ContextCompat.getColor(getBaseContext(), R.color.lighterRed), android.graphics.PorterDuff.Mode.SRC_IN);
                    heartIcon.setContentDescription("outline");

                    if(!mLogin.getNoSignIn()){
                        removeLike(mPost.getId(), mPost);
                    }
                }
                else {
                    heartIcon.setImageResource(R.drawable.ic_favorite_24px);
                    heartIcon.setColorFilter(ContextCompat.getColor(getBaseContext(), R.color.lighterRed), android.graphics.PorterDuff.Mode.SRC_IN);
                    heartIcon.setContentDescription("filled in");

                    if(!mLogin.getNoSignIn()) addLike(mPost.getId(), mPost);
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
                Toast.makeText(getBaseContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(), "Failed to add to favorites, please check internet connection!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getBaseContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(), "Failed to remove from favorites, please check internet connection!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
