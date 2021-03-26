package com.example.presell.activities;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.presell.R;
import com.example.presell.models.CustomAccount;
import com.example.presell.models.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.stripe.android.model.CardParams;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;
import com.stripe.android.view.CardMultilineWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SellerHubActivity extends AppCompatActivity {
    private RelativeLayout accountCreatedLayout, accountNotCreatedLayout;
    private Button sellerButton, linkBankButton, linkCardButton, depositButton;
    private TextView mBalanceText, mBankDepositText, mDebitDepositText;
    private RadioGroup mBankRadioGroup, mDebitRadioGroup;

    private ArrayList<String> mBankStrings, mDebitStrings;

    private ListView mSalesListView;
    private DatabaseReference mRef;

    private Login mLogin;
    private boolean noSignIn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_hub);

        ActionBar mActionBar = getSupportActionBar();
        if(mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setBackgroundDrawable(new ColorDrawable((ContextCompat.getColor(getApplicationContext(), R.color.darkBackground))));
        }

        mBankStrings = new ArrayList<String>();
        mDebitStrings = new ArrayList<String>();

        mLogin = new Login(getApplicationContext());
        noSignIn = mLogin.getNoSignIn();

        setupViews();
    }

    private void setupViews(){
        accountCreatedLayout = findViewById(R.id.account_created_layout);
        accountNotCreatedLayout = findViewById(R.id.account_not_created_layout);

        linkBankButton = findViewById(R.id.change_deposit_bank_button);
        linkCardButton = findViewById(R.id.change_deposit_debit_button);
        depositButton = findViewById(R.id.deposit_now_button);
        mBalanceText = findViewById(R.id.current_balance_text_view);
        mSalesListView = findViewById(R.id.sales_list_view);
        sellerButton = findViewById(R.id.create_seller_acct_button);

        mBankDepositText = findViewById(R.id.bank_deposit_methods_text);
        mDebitDepositText = findViewById(R.id.debit_deposit_methods_text);
        mBankRadioGroup = findViewById(R.id.bank_deposit_methods_radio_group);
        mDebitRadioGroup = findViewById(R.id.debit_deposit_methods_radio_group);

        if(noSignIn){
            linkBankButton.setEnabled(false);
            linkCardButton.setEnabled(false);
            depositButton.setEnabled(false);
        }
        else{
            checkVisibility();
        }
    }

    private void checkVisibility(){
        mRef = FirebaseDatabase.getInstance().getReference().child(mLogin.getUserId()).child("has_custom");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null && snapshot.getValue().equals("true")){
                    accountNotCreatedLayout.setVisibility(View.GONE);
                    accountCreatedLayout.setAlpha(1);
                    setupButtonClicks();
                    listDepositMethods();
                }
                else{
                    setupSignupButton();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getBaseContext(), "Unable to check for seller account, please exit and try again", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupSignupButton(){
        final Context context = this;
        sellerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialogLayout = inflater.inflate(R.layout.dialog_custom_account_setup, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(dialogLayout);
                final AlertDialog alert = builder.create();
                alert.show();

                dialogLayout.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.cancel();
                    }
                });

                dialogLayout.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        saveAccountInfo(dialogLayout, alert);
                        Toast.makeText(getBaseContext(), "Creating your account!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void listDepositMethods(){
        Log.d("SellerHubActivity", "listDepositMethods()");
        FirebaseFirestore.getInstance().
                collection("stripe_sellers")
                .document(mLogin.getUserId())
                .collection("bank_accounts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("SellerHubActivity", "onComplete()");

                        if(task.isSuccessful()){
                            Log.d("SellerHubActivity", "isSuccessful()");

                            QuerySnapshot bankAccountsId = task.getResult();
                            if(bankAccountsId!=null) {
                                for(DocumentSnapshot mBankAccount : bankAccountsId.getDocuments()) {
                                    Log.d("SellerHubActivity", "DocSnapshot() " + mBankAccount.getId());

                                    if(mBankDepositText.getVisibility()==View.GONE){
                                        mBankDepositText.setVisibility(View.VISIBLE);
                                    }

                                    String bankInfo = mBankAccount.get("bank_name") + ": " + mBankAccount.get("last4");
                                    RadioButton bankButton = new RadioButton(getBaseContext());
                                    bankButton.setText(bankInfo);
                                    mBankRadioGroup.addView(bankButton);
                                }
                            }
                        }
                    }
                });
        FirebaseFirestore.getInstance().
                collection("stripe_sellers")
                .document(mLogin.getUserId())
                .collection("debit_cards")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            QuerySnapshot debitCardsId = task.getResult();
                            if(debitCardsId!=null) {
                                for(DocumentSnapshot mDebitCard : debitCardsId.getDocuments()) {
                                    if(mDebitDepositText.getVisibility()==View.GONE){
                                        mDebitDepositText.setVisibility(View.VISIBLE);
                                    }

                                    String debitInfo = mDebitCard.get("brand") + ": " + mDebitCard.get("last4");
                                    RadioButton debitButton = new RadioButton(getBaseContext());
                                    debitButton.setText(debitInfo);
                                    mDebitRadioGroup.addView(debitButton);
                                }
                            }
                        }
                    }
                });
    }

    private void saveAccountInfo(View dialogLayout, AlertDialog alert){
        CustomAccount customAccount = new CustomAccount(this);

        TextInputEditText firstNameInput = dialogLayout.findViewById(R.id.account_holder_first_name_edit_text);
        TextInputEditText lastNameInput = dialogLayout.findViewById(R.id.account_holder_last_name_edit_text);
        TextInputEditText descriptionInput = dialogLayout.findViewById(R.id.description_edit_text);
        CheckBox confirmTCCheckbox = dialogLayout.findViewById(R.id.confirm_tc_checkbox);

        boolean error = false;

        if(firstNameInput.getText() !=null && firstNameInput.getText().length()>0){
            customAccount.setFirstName(firstNameInput.getText().toString());
        }
        else error = true;

        if(lastNameInput.getText() !=null && lastNameInput.getText().length()>0){
            customAccount.setLastName(lastNameInput.getText().toString());
        }
        else error = true;

        if(descriptionInput.getText() !=null && descriptionInput.getText().length()>0){
            customAccount.setProductDescription(descriptionInput.getText().toString());
        }
        else error = true;

        if(!confirmTCCheckbox.isChecked()) error=true;

        if(!error){
            if(customAccount.setTOSAcceptanceIP()){
                customAccount.setTOSAcceptanceDate();
                FirebaseFirestore.getInstance().collection("stripe_sellers")
                        .document(mLogin.getUserId())
                        .set(customAccount.getInfoMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateCustomAcctInDb();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getBaseContext(), "Something went wrong creating your account, please try again", Toast.LENGTH_LONG).show();
                    }
                });
            }
            else{
                error=true;
            }
        }

        if(error) {
            Toast.makeText(this, "Something went wrong creating your account, please try again", Toast.LENGTH_LONG).show();
        }
    }

    private void updateCustomAcctInDb(){
        FirebaseDatabase.getInstance().getReference().child(mLogin.getUserId()).child("has_custom").setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getBaseContext(), "All set, seller account has been created!", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(), "Something went wrong creating your account, please try again", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupButtonClicks(){
        linkBankButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBankInfo();
            }
        });

        linkCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDebitInfo();
            }
        });

        depositButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void getBankInfo(){
        LayoutInflater inflater = getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.dialog_add_bank, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        final AlertDialog alert = builder.create();
        alert.show();

        ArrayList<String> accountTypes = new ArrayList<String>();
        accountTypes.add("Individual/Sole Proprietorship");
//        accountTypes.add("Company, LLC, or Partnership");

        Spinner accountTypeSpinner = dialogLayout.findViewById(R.id.account_type_spinner);
        ArrayAdapter<String> accountTypeAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_orders,accountTypes);
        accountTypeSpinner.setAdapter(accountTypeAdapter);

        Button saveButton = dialogLayout.findViewById(R.id.save_button);
        Button cancelButton = dialogLayout.findViewById(R.id.cancel_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndAddBank(dialogLayout, alert);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.cancel();
            }
        });
    }

    private void getDebitInfo(){
        LayoutInflater inflater = getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.dialog_add_debit, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        final AlertDialog alert = builder.create();
        alert.show();

        Button cancelButton = dialogLayout.findViewById(R.id.cancel_button);
        Button saveButton = dialogLayout.findViewById(R.id.save_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.cancel();
            }
        });

        final Context context = this;
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: ENSURE ALL FIELDS FILLED OUT & VALID CARD
                CardMultilineWidget cardWidget = dialogLayout.findViewById(R.id.card_input_widget);
                PaymentMethodCreateParams.Card card = cardWidget.getPaymentMethodCard();
                PaymentMethod.BillingDetails billingDetails = new PaymentMethod.BillingDetails.Builder()
                        .setName(mLogin.getUserId())
                        .build();
                if(card!=null) {
                    final PaymentMethodCreateParams paymentMethodParams = PaymentMethodCreateParams.create(card, billingDetails);
                    FirebaseFirestore.getInstance()
                            .collection("stripe_sellers")
                            .document(mLogin.getUserId())
                            .collection("debit_cards")
                            .add(paymentMethodParams.toParamMap()).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(context, "Debit card added as deposit method!", Toast.LENGTH_LONG).show();
                            alert.cancel();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to add deposit method, please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void checkAndAddBank(View dialogLayout, final AlertDialog alert){
        try {
            String name = ((TextInputEditText)dialogLayout.findViewById(R.id.account_holder_name_edit_text)).getText().toString();
            int accountTypeSpinnerPos = ((Spinner)dialogLayout.findViewById(R.id.account_type_spinner)).getSelectedItemPosition();
            String accountType;
            if(accountTypeSpinnerPos==0) accountType = "individual";
            else accountType = "company";
            String routingNum = ((TextInputEditText)dialogLayout.findViewById(R.id.description_edit_text)).getText().toString();
            String accountNum = ((TextInputEditText)dialogLayout.findViewById(R.id.account_number_edit_text)).getText().toString();

            Map<String,Object> bankAccountMap = new HashMap<String, Object>();
            bankAccountMap.put("account_holder_name", name);
            bankAccountMap.put("account_holder_type", accountType);
            bankAccountMap.put("routing_number", routingNum);
            bankAccountMap.put("account_number", accountNum);

            FirebaseFirestore.getInstance().collection("stripe_sellers")
                    .document(mLogin.getUserId())
                    .collection("bank_accounts")
                    .add(bankAccountMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Toast.makeText(getBaseContext(), "Bank account added!", Toast.LENGTH_SHORT).show();
                    alert.cancel();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getBaseContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch(Exception e){
            Toast.makeText(this, "Please fill in every value before submitting", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
