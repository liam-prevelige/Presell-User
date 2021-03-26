package com.example.presell.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.presell.GlideApp;
import com.example.presell.R;
import com.example.presell.activities.CreateActivity;
import com.example.presell.adapters.DesignsPagerAdapter;
import com.example.presell.models.GenAppInfo;
import com.example.presell.models.Login;
import com.example.presell.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class StepFourFragment extends Fragment {
    private View view;
    private TextInputEditText titleTextEntry;
    private String titleText;
    private Spinner categorySpinner;
    private TextView usernameTextView;
    private ArrayList<String> selectedCategories;
    private ImageView mProfilePic;
    private ViewPager2 mViewPager;

    private Post post;
    private File mFile;
    private Bitmap mBitmap;
    private boolean firstCycle;

    private StorageReference mStorageRef;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private DatabaseReference allRef;
    private Login login;

    private long pastPostUserCount;
    private long pastPostAllCount;

    private boolean updatedPastUserPostCount;
    private boolean updatedPastAllPostCount;

    public StepFourFragment(File file){
        mFile = file;
        mBitmap = BitmapFactory.decodeFile(mFile.getPath());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        post = new Post();
        login = new Login(requireContext().getApplicationContext());
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference(login.getUserId());
        allRef = database.getReference();
        selectedCategories = ((CreateActivity)requireActivity()).getEnabledCategories();

        firstCycle = true;
        pastPostUserCount = 0;
        pastPostAllCount = 0;
        updatedPastUserPostCount = false;
        updatedPastAllPostCount = false;

        getPastPostCount();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step_four, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        titleTextEntry = view.findViewById(R.id.title_text_input);
        categorySpinner = view.findViewById(R.id.preferred_category_spinner);

        mProfilePic = view.findViewById(R.id.user_photo);
        String profilePicUrl = login.getProfilePicUrl();
        if(!profilePicUrl.equals("empty")){
            GlideApp.with(requireContext())
                    .load(profilePicUrl)
                    .centerInside()
                    .circleCrop()
                    .into(mProfilePic);
        }

        usernameTextView = view.findViewById(R.id.username_text);
        usernameTextView.setText(login.getUsername());

        mViewPager = view.findViewById(R.id.selected_images_view_pager);
//        mViewPager.setOffscreenPageLimit(selectedCategories.size());
        final DesignsPagerAdapter mPagerAdapter = new DesignsPagerAdapter(mFile, selectedCategories, requireContext());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                mPagerAdapter.notifyDataSetChanged();
            }
        });
        setupConfirmButton();
        populateCategorySpinner();
    }

    private void populateCategorySpinner(){
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(requireActivity(), R.layout.spinner_item_orders, selectedCategories);
        categorySpinner.setAdapter(categoryAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mViewPager.setCurrentItem(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupConfirmButton(){
        Button confirm = view.findViewById(R.id.confirm_post_button);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!(titleTextEntry.getText() == null) && !(titleText = titleTextEntry.getText().toString()).equals("")) {
                    post.setTitle(titleText);
                    post.setCoverCategory((String)categorySpinner.getSelectedItem());
                    post.setSelectedCategories(selectedCategories);
                    post.setName(login.getUsername());
                    post.setProfilePicUrl(login.getProfilePicUrl());

                    Toast.makeText(requireContext().getApplicationContext(), "Uploading your design for sale...", Toast.LENGTH_SHORT).show();
                    uploadImageToStorage();
                }
                else{
                    Toast.makeText(requireContext().getApplicationContext(), "Please add a title before uploading", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void uploadImageToStorage(){
        String pathString = "designs/" + login.getUserId() + System.currentTimeMillis() + ".jpg";
        final StorageReference designRef = mStorageRef.child(pathString);

        Uri mFileUri = Uri.fromFile(mFile);

        designRef.putFile(mFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                designRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        post.setDesignURL(uri.toString());
                        Toast.makeText(requireContext().getApplicationContext(), "Uploading your listing!", Toast.LENGTH_SHORT).show();
                        uploadPostToUserDb();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext().getApplicationContext(), "Error uploading: Please check internet connection", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void getPastPostCount(){
        mRef.child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Log.d("StepFourFragment", snapshot.getChildrenCount() + " my ref children");
                    updatePastPostUserCount(snapshot.getChildrenCount());
                }
                updatedPastUserPostCount = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        allRef.child("all posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Log.d("StepFourFragment", snapshot.getChildrenCount() + " all posts children");
                    updatePastPostAllCount(snapshot.getChildrenCount());
                }
                updatedPastAllPostCount = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updatePastPostUserCount(long newCount){
        pastPostUserCount = newCount;
    }

    private void updatePastPostAllCount(long newCount){
        pastPostAllCount = newCount;
        post.setId((newCount+1)+"@"+login.getUserId());
    }

    private void uploadPostToUserDb(){
        Log.d("CreateActivity", "uploadPostToUserDb() outside");

        mRef.child("posts").child("-" + (pastPostUserCount+1)).setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(isAdded() && firstCycle) {
                    Log.d("CreateActivity", "uploadPostToUserDb() inside");

                    if(updatedPastUserPostCount) uploadPostToCategoryDb();
                    else Toast.makeText(requireContext(), "Error uploading, please check internet connection and try again", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext().getApplicationContext(), "Error uploading: Please check internet connection", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadPostToCategoryDb(){
        allRef.child(post.getCoverCategory()).child("-" + (pastPostAllCount+1))
                .child("@" + login.getUserId())
                .setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(updatedPastAllPostCount) {
                    uploadPostToAllDb();
                }
                else Toast.makeText(requireContext(), "Error uploading, please check internet connection and try again", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext(), "Error uploading, please check internet connection and try again", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadPostToAllDb(){
        Log.d("CreateActivity", "uploadPostToAllDb() outside");

        allRef.child("all posts").child("-" + (pastPostAllCount+1)).child("@" + login.getUserId()).setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(isAdded() && firstCycle) {
                    //TODO: WHY DO I CHECK UPDATE VAL
                    Log.d("CreateActivity", "uploadPostToAllDb() inside");
                    firstCycle = false;
                    ((CreateActivity)requireActivity()).uploadPost();
                    Toast.makeText(requireContext().getApplicationContext(), "Done uploading - Your listing is live!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
