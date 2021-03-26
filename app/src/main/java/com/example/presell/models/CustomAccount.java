package com.example.presell.models;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.widget.Toast;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.WIFI_SERVICE;

public class CustomAccount {
    private Map<String,Object> accountInfoMap;
    private Context context;

    public CustomAccount(Context context){
        this.context = context.getApplicationContext();

        accountInfoMap = new HashMap<String,Object>();
        accountInfoMap.put("country", "US");
        accountInfoMap.put("requested_capabilities", "transfers");
        accountInfoMap.put("business_type", "individual");
    }

    public void setProductDescription(String productDescription){
        accountInfoMap.put("product_description", productDescription);
    }

    public void setTOSAcceptanceDate(){
        long unixTime = System.currentTimeMillis() / 1000L;
        accountInfoMap.put("date", unixTime);
    }

    public boolean setTOSAcceptanceIP(){
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        if(wm!=null) {
            WifiInfo wifiInfo = wm.getConnectionInfo();
            byte[] myIPAddress = BigInteger.valueOf(wifiInfo.getIpAddress()).toByteArray();
            reverse(myIPAddress);
            try {
                InetAddress myInetIP = InetAddress.getByAddress(myIPAddress);
                String myIP = myInetIP.getHostAddress();
                accountInfoMap.put("ip", myIP);
                return true;
            }
            catch(Exception e){
                Toast.makeText(context, "Failed to setup seller account, please try again", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                return false;
            }
        }
        else{
            return false;
        }
    }

    private static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    public void setFirstName(String firstName){
        accountInfoMap.put("first_name", firstName);
    }

    public void setLastName(String lastName){
        accountInfoMap.put("last_name", lastName);
    }

    public Map<String,Object> getInfoMap(){
        return accountInfoMap;
    }
}
