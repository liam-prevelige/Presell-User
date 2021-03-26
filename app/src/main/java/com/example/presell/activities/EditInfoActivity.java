package com.example.presell.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.presell.GlideApp;
import com.example.presell.R;
import com.example.presell.models.Login;
import com.google.android.material.textfield.TextInputEditText;
import com.soundcloud.android.crop.Crop;

import java.util.ArrayList;

public class EditInfoActivity extends RegisterActivity {
    private ImageView mProfilePic;
    private TextInputEditText mFirstNameText, mLastNameText, mEmailText, mPasswordText, mPhoneText;
    private Button mSaveButton, mDeleteButton;
    private RadioButton mFemaleButton;
    private RadioButton mMaleButton;
    private Login mLogin;

    private ArrayList<String> changedValues;
    private String mFirstName, mLastName, mGender, mEmail, mPassword, mPhone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable((ContextCompat.getColor(getApplicationContext(), R.color.darkBackground))));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogin = new Login(getApplicationContext());
        changedValues = new ArrayList<String>();

        setupViews();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void setupViews(){
        mProfilePic = findViewById(R.id.camera_icon);
        mFirstNameText = findViewById(R.id.first_name);
        mLastNameText = findViewById(R.id.last_name);
        mEmailText = findViewById(R.id.email);
        mPasswordText = findViewById(R.id.password);
        mPhoneText = findViewById(R.id.phone);
        mSaveButton = findViewById(R.id.save_button);
        mDeleteButton = findViewById(R.id.delete_button);
        mFemaleButton = findViewById(R.id.is_female);
        mMaleButton = findViewById(R.id.is_male);

        mEmailText.setEnabled(false);

        getInitialInfo();
    }

    private void getInitialInfo(){
        mFirstName = mLogin.getFirstName();
        mLastName = mLogin.getLastName();
        mGender = mLogin.getGender();
        mEmail = mLogin.getEmail();
        mPassword = mLogin.getPassword();
        mPhone = mLogin.getPhone();

        fillInitialInfo();
    }

    private void fillInitialInfo(){
        mFirstNameText.setText(mFirstName);
        mLastNameText.setText(mLastName);
        if(mGender.equals("male")) mMaleButton.setEnabled(true);
        else mFemaleButton.setEnabled(true);
        mEmailText.setText(mEmail);
        mPasswordText.setText(mPassword);
        mPhoneText.setText(mPhone);

        if(!mLogin.getProfilePicUrl().equals("empty")){
            GlideApp.with(this)
                    .load(mLogin.getProfilePicUrl())
                    .centerInside()
                    .circleCrop()
                    .override(90, 90)
                    .into(mProfilePic);
        }
        setupButtons();
    }

    private void setupButtons(){
        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions();
                AlertDialog.Builder builder = new AlertDialog.Builder(EditInfoActivity.this);              // Create dialog if user didn't grant permissions
                builder.setTitle("Upload Design");
                builder.setPositiveButton("Choose from phone gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onItemSelected(RegisterActivity.PHOTO_FROM_GALLERY_CODE);
                    }
                });
                builder.show();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(changesMade()){

                }
                else{
                    Toast.makeText(getApplicationContext(), "No changes made to account information", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditInfoActivity.this);              // Create dialog if user didn't grant permissions
                builder.setTitle("Warning: You are about to delete your account!").setMessage("Are you sure you want to do this? All account information will be lost with no method of recovery...`````");
                builder.setPositiveButton("Yes I'm sure!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete account
                    }
                }).setNegativeButton("No, don't delete!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    private boolean changesMade(){
        boolean changed = false;
        if(!mFirstNameText.getText().toString().equals(mFirstName)) changedValues.add(mFirstName);
        if(!mLastNameText.getText().toString().equals(mLastName)) changedValues.add(mLastName);
        if(mFemaleButton.isChecked() && !("female".equals(mGender))) changedValues.add(mGender);
        if(mMaleButton.isChecked() && !("male".equals(mGender))) changedValues.add(mGender);
        if(!mEmailText.getText().toString().equals(mEmail)) changedValues.add(mEmail);
        if(!mPasswordText.getText().toString().equals(mPassword)) changedValues.add(mPassword);
        if(!mPhoneText.getText().toString().equals(mPhone)) changedValues.add(mPhone);

        if(changedValues.size() > 0) changed = true;
        return changed;
    }

    @Override
    public void handleCrop(int resultCode, Intent result) {
        if (resultCode != RESULT_OK) return;
        try {
            chosenBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Crop.getOutput(result));

            GlideApp.with(this)
                    .load(chosenBitmap)
                    .centerInside()
                    .circleCrop()
                    .override(90, 90)
                    .into(mProfilePic);
            profilePicEdited = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
