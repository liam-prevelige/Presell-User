package com.example.presell.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.presell.GlideApp;
import com.example.presell.R;
import com.example.presell.activities.EditInfoActivity;
import com.example.presell.activities.LoginActivity;
import com.example.presell.activities.RegisterActivity;
import com.example.presell.activities.SellerHubActivity;
import com.example.presell.activities.SettingsActivity;
import com.example.presell.models.Login;

public class MyProfileFragment extends Fragment {
    private boolean noSignIn;
    private Login mLogin;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mLogin = new Login(requireContext().getApplicationContext());
        noSignIn = mLogin.getNoSignIn();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        if(noSignIn) {
            view = inflater.inflate(R.layout.fragment_my_profile_no_login, container, false);
        }
        else{
            view = inflater.inflate(R.layout.fragment_my_profile_login, container, false);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(!noSignIn) {
            updateNameAndPic(view);
            setupClicksLogin(view);
        }
        else {
            setupClicksNoLogin(view);
        }
    }

    private void updateNameAndPic(View view){
        TextView nameText = view.findViewById(R.id.name_text);
        ImageView mProfilePic = view.findViewById(R.id.profile_pic);

        String nameString = mLogin.getFirstName() + " " + mLogin.getLastName();
        nameText.setText(nameString);

        String imageUrl = mLogin.getProfilePicUrl();
        if(!imageUrl.equals("empty")){
            GlideApp.with(requireActivity())
                    .load(imageUrl)
                    .override(90, 90)
                    .centerInside()
                    .circleCrop()
                    .into(mProfilePic);
        }
    }

    private void setupClicksLogin(View view){
        Button editInfoButton = view.findViewById(R.id.edit_info_text);
        editInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(requireActivity(), EditInfoActivity.class));
            }
        });

        setupOverlapButtons(view);
    }

    private void setupClicksNoLogin(View view){
        Button signUpButton = view.findViewById(R.id.sign_up_button);
        Button loginButton = view.findViewById(R.id.login_button);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().finishAffinity();
                startActivity(new Intent(requireActivity(), RegisterActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().finishAffinity();
                startActivity(new Intent(requireActivity(), LoginActivity.class));
            }
        });

        setupOverlapButtons(view);
    }

    private void setupOverlapButtons(View view){
        Button settingsButton = view.findViewById(R.id.settings_text);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(requireActivity(), SettingsActivity.class));
            }
        });
        Button sellerHubButton = view.findViewById(R.id.seller_text);
        sellerHubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(requireActivity(), SellerHubActivity.class));
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(!noSignIn) inflater.inflate(R.menu.my_profile_menu_login, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean returnVal = super.onOptionsItemSelected(item);
        if(!noSignIn && item.getItemId() == R.id.logout){
            requireActivity().finish();
        }
        return returnVal;
    }
}
