package com.bradperkins.confinder.utils;

// Date 12/10/18
// 
// Bradley Perkins

// AID - 1809

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Patterns;

import com.bradperkins.confinder.activities.LoginActivity;
import com.bradperkins.confinder.objects.ConKey;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// PerkinsBradley_CE
public class DataHelper {

    private static ArrayList<ConKey> keyValueList;
    private static String pos;
    private static String title;
    private static ArrayList<String> list;


    public static void clearUserData(SharedPreferences sharedPref){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("USER_NAME", "");
        editor.putString("USER_EMAIL", "");
        editor.putString("USER_PASSWORD", "");
        editor.commit();
    }

    public static void saveUserData(SharedPreferences sharedPref, String username, String email, String password){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("USER_NAME", username);
        editor.putString("USER_EMAIL", email);
        editor.putString("USER_PASSWORD", password);
        editor.commit();
    }


    //Logging User Out
    public static void loggingOut(Context context){
        FirebaseAuth.getInstance().signOut();
        SharedPreferences sharedPrefs = context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        DataHelper.clearUserData(sharedPrefs);

        SharedPreferences.Editor userEditor = sharedPrefs.edit();
        userEditor.clear();
        userEditor.commit();

        SharedPreferences welcomPref = context.getSharedPreferences("WELCOME_BOOL" , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = welcomPref.edit();
        editor.putBoolean("WELCOME", false);

        SharedPreferences.Editor welcomeEditor = sharedPrefs.edit();
        welcomeEditor.clear();
        welcomeEditor.commit();

        Intent logIntent = new Intent(context, LoginActivity.class);
        logIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(logIntent);
    }

    //Logging out dialog
    public static void loggingOutDialog(final Context context){
        new AlertDialog.Builder(context)
                .setTitle("Log out?")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loggingOut(context);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    //Email validator
    public static boolean isValidEmail(String email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    //date formatter
    public static String dateFormatter(double numDate){
        String numDateString = String.valueOf(numDate);
        numDateString = numDateString.substring(0, numDateString.length() - 2);

        if (numDateString.length() == 5){
            numDateString = "0"+ numDateString;
            numDateString = numDateString.substring(0, 2) + "/" + numDateString.substring(2, numDateString.length());
            numDateString = numDateString.substring(0, 5) + "/" + numDateString.substring(5, numDateString.length());

            return numDateString;
        } else if (numDateString.length() == 6){
            numDateString = numDateString.substring(0, 2) + "/" + numDateString.substring(2, numDateString.length());
            numDateString = numDateString.substring(0, 5) + "/" + numDateString.substring(5, numDateString.length());
            return numDateString;
        }

        return numDateString;

    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    //Calculate Radial Distance
    public static double distance(double fromLat, double fromLon, double toLat, double toLon, String unit) {
        double theta = fromLon - toLon;
        double dist = Math.sin(deg2rad(fromLat)) * Math.sin(deg2rad(toLat)) + Math.cos(deg2rad(fromLat)) * Math.cos(deg2rad(toLat)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    //Network Checker
    public static boolean hasNetwork(Context context){
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        } else {
            connected = false;
        }

        return connected;
    }

    public static void getChild(final String titleDelete, final Context context){
        keyValueList = new ArrayList<>();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mRef = mFirebaseDatabase.getReference();

        //build the titleList for new cons
        mRef.child("conventions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot consSnapshot : dataSnapshot.getChildren()) {
                    String pos = consSnapshot.getKey();
                    String title = String.valueOf(consSnapshot.child("title").getValue());
                    keyValueList.add(new ConKey(title, pos));
                }

                for (int i=0; i<keyValueList.size(); i++){
                    if (titleDelete.equals(keyValueList.get(i).getTitle())){
                        pos = keyValueList.get(i).getPos();
                        title = keyValueList.get(i).getTitle();
                        SharedPreferences deletePref = context.getSharedPreferences("DELETE_DATA" ,context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = deletePref.edit();
                        editor.putString("DELETE_KEY", pos);
                        editor.putString("DELETE_TITLE", title);
                        editor.commit();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static ArrayList<String> childList(final Context context){
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mRef = mFirebaseDatabase.getReference();
        list = new ArrayList<>();
        mRef.child("conventions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot consSnapshot : dataSnapshot.getChildren()) {
                    String pos = consSnapshot.getKey();
                    String title = String.valueOf(consSnapshot.child("title").getValue());
                    list.add(pos);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }

        });

        return list;

    }

    //Get LatLng from Address
    public static LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;
        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            if (address.get(0) == null){
                return null;
            }
            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return p1;
    }

    public static String chooseImage(Context context) {


        return null;
    }

    public static String timeStamp(){
        return new SimpleDateFormat("HHmmss").format(new Date());
    }
    public static String dateStamp(){
        return new SimpleDateFormat("MMddyyHHmmss").format(new Date());
    }

    public static String datePosStamp(){
        return new SimpleDateFormat("MMddyyHH").format(new Date());
    }

    public static String dateNotifyStamp(){
        return new SimpleDateFormat("MM/dd/yy  HH:mm aa").format(new Date());
    }

    public static int nextDay(){
        int month = Integer.parseInt(new SimpleDateFormat("MM").format(new Date()));
        int day = Integer.parseInt(new SimpleDateFormat("dd").format(new Date())) + 1;
        int year = Integer.parseInt(new SimpleDateFormat("yy").format(new Date()));

        String dayStr;
        String monthStr = String.valueOf(month);

        if (day < 10){
            dayStr = "0" + String.valueOf(day);
        } else {
            dayStr = String.valueOf(day);
        }

        String yearStr1 = String.valueOf(year);
        return Integer.parseInt(monthStr + dayStr + yearStr1);
    }

    public static String stateConvertor(String state){
        String stateAbrev = state;

        switch (state){
            case "alabama":
                stateAbrev = "AL";

                break;
            case "alaska":
                stateAbrev = "AK";
                break;
            case "arizona":
                stateAbrev = "AZ";
                break;
            case "arkansas":
                stateAbrev = "AR";
                break;
            case "california":
                stateAbrev = "CA";
                break;
            case "colorado":
                stateAbrev = "CO";
                break;
            case "connecticut":
                stateAbrev = "CT";
                break;
            case "delaware":
                stateAbrev = "DE";
                break;
            case "florida":
                stateAbrev = "FL";
                break;
            case "georgia":
                stateAbrev = "GA";
                break;
            case "hawaii":
                stateAbrev = "HI";
                break;
            case "idaho":
                stateAbrev = "ID";
                break;
            case "illinois":
                stateAbrev = "OL";
                break;
            case "indiana":
                stateAbrev = "IN";
                break;
            case "iowa":
                stateAbrev = "IA";
                break;
            case "kansas":
                stateAbrev = "KS";
                break;
            case "kentucky":
                stateAbrev = "KY";
                break;
            case "louisiana":
                stateAbrev = "LA";
                break;
            case "maine":
                stateAbrev = "ME";
                break;
            case "maryland":
                stateAbrev = "MD";
                break;
            case "massachusetts":
                stateAbrev = "MA";
                break;
            case "michigan":
                stateAbrev = "MI";
                break;
            case "minnesota":
                stateAbrev = "MN";
                break;
            case "mississippi":
                stateAbrev = "MS";
                break;
            case "missouri":
                stateAbrev = "MO";
                break;
            case "montana":
                stateAbrev = "MT";
                break;
            case "nebraska":
                stateAbrev = "NE";
                break;
            case "nevada":
                stateAbrev = "NV";
                break;
            case "new hampshire":
                stateAbrev = "NH";
                break;
            case "new jersey":
                stateAbrev = "NJ";
                break;
            case "new mexico":
                stateAbrev = "NM";
                break;
            case "new york":
                stateAbrev = "NY";
                break;
            case "north carolina":
                stateAbrev = "NC";
                break;
            case "north dakota":
                stateAbrev = "ND";
                break;
            case "ohio":
                stateAbrev = "OH";
                break;
            case "oklahoma":
                stateAbrev = "OK";
                break;
            case "oregon":
                stateAbrev = "OR";
                break;
            case "pennsylvania":
                stateAbrev = "PA";
                break;
            case "rhode island":
                stateAbrev = "RI";
                break;
            case "south carolina":
                stateAbrev = "SC";
                break;
            case "south dakota":
                stateAbrev = "SD";
                break;
            case "tennessee":
                stateAbrev = "TN";
                break;
            case "texas":
                stateAbrev = "TX";
                break;
            case "utah":
                stateAbrev = "UT";
                break;
            case "vermont":
                stateAbrev = "VT";
                break;
            case "virginia":
                stateAbrev = "VA";
                break;
            case "washington":
                stateAbrev = "WA";
                break;
            case "west virginia":
                stateAbrev = "WV";
                break;
            case "wisconsin":
                stateAbrev = "WI";
                break;
            case "wyoming":
                stateAbrev = "WY";
                break;
        }

        return stateAbrev.toLowerCase();
    }


    /** Returns the consumer friendly device name */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }



}
