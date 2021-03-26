package com.example.presell.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DatabaseReference;

public class Order implements Parcelable {
    private String buyerId;
    private String sellerId;
    private String designUrl;
    private String salesNeeded;

    private String itemType;
    private String itemTimeRemaining;
    private String quantity;
    private String itemPrice;
    private String seekBarProgress;

    private boolean isInstantOrder;

    private DatabaseReference mRef;

    public Order(){
        // Empty constructor, set variables after initialization
    }

    public Order(String buyerId, String sellerId, String designUrl){
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.designUrl = designUrl;
    }

    public Order(String buyerId, String sellerId, String salesNeeded, String designUrl){
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.salesNeeded = salesNeeded;
        this.designUrl = designUrl;
    }

    protected Order(Parcel in) {
        buyerId = in.readString();
        sellerId = in.readString();
        designUrl = in.readString();
        salesNeeded = in.readString();
        itemType = in.readString();
        itemTimeRemaining = in.readString();
        quantity = in.readString();
        itemPrice = in.readString();
        seekBarProgress = in.readString();
        isInstantOrder = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(buyerId);
        dest.writeString(sellerId);
        dest.writeString(designUrl);
        dest.writeString(salesNeeded);
        dest.writeString(itemType);
        dest.writeString(itemTimeRemaining);
        dest.writeString(quantity);
        dest.writeString(itemPrice);
        dest.writeString(seekBarProgress);
        dest.writeByte((byte) (isInstantOrder ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    public void setSalesNeeded(String newSalesNeeded){
        salesNeeded = newSalesNeeded;
    }

    public String getSalesNeeded(){
        return salesNeeded;
    }

    public String getDesignUrl(){
        return designUrl;
    }

    public void setDesignUrl(String newCoverImageURL){
        designUrl = newCoverImageURL;
    }

    public String getItemType(){
        return itemType;
    }

    public void setItemType(String itemType){
        this.itemType = itemType;
    }

    public String getItemTimeRemaining(){
        return itemTimeRemaining;
    }

    public void setItemTimeRemaining(String itemTimeRemaining){
        this.itemTimeRemaining = itemTimeRemaining;
    }

    public String getQuantity(){
        return quantity;
    }

    public void setQuantity(String quantity){
        this.quantity = quantity;
    }

    public void setItemPrice(String itemPrice){
        this.itemPrice = itemPrice;
    }

    public String getItemPrice(){
        return itemPrice;
    }

    public String getSeekBarProgress(){
        return seekBarProgress;
    }

    public void setSeekBarProgress(String seekBarProgress){
        this.seekBarProgress = seekBarProgress;
    }

    public boolean getIsInstantOrder(){
        return isInstantOrder;
    }

    public void setIsInstantOrder(boolean isInstantOrder){
        this.isInstantOrder = isInstantOrder;
    }

    public void setSellerId(String sellerId){
        this.sellerId = sellerId;
    }

    public String getSellerId(){
        return sellerId;
    }

    public void setBuyerId(String buyerId){
        this.buyerId = buyerId;
    }

    public String getBuyerId(){
        return buyerId;
    }
}
