package com.example.presell.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.presell.R;
import com.example.presell.models.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.model.Document;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentAuthConfig;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.SetupIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.Card;
import com.stripe.android.model.ConfirmSetupIntentParams;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.model.SetupIntent;
import com.stripe.android.model.ShippingInformation;
import com.stripe.android.model.Token;
import com.stripe.android.view.AddPaymentMethodActivityStarter;
import com.stripe.android.view.CardInputWidget;
import com.stripe.android.view.CardMultilineWidget;
import com.stripe.android.view.ShippingInfoWidget;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.nio.file.Paths.get;

public class SettingsActivity extends AppCompatActivity {
    private Login mLogin;
    private TextView paymentText, depositText, notSignedInText;
    private Button addPaymentButton, shippingAddressButton;
    private RadioGroup paymentGroup, shippingGroup;
    private Map<Integer,ShippingInformation> shippingMap;

    private Stripe stripe;
    private FirebaseFirestore fRef;

    private String preferredPaymentMethod;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable((ContextCompat.getColor(getApplicationContext(), R.color.darkBackground))));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mLogin = new Login(getApplicationContext());

        fRef = FirebaseFirestore.getInstance();
        fRef.collection("stripe_customers").document(mLogin.getUserId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot mDocument = task.getResult();
                if(mDocument != null && mDocument.get("default_payment_method") != null){
                    preferredPaymentMethod = Objects.requireNonNull(mDocument.get("default_payment_method")).toString();
                    Log.d("SettingsActivity", preferredPaymentMethod + " preferred method");
                }
                getPaymentMethods();
            }
        });

        String publishableKey = PaymentConfiguration.getInstance(this).getPublishableKey();
        stripe = new Stripe(this, publishableKey);
        shippingMap = new HashMap<Integer,ShippingInformation>();

        setupCurrencySpinner();
        updateFeatureAccess();
    }

    private void getShippingMethods(){
        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        mRef.child(mLogin.getUserId()).child("shipping_methods").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot allShippingSnapshot) {
                for(DataSnapshot mShippingMethod : allShippingSnapshot.getChildren()){
                    ShippingInformation mShippingInfo = mShippingMethod.getValue(ShippingInformation.class);
                    if(mShippingInfo != null) {
                        String radioString = mShippingInfo.getName() + "\n" +
                                Objects.requireNonNull(mShippingInfo.getAddress()).getLine1() + "\n" +
                                mShippingInfo.getAddress().getCity() + ", " +
                                mShippingInfo.getAddress().getState() + " " +
                                mShippingInfo.getAddress().getPostalCode();
                        RadioButton mShippingButton = new RadioButton(getBaseContext());
                        mShippingButton.setText(radioString);
                        shippingMap.put(mShippingButton.getId(), mShippingInfo);
                        shippingGroup.addView(mShippingButton);
                    }
                }
                mRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    
    private void updateFeatureAccess(){
        paymentText = findViewById(R.id.payment_text);
        depositText = findViewById(R.id.deposit_text);
        addPaymentButton = findViewById(R.id.change_payment_button);
        notSignedInText = findViewById(R.id.not_signed_in_text);

        paymentGroup = findViewById(R.id.payment_methods_radio_group);
        shippingGroup = findViewById(R.id.shipping_address_radio_group);

        if (mLogin.getNoSignIn()) {
            paymentText.setTextColor(Color.LTGRAY);
            depositText.setTextColor(Color.LTGRAY);

            addPaymentButton.setEnabled(false);
            notSignedInText.setVisibility(View.VISIBLE);
        }
        else{
            setupPaymentButtons();
            setupShippingAddressButton();
            getShippingMethods();
        }
    }

    private void setupPaymentButtons(){
        addPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentDialogHelper(R.layout.dialog_add_payment);
            }
        });
    }

    private void setupShippingAddressButton(){
        shippingAddressButton = findViewById(R.id.change_shipping_address_button);

        shippingAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialogHelper(R.layout.dialog_add_shipping);
            }
        });
    }

    private void createDialogHelper(int resId){
        LayoutInflater inflater = getLayoutInflater();
        final View dialogLayout = inflater.inflate(resId, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        final AlertDialog alert = builder.create();
        alert.show();

        final ShippingInfoWidget mShippingWidget = dialogLayout.findViewById(R.id.shipping_info_widget);
        mShippingWidget.setHiddenFields(
                Collections.singletonList(ShippingInfoWidget.CustomizableShippingField.Phone));

        dialogLayout.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.cancel();
            }
        });

        dialogLayout.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mShippingWidget.validateAllFields()){
                    ShippingInformation mShippingInfo = mShippingWidget.getShippingInformation();
                    if(mShippingInfo!=null) {
                        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
                        mRef.child(mLogin.getUserId())
                                .child("shipping_methods")
                                .push()
                                .setValue(mShippingInfo)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getBaseContext(), "All set, shipping method saved", Toast.LENGTH_SHORT).show();
                                        alert.cancel();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getBaseContext(), "There was a problem saving shipping info, please try again!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                else{
                    Toast.makeText(getBaseContext(), "Error adding shipping address, please ensure fields are filled in.", Toast.LENGTH_LONG).show();
                }
//                saveFinancialInfo(resId, dialogLayout);
            }
        });
    }

    private void paymentDialogHelper(final int resId){
        LayoutInflater inflater = getLayoutInflater();
        final View dialogLayout = inflater.inflate(resId, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                saveFinancialInfo(resId, dialogLayout);
                Toast.makeText(getBaseContext(), "Adding new card", Toast.LENGTH_LONG).show();
                alert.cancel();
            }
        });
    }

    private void saveFinancialInfo(int resId, View dialogLayout){
        switch(resId){
            case R.layout.dialog_add_payment:
                Log.d("SettingsActivitySettingsActivity", "saveFinancialInfo()");
                CardMultilineWidget mCardInput = dialogLayout.findViewById(R.id.card_input_widget);
                saveInfoHelper(mCardInput);
            case R.layout.dialog_add_bank:
            case R.layout.dialog_add_debit:
        }
    }

    private void saveInfoHelper(CardMultilineWidget mCardInput){
        Log.d("SettingsActivitySettingsActivity", "saveInfoHelper()");

        PaymentMethodCreateParams.Card card = mCardInput.getPaymentMethodCard();

        if (card != null) {
            Log.d("SettingsActivitySettingsActivity", "saveInfoHelper() card not null");

            PaymentMethod.BillingDetails billingDetails = new PaymentMethod.BillingDetails.Builder()
                    .setName(mLogin.getUserId())
                    .build();

            final PaymentMethodCreateParams paymentMethodParams = PaymentMethodCreateParams.create(card, billingDetails);
            Log.d("SettingsActivitySettingsActivity", "right before db");

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("stripe_customers")
                    .document(mLogin.getUserId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Log.d("SettingsActivitySettingsActivity", "successful db addition");

                            String setupIntentClientService = documentSnapshot.getString("setup_secret");
                            if(setupIntentClientService!=null) {
                                updateCreditHelper(paymentMethodParams, setupIntentClientService);
                            }
                        }
                    });
        }
    }

    private void updateCreditHelper(PaymentMethodCreateParams paymentMethodParams, String setupIntentClientService){
        ConfirmSetupIntentParams confirmParams = ConfirmSetupIntentParams.create(paymentMethodParams, setupIntentClientService);
        stripe.confirmSetupIntent(this, confirmParams);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("payment_methods").document(mLogin.getUserId()).collection("payment_methods").add(publishableKey);
    }

    private void setupCurrencySpinner(){
        Spinner mCurrencySpinner = findViewById(R.id.currency_spinner);
        ArrayList<String> currenciesList = getCurrencyOptions();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_orders, currenciesList);
        mCurrencySpinner.setAdapter(adapter);
    }

    private static ArrayList<String> getCurrencyOptions(){
        ArrayList<String> currencyOptions = new ArrayList<String>();

        currencyOptions.add("USD");
        currencyOptions.add("EUR");
        currencyOptions.add("JPY");
        currencyOptions.add("GBP");
        currencyOptions.add("CAD");

        return currencyOptions;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("SettingsActivitySettingsActivityTest", "onActivityResult1");

        // Handle the result of stripe.confirmSetupIntent
        stripe.onSetupResult(requestCode, data, new ApiResultCallback<SetupIntentResult>() {
            @Override
            public void onSuccess(@NotNull SetupIntentResult result) {
                Log.d("SettingsActivitySettingsActivityTest", "onSuccess ActivityResult");

                SetupIntent setupIntent = result.getIntent();
                SetupIntent.Status status = setupIntent.getStatus();
                if (status == SetupIntent.Status.Succeeded) {
                    Log.d("SettingsActivitySettingsActivityTest", "SetupIntent Id: " + setupIntent.getId() + " Payment method Id: " + setupIntent.getPaymentMethodId());
//                    final PaymentAuthConfig.Stripe3ds2UiCustomization uiCustomization =
//                            new PaymentAuthConfig.Stripe3ds2UiCustomization.Builder()
//                                    .setLabelCustomization(
//                                            new PaymentAuthConfig.Stripe3ds2LabelCustomization.Builder()
//                                                    .setTextFontSize(12)
//                                                    .build())
//                                    .build();
//                    PaymentAuthConfig.init(new PaymentAuthConfig.Builder()
//                            .set3ds2Config(new PaymentAuthConfig.Stripe3ds2Config.Builder()
//                                    .setTimeout(10)
//                                    .setUiCustomization(uiCustomization)
//                                    .build())
//                            .build());
                    updateFirebasePayment(setupIntent.getPaymentMethodId());
                }
                else if (status == SetupIntent.Status.RequiresPaymentMethod) {
                    // Setup failed – allow retrying using a different payment method
                    Log.d("SettingsActivitySettingsActivityTest", "RequiresPayment ActivityResult");
                    Toast.makeText(getBaseContext(), "Error updating payment method, please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(@NotNull Exception e) {
                // Setup request failed – allow retrying using the same payment method
                e.printStackTrace();
                Log.d("SettingsActivitySettingsActivityTest", "onError Activity Result");
                Toast.makeText(getBaseContext(), "Error updating payment method, please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void updateFirebasePayment(String paymentMethodId){
        FirebaseFirestore mFireStoreRef = FirebaseFirestore.getInstance();
        Map<String,String> paymentMethods = new HashMap<String, String>();
        paymentMethods.put("id",paymentMethodId);

//        mFireStoreRef.collection("stripe_customers").document(mLogin.getUserId()).set
        mFireStoreRef.collection("stripe_customers")
                .document(mLogin.getUserId())
                .collection("payment_methods")
                .add(paymentMethods)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getBaseContext(), "All set! Payment method added.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(), "Error adding payment method, please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //TODO: MAKE STATIC AND USE IN CONFIRM ORDER ACTIVITY
    private void getPaymentMethods(){
        fRef.collection("stripe_customers")
                .document(mLogin.getUserId())
                .collection("payment_methods")
                .addSnapshotListener(new EventListener<QuerySnapshot>(){
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error!=null) {
                            Toast.makeText(getBaseContext(), "Error loading payment methods.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(value != null) {
                            for (DocumentChange docChange : value.getDocumentChanges()) {
                                Map<String, Object> mPaymentInfo = docChange.getDocument().getData();
                                RadioButton mRadioButton = new RadioButton(getBaseContext());
                                try {
                                    Map<String, Object> cardDetails = (Map<String, Object>) mPaymentInfo.get("card");

                                    if (cardDetails != null) {
                                        String paymentMethodText = cardDetails.get("brand") + ": *" +
                                                cardDetails.get("last4") + ", " +
                                                cardDetails.get("exp_month") + "/" +
                                                (Objects.requireNonNull(cardDetails.get("exp_year")).toString()).substring(2, 4);
                                        paymentMethodText = paymentMethodText.substring(0, 1).toUpperCase() + paymentMethodText.substring(1);

                                        mRadioButton.setText(paymentMethodText);
                                        paymentGroup.addView(mRadioButton);
                                        clickListenerPaymentMethod(mRadioButton, (String)mPaymentInfo.get("id"));
                                    }
                                }
                                catch(Exception e){
                                    Log.e("SettingsActivity", "Failed to get card details");
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
    }

    private void clickListenerPaymentMethod(final RadioButton mRadioButton, final String cardId){
        mRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRadioButton.setChecked(true);
                Map<String, String> defaultPaymentId = new HashMap<String, String>();
                defaultPaymentId.put("default_payment_method", cardId);
                fRef.collection("stripe_customers").document(mLogin.getUserId()).set(defaultPaymentId, SetOptions.merge());
            }
        });

        if(preferredPaymentMethod!=null && preferredPaymentMethod.equals(cardId)) {
            Log.d("SettingsActivity", preferredPaymentMethod + " selected is true");
            mRadioButton.setChecked(true);
        }
    }
}
