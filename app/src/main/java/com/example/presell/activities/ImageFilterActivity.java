package com.example.presell.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.presell.R;
import com.example.presell.fragments.StepThreeFragment;

import java.io.File;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Blur;
import Catalano.Imaging.Filters.ColorFiltering;
import Catalano.Imaging.Filters.Emboss;
import Catalano.Imaging.Filters.GaussianBlur;
import Catalano.Imaging.Filters.Sepia;

public class ImageFilterActivity extends AppCompatActivity {
    private static final String SEPIA_EFFECT = "sepia effect";
    private static final String EMBOSS_EFFECT = "emboss effect";
    private static final String BLUR_EFFECT = "blur effect";
    private static final String RED_EFFECT = "red effect";
    private static final String BLUE_EFFECT = "blue effect";
    private static final String GREEN_EFFECT = "green effect";

    private File mFile;
    private Bitmap mBitmap;
    private boolean edited;
    private Button sepiaButton, embossButton, blurButton, redButton, blueButton, greenButton;
    private ProgressBar progressBar;
    private FastBitmap editedBitmap;
    private Sepia sepia;
    private Emboss emboss;
    private GaussianBlur blur;
    private ColorFiltering colorFilter;
    private ImageView mImageView;

    public void setFileAndBitmap(File file, Bitmap bitmap){
        Log.d("ImageFilterActivity", "setFileAndBitmap()");
        mFile = file;
        edited = false;
        mBitmap = bitmap;
        editedBitmap = new FastBitmap(mBitmap.copy(Bitmap.Config.RGB_565, true));
        editedBitmap.toRGB();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_filter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable((ContextCompat.getColor(getApplicationContext(), R.color.darkBackground))));

        edited = false;
        mImageView = findViewById(R.id.selected_image);
        progressBar = findViewById(R.id.progress_bar);

        Intent passedIntent = getIntent();
        if(passedIntent.getExtras() != null){
            mFile = (File)passedIntent.getExtras().get(StepThreeFragment.FILE_KEY);
            mBitmap = BitmapFactory.decodeFile(mFile.getPath());
            if(mBitmap != null) {
                editedBitmap = new FastBitmap(mBitmap.copy(Bitmap.Config.RGB_565, true));
                editedBitmap.toRGB();
                mImageView.setImageBitmap(mBitmap);
                setupFilterButtons();
            }
        }

        setupUndoRedoButtons();
        setupSaveButtons();
    }

    private void setupFilterButtons(){
        sepiaButton = findViewById(R.id.button_sepia);
        embossButton = findViewById(R.id.button_emboss);
        blurButton = findViewById(R.id.button_blur);
        redButton = findViewById(R.id.button_red);
        blueButton = findViewById(R.id.button_blue);
        greenButton = findViewById(R.id.button_green);

        sepia = new Sepia();
        emboss = new Emboss();
        blur = new GaussianBlur();
        colorFilter = new ColorFiltering();

        setupFilterClicks();
    }

    private void setupFilterClicks(){
        sepiaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ApplyFilterTask(SEPIA_EFFECT).execute();
          }
        });

        embossButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ApplyFilterTask(EMBOSS_EFFECT).execute();
            }
        });

        blurButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ApplyFilterTask(BLUR_EFFECT).execute();
            }
        });

        redButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mImageView.setColorFilter(Color.RED);
            }
        });

        greenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        blueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void setupUndoRedoButtons(){

    }

    private void setupSaveButtons(){

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(edited) showConfirmDialog();
        return super.onOptionsItemSelected(item);
    }

    private void showConfirmDialog(){
        new AlertDialog.Builder(this).setTitle("Are you sure you want to exit?")
                .setMessage("Your edits won't be saved.")
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


    private class ApplyFilterTask extends AsyncTask<Void, Void, FastBitmap> {
        private FastBitmap fastBitmap;
        private String command;

        private ApplyFilterTask(String command) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront();

            this.command = command;
            this.fastBitmap = editedBitmap;
        }

        @Override
        protected FastBitmap doInBackground(Void... voids) {
            switch(command){
                case SEPIA_EFFECT:
                    sepia.applyInPlace(fastBitmap);
                    break;
                case EMBOSS_EFFECT:
                    emboss.applyInPlace(fastBitmap);
                    break;
                case BLUR_EFFECT:
                    blur.applyInPlace(fastBitmap);
                    break;
            }
            return fastBitmap;
        }

        protected void onPostExecute(FastBitmap result) {
            if(result!=null) {
                mBitmap = result.toBitmap();
                mImageView.setImageBitmap(mBitmap);
                mImageView.bringToFront();
                progressBar.setVisibility(View.INVISIBLE);
                fastBitmap = result;
            }
        }
    }
}
