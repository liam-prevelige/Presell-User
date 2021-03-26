//package com.example.presell;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import com.example.presell.models.Login;
//import com.stripe.Stripe;
//import com.stripe.exception.StripeException;
//import com.stripe.model.Customer;
//import com.stripe.model.SetupIntent;
//
//public class StripeJavaQuickStart {
//    public static void main(String[] args) {
//        Login mLogin = new Login(Presell.getAppContext());
//        Customer customer = mLogin.getCustomer();
//
//        Stripe.apiKey = "sk_test_51HEf0THvpbpmrUfitRVs4ADV4pbuYOPJR8KSq7Si4SP2rJjpXC69TMOauXrLSTT5yZSqAEbO7VPadnKgM62ezMim00NvTu8Jle";
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("customer", customer.getId());
//        try {
//            SetupIntent setupIntent = SetupIntent.create(params);
//            String clientSecret = setupIntent.getClientSecret();
//        }
//        catch(StripeException e){
//            e.printStackTrace();
//        }
//    }
//}