package com.example.presell.models;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.presell.activities.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

/**
 * Store information related to one individual's Login/Profile
 *
 * Load information to SharedPreferences based on inputs in Firebase db
 * Use SharedPreference so information can be accessed across activities and app sessions
 */
public class Login {
    public static final CharSequence PERIOD_REPLACEMENT_KEY = "hgiasdvohekljh91-76";
    private SharedPreferences profile;
    private DatabaseReference databaseReference;
//    private Customer customer;
    private String firstName, lastName, gender, phone, userId, username, profilePicUrl, password;

    /**
     * Called everywhere except LoginActvity, gets information from application sharedpreferences
     */
    public Login(Context applicationContext){
        profile = PreferenceManager.getDefaultSharedPreferences(applicationContext);
    }

    /**
     * Only called in LoginActivity so user information can be added into sharedpreferences
     * from db and accessed throughout app w/out reloading each time
     */
    public Login(Context applicationContext, Activity callingActivity, String email, String password, String userId){
        profile = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        profile.edit().putString("email", email).apply();
        profile.edit().putString("password", password).apply();
        profile.edit().putString("userId", userId).apply();
        profile.edit().putBoolean("no sign in", false).apply();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child(userId);
        setDefaultStringVals();
        setupDatabaseListener(callingActivity);
    }

    private void setDefaultStringVals(){
        firstName = "";
        lastName = "";
        gender = "";
        phone = "";
    }

    /**
     * Get user-related information from database upon login
     */
    private void setupDatabaseListener(final Activity callingActivity) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("firstName").exists()) {
                    firstName = (String) dataSnapshot.child("firstName").getValue();
                    profile.edit().putString("firstName", firstName).apply();
                }
                if (dataSnapshot.child("lastName").exists()) {
                    lastName = (String) dataSnapshot.child("lastName").getValue();
                    profile.edit().putString("lastName", lastName).apply();
                }
                if (dataSnapshot.child("gender").exists()) {
                    gender = (String) dataSnapshot.child("gender").getValue();
                    profile.edit().putString("gender", gender.toLowerCase()).apply();
                }
                if (dataSnapshot.child("phone").exists()) {
                    phone = (String) dataSnapshot.child("phone").getValue();
                    profile.edit().putString("phone", phone).apply();
                }
                if (dataSnapshot.child("username").exists()) {
                    username = (String) dataSnapshot.child("username").getValue();
                    profile.edit().putString("username", username).apply();
                }
                if (dataSnapshot.child("profile pic").exists()) {
                    profilePicUrl = (String) dataSnapshot.child("profile pic").getValue();
                    profile.edit().putString("profile pic", profilePicUrl).apply();
                }
                if (dataSnapshot.child("password").exists()) {
                    password = (String) dataSnapshot.child("password").getValue();
                    profile.edit().putString("password", password).apply();
                }
//                if (dataSnapshot.child("customer").exists()) {
//                    customer = dataSnapshot.child("customer").getValue(Customer.class);
//                    Gson gson = new Gson();
//                    String json = gson.toJson(customer);
//                    profile.edit().putString("customer", json).apply();
//                }

                callingActivity.startActivity(new Intent(callingActivity, MainActivity.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DatabaseError", "Error loading profile");
            }
        });
    }

    /**
     * Always return values when getters called from SharedPreferences so dB isn't reloaded
     */
    public String getFirstName(){
        return profile.getString("firstName", "empty");
    }

    public String getLastName(){
        return profile.getString("lastName", "empty");
    }

    public String getGender(){
        return profile.getString("gender", "empty");
    }

    public String getEmail(){
        return profile.getString("email", "empty");
    }

    public String getPhone(){
        return profile.getString("phone", "");
    }

    public boolean getNoSignIn(){return profile.getBoolean("no sign in", false);}

    public void setNoSignIn(Boolean isNoSignIn){
        profile.edit().putBoolean("no sign in", isNoSignIn).apply();
    }

    public String getUserId() {
        return profile.getString("userId", "empty");
    }

    public String getPassword(){
        return profile.getString("password", "");
    }

    public String getUsername() {
        return profile.getString("username", "empty");
    }

    public String getProfilePicUrl() {
        return profile.getString("profile pic", "empty");
    }
//
//    public Customer getCustomer() {
//        Gson gson = new Gson();
//        String json = profile.getString("customer", "empty");
//        return gson.fromJson(json, Customer.class);
//    }
}
