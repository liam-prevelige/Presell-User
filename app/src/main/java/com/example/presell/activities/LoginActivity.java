package com.example.presell.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.presell.R;
import com.example.presell.models.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Initial page of app, allow user to login with their registered email/password (or access
 * registration if not yet created)
 */
public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TEST", "IS IT WORKING?");
        super.onCreate(savedInstanceState);
        if(getActionBar()!=null) getActionBar().setTitle("Sign in");
        setContentView(R.layout.activity_login);
        signIn();       // setup and handle button click for signing in
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    /**
     * Create listener for sign in button and check current login against those stored in firebase
     * authentication
     */
    private void signIn(){
        Button signInButton = findViewById(R.id.sign_in);
        signInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText emailEntry = findViewById(R.id.email);
                EditText passwordEntry = findViewById(R.id.password);
                if(noLoginInputErrors(emailEntry, passwordEntry)){
                    handleFirebaseSignIn();
                }
            }
        });
    }

    /**
     * Given an email and password, ensure everything has been entered correctly (value exists, correct
     * length and formatting). Produce an error otherwise
     */
    private boolean noLoginInputErrors(EditText emailEntry, EditText passwordEntry){
        boolean correctLoginInput = true;

        if(emailEntry.getText().toString().equals("")){     // Error if no email entered
            emailEntry.setError("This field is required");
            correctLoginInput = false;
        }
        // Use provided Android functionality to check whether the Email Address is properly formatted
        else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(emailEntry.getText().toString()).matches()){
            emailEntry.setError("This email address is invalid");
            correctLoginInput = false;
        }
        if(passwordEntry.getText().toString().equals("")){      // Error if no password entered
            passwordEntry.setError("This field is required");
            correctLoginInput = false;
        }
        if(passwordEntry.getText().toString().length() < 7){    // Error if password incorrect length
            passwordEntry.setError("Password must be more than six characters");
            correctLoginInput = false;
        }
        return correctLoginInput;
    }

    /**
     * Check with Firebase authentication service whether or not an email exists
     *
     * If successful send to mainactivity, otherwise notify user of incorrect login
     */
    private void handleFirebaseSignIn(){
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();  // Connect to firebase authentication

        // Get text entry for email/password
        final EditText emailEntry = findViewById(R.id.email);
        final EditText passwordEntry = findViewById(R.id.password);

        // Try signing in with current login using FirebaseAuthentication built in service
        mAuth.signInWithEmailAndPassword(emailEntry.getText().toString(), passwordEntry.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // If successful load user's info from dB to shared preferences for later use
                            FirebaseUser user = mAuth.getCurrentUser();
                            String email = emailEntry.getText().toString();
                            String password = passwordEntry.getText().toString();
                            assert user != null;
                            String userId = user.getUid();

                            setupLoginInfo(email, password, userId);

//                            // Send user to main activity to access app features
//                            startActivity(new Intent(LoginActivity.this, MainActivity.class));

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Sign in failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupLoginInfo(String email, String password, String userId){
        Login mLogin = new Login(getApplicationContext(), this, email, password, userId);
        mLogin.setNoSignIn(false);
        Toast.makeText(LoginActivity.this, "Logging you in!", Toast.LENGTH_SHORT).show();
    }

    public void onClickSignUp(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void onClickNoSignIn(View view) {
        Login login = new Login(getApplicationContext());
        login.setNoSignIn(true);

        startActivity(new Intent(this, MainActivity.class));
    }


}
