package com.example.presell.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.presell.BuildConfig;
import com.example.presell.R;
import com.example.presell.activities.CreateActivity;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import Catalano.Imaging.FastBitmap;

public class StepTwoFragment extends Fragment {
    private static final int CHOOSE_PHOTO_REQUEST = 0;
    private static final int RESULT_OK = -1;
    private static final int PHOTO_FROM_GALLERY_CODE = 1;
    private static final int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE = 3;

    private View view;
    private Bitmap chosenBitmap;
    private File mFile;
    private Uri mUri;
    private ImageView mImage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step_two, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        setupConfirmButton();
        createPhotoButtonListener();
    }

    private void setupConfirmButton(){
        Button confirm = view.findViewById(R.id.confirm_image_button);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(chosenBitmap!=null) {
                    ((CreateActivity) requireActivity()).startFragment(new StepThreeFragment(mFile, chosenBitmap));
                }
                else{
                    Toast.makeText(requireContext(), "Choose design to upload before continuing", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Allow a user to add new images to their post upon click of related button
     */
    private void createPhotoButtonListener() {
        Button uploadButton = view.findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();                 //make sure permissions are set up
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());        //create the dialog box for selecting pictures
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
    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CHOOSE_PHOTO_REQUEST);
        }
    }

    /**
     * Determine whether user has allowed permissions, and handle outcome accordingly
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());              // Create dialog if user didn't grant permissions
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
    private void onItemSelected(int code) {
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
     * Create the file with unique formatting
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "JPEG_" + timeStamp + "_";
        File storageDirectory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(fileName, ".jpg", storageDirectory);
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
                        Bitmap galleryBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                        setImageDetails(galleryBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
            mImage = requireActivity().findViewById(R.id.selected_image);
            mImage.setBackground(new Drawable() {
                @Override
                public void draw(@NonNull Canvas canvas) { }
                @Override
                public void setAlpha(int i) { }
                @Override
                public void setColorFilter(@Nullable ColorFilter colorFilter) { }
                @Override
                public int getOpacity() {
                    return PixelFormat.UNKNOWN;
                }
            });
            mImage.setImageBitmap(chosenBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        mUri = FileProvider.getUriForFile(requireActivity(), BuildConfig.APPLICATION_ID, mFile);
//        beginCrop(mUri);
    }
}
