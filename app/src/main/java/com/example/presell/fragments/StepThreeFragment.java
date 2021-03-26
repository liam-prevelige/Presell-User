package com.example.presell.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.presell.BuildConfig;
import com.example.presell.R;
import com.example.presell.activities.CreateActivity;
import com.example.presell.activities.ImageFilterActivity;
import com.example.presell.activities.RemoveColorActivity;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StepThreeFragment extends Fragment {
    public static final String FILE_KEY = "file_key";
    public static final String BITMAP_KEY = "bitmap_key";

    private static final int RESULT_OK = -1;

    private View view;
    private Bitmap mBitmap;
    private ImageView selectedImage;
    private File mFile;
    private boolean edited;

    public StepThreeFragment(File file, Bitmap bitmap){
        mBitmap = bitmap;
        mFile = file;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step_three, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        edited = false;

        ImageView fillerImage = view.findViewById(R.id.background_image);
        fillerImage.setVisibility(View.INVISIBLE);

        int scale = (int) getResources().getDimension(R.dimen.image_length);
        selectedImage = view.findViewById(R.id.selected_image);
        selectedImage.setVisibility(View.VISIBLE);
        selectedImage.setImageBitmap(mBitmap);

        setupEditButtons();
        setupConfirmButton();
    }

    public boolean getIsEdited(){
        return edited;
    }

    private void setupEditButtons(){
        Button cropButton = view.findViewById(R.id.crop_button);
        Button rotateButton = view.findViewById(R.id.rotate_button);
//        Button tintButton = view.findViewById(R.id.tint_button);
//        Button borderButton = view.findViewById(R.id.border_button);

        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edited = true;
                beginCrop();
            }
        });

        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edited = true;
                mBitmap = rotate(mBitmap, 90);
                updateImageInMemory();
            }
        });

//        tintButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                edited = true;
//                startImageFilterActivity();
//            }
//        });

//        borderButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                edited = true;
//                Intent intent = new Intent(getActivity(), RemoveColorActivity.class);
//                intent.putExtra(FILE_KEY, mFile);
//
//                startActivity(intent);
//            }
//        });
    }

    private void startImageFilterActivity(){
        Intent intent = new Intent(getActivity(), ImageFilterActivity.class);
        intent.putExtra(FILE_KEY, mFile);

        startActivity(intent);
//        ImageFilterActivity imageFilterActivity = new ImageFilterActivity();
//        imageFilterActivity.setFileAndBitmap(mFile, mBitmap);
//        startActivity(new Intent(getContext(), imageFilterActivity.getClass()));
    }

    private void setupConfirmButton(){
        Button confirmButton = view.findViewById(R.id.confirm_edits_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CreateActivity)requireActivity()).startFragment(new StepFourFragment(mFile));
            }
        });
    }

    private void updateImageInMemory(){
        try {
            FileOutputStream fOut = new FileOutputStream(mFile);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();

            selectedImage.setImageBitmap(mBitmap);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Helper method for rotating Bitmap at a given angel
     */
    private Bitmap rotate(Bitmap bm, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
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
                case Crop.REQUEST_CROP:
                    handleCrop(resultCode, data);   // Allow user to crop image for display
                    break;
            }
        }
    }

    /**
     * Call the crop of the image, which will have a result that needs to be managed
     */
    private void beginCrop() {
        if (mFile != null) {
            Uri fileLocation = FileProvider.getUriForFile(requireActivity(), BuildConfig.APPLICATION_ID, mFile);
            Crop.of(fileLocation, fileLocation).start(requireActivity(), this);
        }
    }

    /**
     * Handle whatever the crop activity sends back and set the UI accordingly
     */
    private void handleCrop(int resultCode, Intent result) {
        if (resultCode != RESULT_OK) return;
        try {
            mBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Crop.getOutput(result));
            selectedImage.setImageBitmap(mBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
