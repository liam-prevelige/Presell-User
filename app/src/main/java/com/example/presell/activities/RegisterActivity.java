package com.example.presell.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.presell.BuildConfig;
import com.example.presell.GlideApp;
import com.example.presell.R;
import com.example.presell.models.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soundcloud.android.crop.Crop;

import com.stripe.android.Stripe;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Conduct profile registration and store corresponding information in firebase dB and authentication
 */
public class RegisterActivity extends AppCompatActivity {
    public static final int CHOOSE_PHOTO_REQUEST = 0;
    public static final int PHOTO_FROM_GALLERY_CODE = 1;

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE = 3;

    private ImageView mProfilePic;
    private EditText firstName, lastName, email, password, phone, username;
    private Button registerButton;
    private RadioButton isFemale, isMale;
    private Class changeToActivity;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    public Bitmap chosenBitmap;
    private File mFile;
    private Uri mUri;
    public boolean profilePicEdited;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        changeToActivity = LoginActivity.class;     // Activity to be returned to - useful if implementing "edit profile"
        profilePicEdited = false;

        setProfileVariables();
        registerAndPicButton();
    }

    /**
     * If a user chooses to register their profile, store info in dB if all values filled in
     * Otherwise return to Login
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.confirm_register) {
            handleRegistration();
        }
        else {
            if(changeToActivity.equals(LoginActivity.class)) finishAffinity();      // Clear activity stack if going back to Login
            startActivity(new Intent(this, changeToActivity));
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Create references to input fields
     */
    private void setProfileVariables(){
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        username = findViewById(R.id.username_text);
        phone = findViewById(R.id.phone);
        isFemale = findViewById(R.id.is_female);
        isMale  = findViewById(R.id.is_male);
        mProfilePic = findViewById(R.id.camera_icon);
        registerButton = findViewById(R.id.confirm_register);

    }

    /**
     * Check whether required profile variables are empty or improperly formatted
     * If incorrect formatting, produce an error using helper method
     *
     * @return whether the text box inputs were all formatted correctly
     */
    private boolean someProfileVarsEmpty(){
        boolean invalid = false;
        if(firstName.getText().toString().equals("")){   // must have name
            requestError(firstName);
            invalid = true;
        }
        if(lastName.getText().toString().equals("")){   // must have name
            requestError(lastName);
            invalid = true;
        }
        if(email.getText().toString().equals("")){  // must have email
            requestError(email);
            invalid = true;
        }
        if(username.getText().toString().equals("")){  // must have username
            requestError(username);
            invalid = true;
        }
        // Use default Android method to determine whether the email was formatted properly
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
            email.setError("This email address is invalid");
            invalid = true;
        }
        if(password.getText().toString().equals("")){   // must have password
            requestError(password);
            invalid = true;
        }
        if(!(isMale.isChecked() || isFemale.isChecked())){  // must choose gender
            Toast.makeText(getApplicationContext(), "Gender is a required field", Toast.LENGTH_LONG).show();
            invalid = true;
        }
        return invalid;
    }

    /**
     * Helper method to provide an error for a required EditText component having an empty field
     */
    private void requestError(EditText errorProducer){
        errorProducer.setError("This field is required");
    }

    /**
     * If the register button is clicked, send info to dB
     */
    private void registerAndPicButton(){
        registerButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                handleRegistration();
            }
        });
        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions();
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);              // Create dialog if user didn't grant permissions
                builder.setTitle("Upload Design");
                builder.setPositiveButton("Choose from phone gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onItemSelected(PHOTO_FROM_GALLERY_CODE);
                    }
                });
                builder.show();
            }
        });
    }

    /**
     * Ensure the user has given permissions necessary for using the camera
     */
    public void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CHOOSE_PHOTO_REQUEST);
        }
    }

    /**
     * Determine whether user has allowed permissions, and handle outcome accordingly
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);              // Create dialog if user didn't grant permissions
            alert.setTitle("Important Permissions");
            alert.setMessage("Allow access to upload photos from your device.");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    checkPermissions();
                }
            });
            alert.show();
        }
    }

    /**
     * Manage process of upload/capturing photo based on the option the user has selected
     */
    public void onItemSelected(int code) {
        Intent intent;
        switch (code) {
            case PHOTO_FROM_GALLERY_CODE:
                try {
                    mFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE);
                break;
        }
    }

    /**
     * If all values properly formatted, add the information to firebase and return to Login
     */
    private void handleRegistration(){
        if(!someProfileVarsEmpty()){
            if(password.getText().toString().length() >= MIN_PASSWORD_LENGTH) {
                addProfileToFirebase();
                Toast.makeText(getBaseContext(), "Creating your account - one moment please.", Toast.LENGTH_SHORT).show();
            }
            else{
                password.setError("Password must be at least six characters");
            }
        }
    }

    /**
     * Create the file with unique formatting
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "JPEG_" + timeStamp + "_";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(fileName, ".jpg", storageDirectory);
    }

    /**
     * Add information to firebase authentication and store login information to realtime dB
     */
    private void addProfileToFirebase(){
        // Add user as a valid input in authentication system
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Display message to user depending on whether addition of account was successful
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();

                            if(user!=null) {
                                userId = user.getUid();
                                updateStorageAndDatabase();
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Failed to add account, please try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    /**
     * Manage the image incoming and handle differently based on from where it's being returned
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        assert data != null;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_IMAGE_ACTIVITY_REQUEST_CODE:
                    Uri selectedImage = data.getData();
                    try {
                        Bitmap galleryBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        setImageDetails(galleryBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case Crop.REQUEST_CROP:
                    handleCrop(resultCode, data);   // Allow user to crop image for display
                    break;
            }
        }
    }

    /**
     * Get the details of the image location and crop
     */
    private void setImageDetails(Bitmap bm) {
        try {
            FileOutputStream fOut = new FileOutputStream(mFile);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();

            chosenBitmap = bm;
            beginCrop();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Call the crop of the image, which will have a result that needs to be managed
     */
    private void beginCrop() {
        if (mFile != null) {
            mUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, mFile);
            Crop.of(mUri, mUri).start(this);
        }
    }

    /**
     * Handle whatever the crop activity sends back and set the UI accordingly
     */
    public void handleCrop(int resultCode, Intent result) {
        if (resultCode != RESULT_OK) return;
        try {
            chosenBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Crop.getOutput(result));

            GlideApp.with(this)
                    .load(chosenBitmap)
                    .centerInside()
                    .circleCrop()
                    .into(mProfilePic);
            profilePicEdited = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStorageAndDatabase(){
        databaseReference = FirebaseDatabase.getInstance().getReference().child(userId);

        if(profilePicEdited){
            String pathString = "profile pics/" + userId + System.currentTimeMillis() + ".jpg";

            final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(pathString);
            storageReference.putFile(mUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            databaseReference.child("profile pic").setValue(uri.toString());
                            updateDatabaseInfo();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(RegisterActivity.this, "Error creating account! Please check your internet connection.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            updateDatabaseInfo();
        }
    }

    private void updateDatabaseInfo() {
        // Create a reference based on email and add user information within
        String gender = "";
        if(isMale.isChecked()) gender = "male";
        else gender = "female";
        databaseReference.child("gender").setValue(gender);
        databaseReference.child("firstName").setValue(firstName.getText().toString());
        databaseReference.child("lastName").setValue(lastName.getText().toString());
        databaseReference.child("phone").setValue(phone.getText().toString());
        databaseReference.child("password").setValue(password.getText().toString());
        databaseReference.child("email").setValue(email.getText().toString());
        databaseReference.child("has_custom").setValue("false");

        Map<String, Object> customerParams = new HashMap<String, Object>();
        customerParams.put("description", userId);
//        Customer mCustomer = null;
//
//        try {
//            Stripe.apiKey = "sk_test_51HEf0THvpbpmrUfitRVs4ADV4pbuYOPJR8KSq7Si4SP2rJjpXC69TMOauXrLSTT5yZSqAEbO7VPadnKgM62ezMim00NvTu8Jle";
//            mCustomer = Customer.create(customerParams);
//        }
//        catch(StripeException e){
//            e.printStackTrace();
//        }

//        databaseReference.child("customer").setValue(mCustomer);
        databaseReference.child("username").setValue(username.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "All set, your account has been created!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getBaseContext(), LoginActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error creating account! Please check your internet connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClickBackToLogin(View view) {
        finishAffinity();
        startActivity(new Intent(this, LoginActivity.class));
    }
}
