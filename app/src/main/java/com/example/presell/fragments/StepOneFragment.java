package com.example.presell.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.presell.R;
import com.example.presell.activities.CreateActivity;
import com.example.presell.activities.MainActivity;
import com.example.presell.adapters.CategoryCheckBoxAdapter;
import com.example.presell.adapters.PostRecyclerAdapter;

import java.util.ArrayList;

public class StepOneFragment extends Fragment {
    private ArrayList<String> categoriesList;
    private View mView;
    private ListView mListView;
    private ArrayList<String> enabledCategoriesList;

    public StepOneFragment(ArrayList<String> categoriesList){
        this.categoriesList = categoriesList;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enabledCategoriesList = new ArrayList<String>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step_one, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        populateCheckBoxes();
        setupConfirmButton();
    }

    private void populateCheckBoxes(){
        mListView = mView.findViewById(R.id.choose_categories_list_view);
        CategoryCheckBoxAdapter mAdapter = new CategoryCheckBoxAdapter(getContext(), R.id.choose_categories_list_view, categoriesList);
        mListView.setAdapter(mAdapter);
    }

    public ArrayList<String> getSelectedCategoriesList(){
        return enabledCategoriesList;
    }

    private boolean anyChecks(){
        boolean checked = false;
        enabledCategoriesList.clear();
        for(int i = 0; i < categoriesList.size(); i++){
            if(((CheckBox)mListView.getChildAt(i)).isChecked()){
                enabledCategoriesList.add(((CheckBox) mListView.getChildAt(i)).getText().toString());
                checked = true;
            }
        }
        return checked;
    }

    private void setupConfirmButton(){
        Button confirmButton = mView.findViewById(R.id.confirm_categories_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(anyChecks()){
                    ((CreateActivity)requireActivity()).startFragment(new StepTwoFragment());
                }
                else{
                    Toast.makeText(getContext(), "Minimum of one category required.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
