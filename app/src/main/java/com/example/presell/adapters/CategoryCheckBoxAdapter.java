package com.example.presell.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.presell.R;

import java.util.List;

public class CategoryCheckBoxAdapter extends ArrayAdapter<String> {
    private List<String> mCategoriesList;
    private Context mContext;

    public CategoryCheckBoxAdapter(@NonNull Context context, int resource, @NonNull List<String> categoriesList) {
        super(context, resource, categoriesList);
        mCategoriesList = categoriesList;
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(mCategoriesList.size() > 0){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.category_check_box, parent, false);
            CheckBox mCheckBox = convertView.findViewById(R.id.category_check_box);
            mCheckBox.setText(mCategoriesList.get(position));
        }
        return super.getView(position, convertView, parent);
    }

    @Override
    public int getCount() {
        return mCategoriesList.size();
    }
}
