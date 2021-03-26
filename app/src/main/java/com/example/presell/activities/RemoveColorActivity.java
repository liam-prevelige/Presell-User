package com.example.presell.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
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

public class RemoveColorActivity extends AppCompatActivity {
    private File mFile;
    private Bitmap mBitmap;
    private boolean edited;
    private Button removeButton, undoButton, redoButton, saveButton;
    private ProgressBar progressBar;
    private FastBitmap editedBitmap;
    private ImageView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_borders);

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

                setupImageClick();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupImageClick(){
        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    float touchX = event.getX();
                    float touchY = event.getY();

                    drawPoint(touchX, touchY, 80);
                }
                return false;
            }
        });
    }

    private void drawPoint(float x,float y, int radius){
        Log.d("RemoveColorActivity", "Draw Point");

        int[] posXY = new int[2];
        mImageView.getLocationOnScreen(posXY);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GREEN);

        Bitmap tempBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(tempBitmap);

        tempCanvas.drawBitmap(mBitmap, 0, 0, null);

        float wConvert = (float)mBitmap.getWidth()/mImageView.getWidth();
        float hConvert = (float)mBitmap.getHeight()/mImageView.getHeight();

        tempCanvas.drawCircle(wConvert*x, hConvert*y, radius, paint);

        mImageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(edited) showConfirmDialog();
        else finish();
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

    private void removeColor(Color color, Bitmap bitmap, int startX, int startY, int colorThreshold){
        int maxWidth = bitmap.getWidth();
        int maxHeight = bitmap.getHeight();
    }

//    private class ApplyFilterTask extends AsyncTask<Void, Void, FastBitmap> {
//        private FastBitmap fastBitmap;
//        private String command;
//
//        private ApplyFilterTask(String command) {
//            progressBar.setVisibility(View.VISIBLE);
//            progressBar.bringToFront();
//
//            this.command = command;
//            this.fastBitmap = editedBitmap;
//        }
//
//        @Override
//        protected FastBitmap doInBackground(Void... voids) {
//            switch(command){
//                case SEPIA_EFFECT:
//                    sepia.applyInPlace(fastBitmap);
//                    break;
//                case EMBOSS_EFFECT:
//                    emboss.applyInPlace(fastBitmap);
//                    break;
//                case BLUR_EFFECT:
//                    blur.applyInPlace(fastBitmap);
//                    break;
//            }
//            return fastBitmap;
//        }
//
//        protected void onPostExecute(FastBitmap result) {
//            if(result!=null) {
//                mBitmap = result.toBitmap();
//                mImageView.setImageBitmap(mBitmap);
//                mImageView.bringToFront();
//                progressBar.setVisibility(View.INVISIBLE);
//                fastBitmap = result;
//            }
//        }
//    }
}
