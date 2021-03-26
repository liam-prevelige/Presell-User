package com.example.presell.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.presell.R;
import com.example.presell.activities.MainActivity;
import com.example.presell.adapters.OrdersAdapter;
import com.example.presell.models.GenAppInfo;
import com.example.presell.models.Login;
import com.example.presell.models.Order;
import com.example.presell.models.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class OrdersFragment extends Fragment {
    private GenAppInfo mInfo;
    private Login mLogin;
    private ArrayList<Order> mOrdersList;
    private View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.fragment_orders, null);
        setHasOptionsMenu(true);

        mInfo = new GenAppInfo(requireContext().getApplicationContext());
        mLogin = new Login(requireContext().getApplicationContext());
        mOrdersList = new ArrayList<Order>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        this.view = view;

        Spinner filtersSpinner = view.findViewById(R.id.orders_filters);
        addFilters(filtersSpinner);
        setupPastCurrentClick(view);
//        addDefaultOrders(view);
        addUserOrders(view);
        getOrdersFromDb();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void setupPastCurrentClick(View view){
        final TextView pastText = view.findViewById(R.id.past_text);
        final TextView currentText = view.findViewById(R.id.current_text);

        if(mInfo.getSelectedOrderTimeline().equals("past")) {
            pastText.setBackgroundColor(Color.GREEN);
        }
        else{
            currentText.setBackgroundColor(Color.GREEN);
        }

        pastText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pastText.setBackgroundColor(Color.GREEN);
                currentText.setBackgroundColor(Color.WHITE);
                mInfo.setSelectedOrderTimeline("past");
            }
        });

        currentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pastText.setBackgroundColor(Color.WHITE);
                currentText.setBackgroundColor(Color.GREEN);
                mInfo.setSelectedOrderTimeline("current");
            }
        });
    }

    private void addFilters(Spinner spinner){
        ArrayList<String> sortByOptions = new ArrayList<String>();
        sortByOptions.add("Newest");
        sortByOptions.add("$ High-Low");
        sortByOptions.add("$ Low-High");
        sortByOptions.add("Oldest");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item_orders, sortByOptions);
        spinner.setAdapter(adapter);
    }

    private void addDefaultOrders(View view){
        ArrayList<Order> leftOrdersList = new ArrayList<Order>();
        ArrayList<Order> rightOrdersList = new ArrayList<Order>();

        Order defaultOrderOne = new Order("buyerId", "sellerId", ">2", "https://mcdn.wallpapersafari.com/medium/10/78/lIrx9i.jpg");
        Order defaultOrderTwo = new Order("buyerId", "sellerId", ">2", "https://www.tourofhonor.com/appimages/2019fl4.jpg");
        Order defaultOrderThree = new Order("buyerId", "sellerId", ">2", "https://mcdn.wallpapersafari.com/medium/10/78/lIrx9i.jpg");
        Order defaultOrderFour = new Order("buyerId", "sellerId", ">2", "https://www.tourofhonor.com/appimages/2019fl4.jpg");
        
        leftOrdersList.add(defaultOrderOne);
        rightOrdersList.add(defaultOrderTwo);
        leftOrdersList.add(defaultOrderThree);
        rightOrdersList.add(defaultOrderFour);

        leftOrdersList.add(defaultOrderOne);
        rightOrdersList.add(defaultOrderTwo);
        leftOrdersList.add(defaultOrderThree);
        rightOrdersList.add(defaultOrderFour);

//        OrdersAdapter mAdapter = new OrdersAdapter(getContext(), R.layout.order_row, leftOrdersList, rightOrdersList);

        ListView mListView = view.findViewById(R.id.orders_list_view);
//        mListView.setAdapter(mAdapter);
    }

    private void getOrdersFromDb(){
        Log.d("OrdersAdapter", "getOrdersFromDb()");

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        mRef.child(mLogin.getUserId()).child("orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot orderIndex : snapshot.getChildren()){
                    for(DataSnapshot order : orderIndex.getChildren()) {
                        Order mOrder = order.getValue(Order.class);
                        addToOrdersList(mOrder, false);
                    }
                }
                addToOrdersList(null, true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //TODO: REFACTOR TO LOAD MANY ORDERS IN INCREMENTS
    private void addToOrdersList(Order order, boolean lastOrder){
        Log.d("OrdersAdapter", "addToOrdersList()");

        if(lastOrder && mOrdersList.size()>0){
            ArrayList<Order> leftOrdersList = new ArrayList<Order>();
            ArrayList<Order> rightOrdersList = new ArrayList<Order>();
            for(int i = 0; i < mOrdersList.size(); i++){
                if(i%2==0) leftOrdersList.add(mOrdersList.get(i));
                else rightOrdersList.add(mOrdersList.get(i));
            }
            OrdersAdapter mAdapter = new OrdersAdapter(requireContext(), R.layout.order_row, leftOrdersList, rightOrdersList);
            ListView mListView = view.findViewById(R.id.orders_list_view);
            mListView.setAdapter(mAdapter);
            mOrdersList = new ArrayList<Order>();
        }
        else{
            mOrdersList.add(order);
        }
    }

    private void addUserOrders(View view){
        ArrayList<Order> leftOrdersList = new ArrayList<Order>();
        ArrayList<Order> rightOrdersList = new ArrayList<Order>();
    }
//
//    @Override
//    public void onRefresh() {
//        Toast.makeText(requireContext(), "Refreshing", Toast.LENGTH_LONG).show();
//    }
}
