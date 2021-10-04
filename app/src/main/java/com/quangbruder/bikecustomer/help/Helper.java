package com.quangbruder.bikecustomer.help;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;

import com.quangbruder.bikecustomer.data.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Helper {

    static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Map<String,String> createAuthParameters(String email, String password){
        Map<String, String> result = new HashMap<String, String>();
        result.put("email",email);
        result.put("password",password);
        return result;
    }

    public static void storeToken(Context context,String token){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public static String retrieveToken(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("token", "");
    }


    public static void storeUserInfo(Context context,User user){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Email",user.getEmail());
        editor.putString("Password",user.getPassword());
        editor.putString("Name",user.getName());
        editor.putString("CustomerId",user.getUserId());
        editor.apply();
    }

    public static User retrieveUserInfo(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String email = preferences.getString("Email", "");
        String password = preferences.getString("Password", "");
        String name = preferences.getString("Name", "");
        String id = preferences.getString("CustomerId", "");
        User user = new User(id,name,email,password);
        if(user.isAccepted()) {
            return user;
        }
        return null;
    }

    public static void removeUserInfo(Context context){
        SharedPreferences mySPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mySPrefs.edit();
        editor.remove("Email");
        editor.remove("Password");
        editor.remove("token");
        editor.apply();
    }

    public static void storeRentStatus(Context context, boolean isRenting){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isRenting",isRenting);
        editor.apply();
    }

    public static boolean retrieveRentStatus(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean loggedIn = preferences.getBoolean("isRenting", false);
        return loggedIn;
    }

    public static void storeRentBike(Context context, String bikeId){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("RentBike",bikeId);
        editor.apply();
    }

    public static String retrieveRentBike(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String bikeId = preferences.getString("RentBike", null);
        return bikeId;
    }

    public static void storeRentPin(Context context, String pin){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("RentPin",pin);
        editor.apply();
    }

    public static String retrieveRentPin(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String pin = preferences.getString("RentPin", null);
        return pin;
    }

    public static User createUserFromJSON(JSONObject jsonObject) throws JSONException {
        User result = new User();
        result.setName(jsonObject.getString("name"));
        result.setUserId(jsonObject.getString("customerId"));
        return result;
    }

    public static Bitmap rescaleBitMap(int id, Context context){
        BitmapDrawable bitmapdraw = (BitmapDrawable) context.getResources().getDrawable(id);
        return Bitmap.createScaledBitmap(bitmapdraw.getBitmap(),200,200,false);
    }


}
