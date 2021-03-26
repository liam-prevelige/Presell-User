package com.example.presell.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.presell.R;
import com.example.presell.models.Login;
import com.example.presell.models.Order;
import com.example.presell.models.Post;
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
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentSession;
import com.stripe.android.PaymentSessionConfig;
import com.stripe.android.SetupIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.BankAccount;
import com.stripe.android.model.CardParams;
import com.stripe.android.model.ConfirmSetupIntentParams;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.model.SetupIntent;
import com.stripe.android.model.ShippingInformation;
import com.stripe.android.view.CardMultilineWidget;
import com.stripe.android.view.ShippingInfoWidget;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConfirmOrderActivity extends AppCompatActivity {
    private Login mLogin;
    private FirebaseFirestore fRef;
    private Post mPost;
    private Order mOrder;

    private RadioGroup paymentGroup, shippingGroup;
    private Button shippingAddressButton, confirmOrderButton, addPaymentButton;
    private TextView mItemTitleText, mItemTypeText, mEstTimeRemainingText, mQuantityText, mTotalPriceText;

    private int checkedPaymentId;
    private int checkedShippingId;

    private Map<String,String> paymentMap;
    private Map<Integer,ShippingInformation> shippingMap;
    private boolean firstPayment;
    private Stripe stripe;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable((ContextCompat.getColor(getApplicationContext(), R.color.darkBackground))));

        mLogin = new Login(getApplicationContext());
        paymentMap = new HashMap<String,String>();
        firstPayment = true;

        paymentGroup = findViewById(R.id.payment_methods_radio_group);
        shippingGroup = findViewById(R.id.shipping_address_radio_group);
        fRef = FirebaseFirestore.getInstance();
        confirmOrderButton = findViewById(R.id.confirm_order_button);
        mItemTitleText = findViewById(R.id.item_title_text);
        mItemTypeText = findViewById(R.id.item_type_text);
        mEstTimeRemainingText = findViewById(R.id.time_estimate_text);
        mQuantityText = findViewById(R.id.quantity_text);
        mTotalPriceText = findViewById(R.id.price_total_text);

        String publishableKey = PaymentConfiguration.getInstance(this).getPublishableKey();
        stripe = new Stripe(this, publishableKey);
        shippingMap = new HashMap<Integer,ShippingInformation>();

        if((mPost = getIntent().getParcelableExtra(BuyActivity.POST_KEY))!=null){
            mOrder = getIntent().getParcelableExtra(BuyActivity.ORDER_KEY);
            setupShippingAddressButton();
            getPaymentMethods();
            getShippingMethods();
            getPostInformation();
            setupConfirmOrderButton();
            setupPaymentButtons();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
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

    private void setupPaymentButtons(){
        addPaymentButton = findViewById(R.id.change_payment_button);

        addPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentDialogHelper(R.layout.dialog_add_payment);
            }
        });
    }

    //TODO: CHECK FOR COMPLETION OF ALL FIELDS
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
//                                    CardParams cardDetails = (CardParams)mPaymentInfo.get("card");

                                    if (cardDetails != null) {
                                        String paymentMethodText = cardDetails.get("brand") + ": *" +
                                                cardDetails.get("last4") + ", " +
                                                cardDetails.get("exp_month") + "/" +
                                                (Objects.requireNonNull(cardDetails.get("exp_year")).toString()).substring(2, 4);
                                        paymentMethodText = paymentMethodText.substring(0, 1).toUpperCase() + paymentMethodText.substring(1);

                                        mRadioButton.setText(paymentMethodText);
                                        paymentMap.put(paymentMethodText, (String)mPaymentInfo.get("id"));
                                        paymentGroup.addView(mRadioButton);
//                                        clickListenerPaymentMethod(mRadioButton, (String)mPaymentInfo.get("id"));
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

//    private void clickListenerPaymentMethod(final RadioButton mRadioButton, final String cardId){
//        mRadioButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mRadioButton.setChecked(true);
//                checkedCardId = cardId;
//                mRadioButton.setChecked(true);
//            }
//        });
//    }

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
                        addShippingMethodToDb(mShippingInfo);
                    }
                }
                else{
                    Toast.makeText(getBaseContext(), "Error adding shipping address, please ensure fields are filled in.", Toast.LENGTH_LONG).show();
                }
//                saveFinancialInfo(resId, dialogLayout);
            }
        });
    }

    private void addShippingMethodToDb(ShippingInformation mShippingInfo){
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        mRef.child(mLogin.getUserId())
                .child("shipping_methods")
                .push()
                .setValue(mShippingInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getBaseContext(), "All set, shipping method saved", Toast.LENGTH_SHORT).show();
//                        alert.cancel();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(), "There was a problem saving shipping info, please try again!", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void setupConfirmOrderButton(){
        confirmOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(filledOut()){
                    createDialog();
                }
                else{
                    Toast.makeText(getBaseContext(), "Please ensure you have selected a shipping address and payment method before placing order.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void createDialog(){
        new AlertDialog.Builder(this).setTitle("All set?")
                .setMessage("Click 'yes' to confirm all product, shipping, and payment info is correct. Your order will handled immediately for your convenience.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d("ConfirmOrderActivity", "startPaymentProcessing()");
                        startPaymentProcessing();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        }).show();
    }

    private boolean filledOut(){
        checkedPaymentId = paymentGroup.getCheckedRadioButtonId();
        checkedShippingId = shippingGroup.getCheckedRadioButtonId();

        return checkedPaymentId != RadioGroup.NO_ID && checkedShippingId != RadioGroup.NO_ID;
    }

    private void startPaymentProcessing(){
        Toast.makeText(this, "Congratulations! Working on your order now.", Toast.LENGTH_SHORT).show();
        Map<String,String> orderMap = new HashMap<String,String>();
        orderMap.put("currency", "usd");

        String paymentMethodId = null;

        for(int i = 0; i < paymentGroup.getChildCount(); i++){
            RadioButton v = (RadioButton)paymentGroup.getChildAt(i);
            if(v.getId() == paymentGroup.getCheckedRadioButtonId()){
                paymentMethodId = paymentMap.get(v.getText());
            }
        }
        //TODO: FIX HOW I GET ITEMTYPE
        if(paymentMethodId!=null){
            orderMap.put("payment_method", paymentMethodId);
            getItemPrice(orderMap, "Mug", mOrder.getSeekBarProgress());
        }
    }

    private void getPostInformation(){
        mItemTitleText.setText(mPost.getTitle());
        mItemTypeText.setText("Custom " + mOrder.getItemType());
        mQuantityText.setText("Quantity: " + mOrder.getQuantity());
        mTotalPriceText.setText("Total: " + mOrder.getItemPrice());
    }

    private void getItemPrice(final Map<String,String> orderMap, String itemType, String seekBarProgress){
        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

        mRef.child("item_prices").child(itemType).child(seekBarProgress + "")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null) {
                    String price = (String)snapshot.getValue();
                    orderMap.put("amount", price);
                    mOrder.setItemPrice(price);
                    sendOrderToFirestore(orderMap);
                    mRef.removeEventListener(this);

                    //TODO: FINISH onDataChange()
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getBaseContext(), "Error finding item price, please exit and try again", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendOrderToFirestore(Map<String,String> orderMap){
        if(firstPayment) {
            String paymentsCollection = "payments_hold";
            if(mOrder.getIsInstantOrder()) paymentsCollection = "payments";

            FirebaseFirestore mStore = FirebaseFirestore.getInstance();
            mStore.collection("stripe_customers")
                    .document(mLogin.getUserId())
                    .collection(paymentsCollection)
                    .add(orderMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    Toast.makeText(getBaseContext(), "All set, order placed!", Toast.LENGTH_LONG).show();
                    uploadOrderToDb();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getBaseContext(), "Oops, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                }
            });
            firstPayment = false;
        }
    }

    private void uploadOrderToDb(){
        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child(mLogin.getUserId()).child("orders");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long id = snapshot.getChildrenCount();
                mRef.child("-" + id).child(mOrder.getSellerId()).setValue(mOrder);
                mRef.removeEventListener(this);

                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getBaseContext(), "Oops, something went wrong. Your order is being processed but your purchase history has not been updated.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
