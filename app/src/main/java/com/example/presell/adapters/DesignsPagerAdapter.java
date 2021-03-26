package com.example.presell.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.presell.GlideApp;
import com.example.presell.R;
import com.example.presell.activities.BuyActivity;
import com.example.presell.models.Login;
import com.example.presell.models.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;

public class DesignsPagerAdapter extends RecyclerView.Adapter<DesignsPagerAdapter.ViewHolder> {
    private ViewHolder shirtHolder, mugHolder, coasterHolder, plateHolder, stickerHolder, airpodsHolder;

    private Bitmap mBitmap;
    private ArrayList<String> mCategoriesList;
    private LayoutInflater mLayoutInflater;
    private TextView categoryTextView;

    private boolean fromUrl, firstItem;
    private String imageUrl;
    private Context context;
    private Post post;

    private String coverCategoryItem;
    private int replacedCategoryIndex;

    public DesignsPagerAdapter(File file, ArrayList<String> categoriesList, Context context) {
        fromUrl = false;
        firstItem = true;

        mBitmap = BitmapFactory.decodeFile(file.getPath());
        mCategoriesList = categoriesList;

        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public DesignsPagerAdapter(Post post, Context context) {
        fromUrl = true;
        firstItem = true;

        this.post = post;
        this.imageUrl = post.getDesignURL();
        mCategoriesList = post.getSelectedCategories();

        this.context = context;

        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mDesign;
        private String itemType;

        private ViewHolder(View itemView) {
            super(itemView);
            mDesign = itemView.findViewById(R.id.selected_design_image);
        }

        public void setItemType(String itemType){
            this.itemType = itemType;
        }

        public String getItemType(){
            return itemType;
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Log.d("DesignsPagerAdapter", "onAttachedToRecyclerView()  " + recyclerView);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    //TODO: CREATE HELPER METHOD
    @NonNull
    @Override
    public DesignsPagerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("DesignsPagerAdapter", "onCreateViewHolder() " + mCategoriesList.get(viewType));
        String currentItem;

        if(fromUrl && firstItem){       // Check fromUrl since only uploaded posts have preferred category
            currentItem = post.getCoverCategory();
            coverCategoryItem = currentItem;
            replacedCategoryIndex = viewType;
            firstItem = false;
        }
        else{
            currentItem = mCategoriesList.get(viewType);
            if(currentItem.equals(coverCategoryItem)) currentItem = mCategoriesList.get(replacedCategoryIndex);
        }

        switch (currentItem) {
            case "T-Shirts":
                if(shirtHolder == null) {
                    shirtHolder = new ViewHolder(mLayoutInflater.inflate(R.layout.pager_item_shirt, parent, false));
                    shirtHolder.setItemType("T-Shirts");
                }
                return shirtHolder;
            case "Mugs":
                if(mugHolder == null) {
                    mugHolder = new ViewHolder(mLayoutInflater.inflate(R.layout.pager_item_mug, parent, false));
                    mugHolder.setItemType("Mugs");
                }
                return mugHolder;
            case "Coasters":
                if(coasterHolder == null) {
                    coasterHolder = new ViewHolder(mLayoutInflater.inflate(R.layout.pager_item_coaster, parent, false));
                    coasterHolder.setItemType("Coasters");
                }
                return coasterHolder;
            case "Plates":
                if(plateHolder == null) {
                    plateHolder = new ViewHolder(mLayoutInflater.inflate(R.layout.pager_item_plate, parent, false));
                    plateHolder.setItemType("Plates");
                }
                return plateHolder;
            case "Stickers":
                if(stickerHolder == null) {
                    stickerHolder = new ViewHolder(mLayoutInflater.inflate(R.layout.pager_item_sticker, parent, false));
                    stickerHolder.setItemType("Stickers");
                }
                return stickerHolder;
            default:
                if(airpodsHolder == null) {
                    airpodsHolder = new ViewHolder(mLayoutInflater.inflate(R.layout.pager_item_airpods_skin, parent, false));
                    airpodsHolder.setItemType("Airpods Skins");
                }
                return airpodsHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("DesignsPagerAdapter", "onBindViewHolder()");

        if(fromUrl){
            Glide.with(context).load(imageUrl).into(holder.mDesign);
            holder.mDesign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startBuyActivity(post);
                }
            });
        }
        else{
            holder.mDesign.setImageBitmap(mBitmap);
        }
    }

//    private String getPreferredCategory(int postIndex){
//        Login mLogin = new Login(context.getApplicationContext());
//
//        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
//
//        mRef.child(mLogin.getUserId()).child("-" + postIndex).child("coverCategory").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    private void startBuyActivity(Post mPost){
        Intent intent = new Intent(context, BuyActivity.class);
        intent.putExtra(PostRecyclerAdapter.POST_KEY, mPost);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        Log.d("DesignsPagerAdapter", "getItemCount()");

        return mCategoriesList.size();
    }
}