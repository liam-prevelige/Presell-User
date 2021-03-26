package com.example.presell.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class GenAppInfo {
    private SharedPreferences mInfo;

    public GenAppInfo(Context applicationContext){
        mInfo = PreferenceManager.getDefaultSharedPreferences(applicationContext);
    }

    public String getSelectedCategory(){
        return mInfo.getString("selected category", "none");
    }

    public void setSelectedCategory(String category){
        mInfo.edit().putString("selected category", category).apply();
    }

    public String getSelectedOrderTimeline(){
        return mInfo.getString("selected order timeline", "past");
    }

    public void setSelectedOrderTimeline(String orderTimeline){
        mInfo.edit().putString("selected order timeline", orderTimeline).apply();
    }

    public boolean getPostUploaded(){
        return mInfo.getBoolean("post uploaded", false);
    }

    public void setPostUploaded(boolean postUploaded){
        mInfo.edit().putBoolean("post uploaded", postUploaded).apply();
    }
}
