package com.example.presell.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Post object - holds information for a post that the user is trying to upload to feed to get
 * votes on
 */
public class Post implements Parcelable {
    private String title;
    private String datetime;
    private String designURL;
    private String profilePicUrl;
    private ArrayList<String> selectedCategories;

    private ArrayList<String> pictures;

    private String id;
    private boolean deleted;
    private String name;
    private Bitmap coverBitmap;
    private String email;
    private String coverCategory;

    public Post(){
        deleted = false;    // By default deleted value is false, once true remove from Feed/MyActivity
    }

    public Post(String title, String description, String datetime){
        this.title = title;
        this.datetime = datetime;
        deleted = false;
    }

    protected Post(Parcel in) {
        title = in.readString();
        datetime = in.readString();
        designURL = in.readString();
        profilePicUrl = in.readString();
        selectedCategories = in.createStringArrayList();
        pictures = in.createStringArrayList();
        id = in.readString();
        deleted = in.readByte() != 0;
        name = in.readString();
        coverBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        email = in.readString();
        coverCategory = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public ArrayList<String> getPictures() {
        if(pictures!= null)
            return pictures;
        else
            return new ArrayList<>();
    }

    public String getDesignURL() {
        return designURL;
    }

    public void setDesignURL(String designURL){
        this.designURL = designURL;
    }

    public void setPictures(ArrayList<String> pictures) {
        this.pictures = pictures;
    }

    public void addPicture(String uri){
        if(pictures==null) pictures = new ArrayList<String>();
        pictures.add(uri);
    }

    public boolean getDeleted(){
        return deleted;
    }

    public String getId(){
        return id;
    }

    public void setId(String newId){
        id = newId;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setCoverCategory(String coverCategory) {
        this.coverCategory = coverCategory;
    }

    public String getCoverCategory() {
        return coverCategory;
    }

    public ArrayList<String> getSelectedCategories() {
        return selectedCategories;
    }

    public void setSelectedCategories(ArrayList<String> selectedCategories) {
        this.selectedCategories = selectedCategories;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public boolean getIsDeleted(){
        return deleted;
    }

    public void setIsDeleted(boolean deleted){
        this.deleted = deleted;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(datetime);
        parcel.writeString(designURL);
        parcel.writeString(profilePicUrl);
        parcel.writeStringList(selectedCategories);
        parcel.writeStringList(pictures);
        parcel.writeString(id);
        parcel.writeByte((byte) (deleted ? 1 : 0));
        parcel.writeString(name);
        parcel.writeParcelable(coverBitmap, i);
        parcel.writeString(email);
        parcel.writeString(coverCategory);
    }
}
