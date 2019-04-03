package com.bradperkins.confinder.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.fragments.RegisterFragment;
import com.bradperkins.confinder.utils.DataHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity implements RegisterFragment.RegListener {

    public static final String TAG = "RegisterActivity.TAG";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        mAuth = FirebaseAuth.getInstance();
        boolean hasNetwork = DataHelper.hasNetwork(this);
        if (hasNetwork){
            getFragmentManager().beginTransaction().replace(R.id.reg_placeholder, RegisterFragment.newInstance()).commit();
        }else{
            Toast.makeText(this, "Please Connect to your Network", Toast.LENGTH_SHORT).show();
        }

    }

    private void registerUser(final String email, final String pass, final String username){
        final String userID = mAuth.getUid();

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(RegisterActivity.this, "Registration Successful.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();

                            //TODO do a check to verify username is available
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username).build();

                            user.updateProfile(profileUpdates);

                            String regName = profileUpdates.getDisplayName();

                            SharedPreferences sharedPref = getSharedPreferences("USER_DATA" , Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("USER_NAME", regName);
                            editor.putString("USER_EMAIL", email);
                            editor.putString("USER_PASSWORD", pass);
                            editor.putString("USER_ID", userID);
                            editor.commit();

                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(RegisterActivity.this, "Email already in use. Please use different email to Register.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    @Override
    public void register(String email, String pass, String username) {
        registerUser(email, pass, username);
    }


}




