package com.example.presell.adapters;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.presell.R;
import com.example.presell.activities.MainActivity;
import com.example.presell.fragments.HomeFragment;
import com.example.presell.fragments.MyProfileFragment;
import com.example.presell.fragments.SearchFragment;
import com.example.presell.models.GenAppInfo;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class CategoryButtonAdapter extends ArrayAdapter<String> {
    public static final String CATEGORY_KEY = "category key";
    private int mResource;
    private Context mContext;
    private GenAppInfo mInfo;

    public CategoryButtonAdapter(@NonNull Context context, int resource, ArrayList<String> categoriesList) {
        super(context, resource, categoriesList);
        mResource = resource;
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(mResource, null);
        }

        String category = getItem(position);

        if (category != null) {
            final Button categoryButton = v.findViewById(R.id.category_search_button);

            if (categoryButton != null){
                categoryButton.setText(category);
                categoryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AppCompatActivity activity = (AppCompatActivity) view.getContext();
                        mInfo = new GenAppInfo(view.getContext().getApplicationContext());
                        Log.d("CategoryButtonAdapter", categoryButton.getText().toString());
                        mInfo.setSelectedCategory(categoryButton.getText().toString());

                        Log.d("CategoryButtonAdapter", "sent intent");

//                        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.nav_view);
//                        bottomNavigationView.setSelectedItemId(R.id.nav_home);
                        
//                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                        ((MainActivity)activity).navigateToFeedFragment();
                        ((MainActivity)activity).changeCategory();


//                        Navigation.findNavController(activity, R.id.fragment_container).navigate(R.id.navigation_home);

//                        Fragment myFragment = new HomeFragment();

//                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, myFragment).addToBackStack(null).commit();
                    }
                });
            }
        }
        return v;
    }
}
