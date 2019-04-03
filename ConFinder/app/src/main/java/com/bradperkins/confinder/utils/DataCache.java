package com.bradperkins.confinder.utils;

// Date 12/11/18
// 
// Bradley Perkins

// AID - 1809

import android.content.Context;

import com.bradperkins.confinder.objects.Con;
import com.google.firebase.auth.FirebaseAuth;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

// PerkinsBradley_CE
public class DataCache {


    //Main Convention List
    private static final String FILE_NAME = "cons.dat";
    //Save out con
    public static void saveConData(Context context, ArrayList<Con> conList) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(conList);
            oos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Con> loadConData(Context context) {
        ArrayList<Con> cons = null;
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            cons = (ArrayList<Con>)ois.readObject();
            ois.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        if(cons == null) {
            cons = new ArrayList<>();
        }
        return cons;
    }


    //Master List for sorting list purposes
    private static final String FILE_MASTER = "masterlist.dat";
    //Save out con
    public static void saveMasterList(Context context, ArrayList<Con> conList) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_MASTER, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(conList);
            oos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Con> loadMasterList(Context context) {
        ArrayList<Con> cons = null;
        try {
            FileInputStream fis = context.openFileInput(FILE_MASTER);
            ObjectInputStream ois = new ObjectInputStream(fis);
            cons = (ArrayList<Con>)ois.readObject();
            ois.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        if(cons == null) {
            cons = new ArrayList<>();
        }
        return cons;
    }

    //Favorites list
    private static final String FILE_FAV = "favs.dat";
    //Save out con
    public static void saveFavConData(Context context, ArrayList<Con> conList) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userID = mAuth.getUid();

        try {
            FileOutputStream fos = context.openFileOutput(userID + FILE_FAV, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(conList);
            oos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    public static ArrayList<Con> loadFavConData(Context context) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userID = mAuth.getUid();

        ArrayList<Con> cons = null;

        try {
            FileInputStream fis = context.openFileInput(userID + FILE_FAV);
            ObjectInputStream ois = new ObjectInputStream(fis);
            cons = (ArrayList<Con>)ois.readObject();
            ois.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(cons == null) {
            cons = new ArrayList<>();
        }

        return cons;
    }

    //Save out con
    public static void saveUserConData(Context context, ArrayList<String> conList, String FILE) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userID = mAuth.getUid();
        try {
            FileOutputStream fos = context.openFileOutput(userID + FILE, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(conList);
            oos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> loadUserConData(Context context, String FILE) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userID = mAuth.getUid();
        ArrayList<String> likes = null;
        try {
            FileInputStream fis = context.openFileInput(userID + FILE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            likes = (ArrayList<String>)ois.readObject();
            ois.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(likes == null) {
            likes = new ArrayList<>();
        }
        return likes;
    }

    //History Convention List
    private static final String FILE_HISTORY = "history.dat";
    //Save out con
    public static void saveHistoryData(Context context, ArrayList<Con> conList) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_HISTORY, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(conList);
            oos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Con> loadHistoryData(Context context) {
        ArrayList<Con> cons = null;
        try {
            FileInputStream fis = context.openFileInput(FILE_HISTORY);
            ObjectInputStream ois = new ObjectInputStream(fis);
            cons = (ArrayList<Con>)ois.readObject();
            ois.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        if(cons == null) {
            cons = new ArrayList<>();
        }
        return cons;
    }



}
