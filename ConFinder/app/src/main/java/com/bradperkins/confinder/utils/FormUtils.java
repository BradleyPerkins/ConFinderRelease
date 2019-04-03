package com.bradperkins.confinder.utils;

// Date 1/9/19
// 
// Bradley Perkins

// AID - 1809

import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// PerkinsBradley_CE
public class FormUtils {

    //Zip Checker
    public static boolean zipCheck(String zip){
        String regex = "^[0-9]{5}(?:-[0-9]{4})?$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(zip);
        return matcher.matches();
    }

    //Email validator
    public static boolean isValidEmail(String email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    //Validate Password
    public static boolean isValidPassword(final String password) {
        String str = password;
        int length = str.length();
        if (length>6){
            return true;
        } else{
            return false;
        }
//
//
//
//        Pattern pattern;
//        Matcher matcher;
//        final String PASSWORD_PATTERN = "(?=.*[a-z])(?=\\S+$).{7,24}";
//        pattern = Pattern.compile(PASSWORD_PATTERN);
//        matcher = pattern.matcher(password);
//        return matcher.matches();
    }

    //Password Match
    public static boolean passwordCheck(String pw1, String pw2) {
        return (pw1.equals(pw2));
    }

    public static boolean isValidUsername(String username) {

        String str = username;
        int length = str.length();
        if (length>6){
            return true;
        } else{
            return false;
        }
//        Pattern pattern;
//        Matcher matcher;
//        final String PATTERN = "(?=.*[a-z]).{7,24}";
//        pattern = Pattern.compile(PATTERN);
//        matcher = pattern.matcher(username);
//        return matcher.matches();
    }
}
