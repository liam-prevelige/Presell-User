package com.example.presell.fragments;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.presell.R;
import com.example.presell.adapters.CategoryButtonAdapter;

import java.util.ArrayList;

public class SearchFragment extends Fragment {
    private ListView categoriesButtonList;
    private Context mContext;
    private ArrayList<String> categoriesList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("SearchFragment", "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        categoriesButtonList = requireActivity().findViewById(R.id.category_button_list_view);
        categoriesList = HomeFragment.instantiateCategories(new ArrayList<String>());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d("SearchFragment", "onViewCreated() " + getActivity());

        super.onViewCreated(view, savedInstanceState);

        ListView mListView = (ListView) requireActivity().findViewById(R.id.category_button_list_view);

        Log.d("SearchFragment", "Categories List Size: " + categoriesList.size());

        CategoryButtonAdapter mButtonAdapter = new CategoryButtonAdapter(mContext, R.layout.search_category_item, categoriesList);

        mListView.setAdapter(mButtonAdapter);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }
}
