package com.example.presell;

import com.stripe.android.PaymentConfiguration;
import android.app.Application;
import android.content.Context;
import android.util.Log;

public class Presell extends Application {
    private static Context appContext;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("PresellApplicationClass", "onCreate()");
        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51HEf0THvpbpmrUfivMR6L2A8WEMgLu30EvUlKgg1RnvrcAukgXxwgG1AcKINM1o8x5HM1OUTphsWY4PacPsyzG0C00OFo0kCAo"
        );

        Presell.appContext = getApplicationContext();
    }

    public static Context getAppContext(){
        return Presell.appContext;
    }
}
