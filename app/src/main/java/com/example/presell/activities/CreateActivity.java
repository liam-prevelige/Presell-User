package com.example.presell.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.presell.R;
import com.example.presell.fragments.HomeFragment;
import com.example.presell.fragments.StepOneFragment;
import com.example.presell.fragments.StepThreeFragment;
import com.example.presell.models.GenAppInfo;
import com.example.presell.models.Login;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CreateActivity extends AppCompatActivity {
    private static final int NUM_STEPS = 4;

    private ArrayList<String> categoriesList;
    private ProgressBar progressBar;
    private FragmentManager fm;
    private TextView stepText;
    private ArrayList<Fragment> fragmentsList;

    private int currentStep;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        //TODO: REFRESH KILLING ME THO
//        GenAppInfo mInfo = new GenAppInfo(getApplicationContext());
//        mInfo.setPostUploaded(true);

        ActionBar mActionBar = getSupportActionBar();
        if(mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setBackgroundDrawable(new ColorDrawable((ContextCompat.getColor(getApplicationContext(), R.color.darkBackground))));
        }

        fragmentsList = new ArrayList<Fragment>();
        categoriesList = HomeFragment.instantiateCategories(new ArrayList<String>());
        fm = getSupportFragmentManager();
        progressBar = findViewById(R.id.progressBar);
        stepText = findViewById(R.id.step_text);

//        createPrices();
        startFragment(new StepOneFragment(categoriesList));
    }

//    private void createPrices(){
//        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
//        db.child("item_prices").child("Mug").child("0").setValue("10");
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.exit){
            Log.d("CreateActivity", "selected exit");
            showConfirmDialog();
        }
        else{
            backFragment();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showConfirmDialog(){
        new AlertDialog.Builder(this).setTitle("Are you sure you want to exit?")
                .setMessage("Your progress won't be saved.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        }).show();
    }

    public void startFragment(Fragment fragment){
        if(fragmentsList.size() > 0){
            fm.beginTransaction().hide(fragmentsList.get(fragmentsList.size()-1)).add(R.id.create_frame_layout, fragment).commit();
        }
        else {
            fm.beginTransaction().add(R.id.create_frame_layout, fragment).commit();
        }
        currentStep+=1;
        String stepString = "Step " + currentStep;
        stepText.setText(stepString);
        progressBar.incrementProgressBy(100/NUM_STEPS);
        fragmentsList.add(fragment);
    }

    public void backFragment(){
        if(fragmentsList.size() > 1){
            if(fragmentsList.size() == 3 && ((StepThreeFragment)(fragmentsList.get(2))).getIsEdited()){
                showWarning();
            }
            else backFragmentHelper();
        }
        else finish();
    }

    private void backFragmentHelper(){
        fm.beginTransaction().remove(fragmentsList.get(fragmentsList.size() - 1)).commit();
        fragmentsList.remove(fragmentsList.size() - 1);

        fm.beginTransaction().show(fragmentsList.get(fragmentsList.size() - 1)).commit();
        currentStep -= 1;
        String stepString = "Step " + currentStep;
        stepText.setText(stepString);
        progressBar.incrementProgressBy(-100 / NUM_STEPS);
    }

    private void showWarning(){
        new AlertDialog.Builder(this).setTitle("Are you sure?")
                .setMessage("If you return to the prior step, your design edits will not be saved.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        backFragmentHelper();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        }).show();
    }

    public void uploadPost(){
        Log.d("CreateActivity", "uploadPost()");
        // Close fragments and send to dB
        Toast.makeText(getApplicationContext(), "All set - your listing is active!", Toast.LENGTH_LONG).show();
        finish();
//        MainActivity.updateFeedImages();
    }

    public ArrayList<String> getEnabledCategories(){
        return ((StepOneFragment)fragmentsList.get(0)).getSelectedCategoriesList();
    }
}
