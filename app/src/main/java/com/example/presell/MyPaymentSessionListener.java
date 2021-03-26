package com.example.presell;

import androidx.annotation.NonNull;

import com.stripe.android.PaymentSession;
import com.stripe.android.PaymentSessionData;
import com.stripe.android.model.PaymentMethod;

import org.jetbrains.annotations.NotNull;

public class MyPaymentSessionListener implements PaymentSession.PaymentSessionListener {
    // Called whenever the PaymentSession's data changes,
    // e.g. when the user selects a new `PaymentMethod` or enters shipping info.
    @Override
    public void onPaymentSessionDataChanged(@NonNull PaymentSessionData data) {
        if (data.getUseGooglePay()) {
            // customer intends to pay with Google Pay
        } else {
            final PaymentMethod paymentMethod = data.getPaymentMethod();
            if (paymentMethod != null) {
                // Display information about the selected payment method
            }
        }

        // Update your UI here with other data
        if (data.isPaymentReadyToCharge()) {
            // Use the data to complete your charge - see below.
        }
    }

    @Override
    public void onCommunicatingStateChanged(boolean isCommunicating) {
        if (isCommunicating) {
            // update UI to indicate that network communication is in progress
        } else {
            // update UI to indicate that network communication has completed
        }
    }

    @Override
    public void onError(int errorCode, @NotNull String errorMessage) {
    }
}