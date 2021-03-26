package com.example.presell.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.signature.ObjectKey;
import com.example.presell.GlideApp;
import com.example.presell.R;
import com.example.presell.models.Order;
import java.util.List;

public class OrdersAdapter extends ArrayAdapter<Order> {
    private Context context;
    private int resourceLayout;
    private List<Order> leftOrders;
    private List<Order> rightOrders;

    public OrdersAdapter(@NonNull Context context, int resource, @NonNull List<Order> leftOrders, List<Order> rightOrders) {
        super(context, resource, leftOrders);
        Log.d("OrdersAdapter", "OrdersAdapter()");

        this.context = context;
        this.resourceLayout = resource;
        this.leftOrders = leftOrders;
        this.rightOrders = rightOrders;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d("OrdersAdapter", "getView()");
        View mView = convertView;
        if (mView == null) {
            mView = LayoutInflater.from(context).inflate(resourceLayout, null);
            Order mOrderLeft = leftOrders.get(position);
            if (mOrderLeft != null) {
                ViewHolder holder = new ViewHolder(context);
                holder.leftLayout = mView.findViewById(R.id.left_image_view);
                holder.leftTTSText = mView.findViewById(R.id.left_time_prediction_text);
                holder.leftPriceText = mView.findViewById(R.id.left_price_text);

                getImage(mOrderLeft.getItemType(), holder.leftLayout, holder, mOrderLeft.getDesignUrl());

                String timeUntilSale = mOrderLeft.getItemTimeRemaining();
                holder.leftTTSText.setText(timeUntilSale);

                String itemPrice = "$" + mOrderLeft.getItemPrice();
                holder.leftPriceText.setText(itemPrice);
            }

            Order mOrderRight = null;
            if (!(position >= rightOrders.size())) {
                mOrderRight = rightOrders.get(position);
            }

            if (mOrderRight != null) {
                ViewHolder holder = new ViewHolder(context);
                holder.rightLayout = mView.findViewById(R.id.right_image_view);
                holder.rightTTSText = mView.findViewById(R.id.right_time_prediction_text);
                holder.rightPriceText = mView.findViewById(R.id.right_price_text);

                getImage(mOrderRight.getItemType(), holder.rightLayout, holder, mOrderRight.getDesignUrl());

                String timeUntilSale = mOrderRight.getItemTimeRemaining();
                holder.rightTTSText.setText(timeUntilSale);

                String itemPrice = "$" + mOrderRight.getItemPrice();
                holder.rightPriceText.setText(itemPrice);
            } else {
                RelativeLayout rightLayout = mView.findViewById(R.id.right_image_view);
                TextView rightTTSText = mView.findViewById(R.id.right_time_prediction_text);
                TextView rightPriceText = mView.findViewById(R.id.right_price_text);

                rightLayout.setVisibility(View.GONE);
                rightTTSText.setVisibility(View.GONE);
                rightPriceText.setVisibility(View.GONE);
            }
        }
        return mView;
    }

    private void loadImage(Context context, String url, ImageView imageView){
        Log.d("OrdersAdapter", "loadImage()");


    }

    private void getImage(String currentItem, RelativeLayout orderDesignLayout, ViewHolder holder, String url){
        View mView;
        switch (currentItem) {
            case "T-Shirts":
                mView = LayoutInflater.from(context).inflate(R.layout.pager_item_shirt, null);
                orderDesignLayout.addView(mView);
                break;
            case "Mugs":
                mView = LayoutInflater.from(context).inflate(R.layout.pager_item_mug, null);
                orderDesignLayout.addView(mView);
                break;
            case "Coasters":
                mView = LayoutInflater.from(context).inflate(R.layout.pager_item_coaster, null);
                orderDesignLayout.addView(mView);
                break;
            case "Plates":
                mView = LayoutInflater.from(context).inflate(R.layout.pager_item_plate, null);
                orderDesignLayout.addView(mView);
                break;
            case "Stickers":
                mView = LayoutInflater.from(context).inflate(R.layout.pager_item_sticker, null);
                orderDesignLayout.addView(mView);
                break;
            default:
                mView = LayoutInflater.from(context).inflate(R.layout.pager_item_airpods_skin, null);
                orderDesignLayout.addView(mView);
                break;
        }

        if(url!=null) {
            Log.d("OrdersAdapter", "url not null");
            holder.designView = orderDesignLayout.findViewById(R.id.selected_design_image);
            holder.setUrl(url);
//            orderImageView.inflate(context, layoutView, null);
//            Bitmap mBitmap = getBitmapFromView(orderImageView);
//            orderImageView.(mBitmap);

            Log.d("OrdersAdapter", "set bitmap");

//            GlideApp.with(context.getApplicationContext())
//                    .load(mBitmap)
//                    .dontAnimate()
//                    .into(orderImageView);
        }
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class ViewHolder {
        RelativeLayout leftLayout;
        RelativeLayout rightLayout;
        TextView leftTTSText;
        TextView rightTTSText;
        TextView leftPriceText;
        TextView rightPriceText;
        ImageView designView;
        String url;
        Context context;

        ViewHolder(Context context){
            this.context = context;
        }

        void setUrl(String url){
            this.url = url;
            GlideApp.with(context.getApplicationContext())
                    .load(url)
                    .into(designView);
        }
    }
}