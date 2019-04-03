package com.bradperkins.confinder.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.fragments.LoginFragment;
import com.bradperkins.confinder.fragments.SpinnerFragment;
import com.bradperkins.confinder.fragments.SplashScreenFragment;
import com.bradperkins.confinder.utils.DataHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements LoginFragment.LoginListener{
    private boolean hasNetwork;
    Context context;

    private static final String TAG = "LoginActivity.TAG";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;

        hasNetwork = DataHelper.hasNetwork(this);

        //Load saved user data is exists
        SharedPreferences sharedPref = getSharedPreferences("USER_DATA" ,this.MODE_PRIVATE);
        String username = sharedPref.getString("USER_NAME", "");
        String userEmail = sharedPref.getString("USER_EMAIL", "");
        String userPass = sharedPref.getString("USER_PASSWORD", "");

        Log.d(TAG, username + " - " + userEmail + " - " + " - " + userPass);

        if (!hasNetwork){
            if (username != null && userEmail != null && userPass != null){
                Intent mainIntent = new Intent(this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
            }
        }

        mAuth = FirebaseAuth.getInstance();
        getFragmentManager().beginTransaction().replace(R.id.login_placeholder, LoginFragment.newInstance()).commit();

        if (!username.isEmpty() && !userEmail.isEmpty() && !userPass.isEmpty()){
            login(userEmail, userPass);
        }
    }

    @Override
    public void login(final String email, final String password) {
        getFragmentManager().beginTransaction().add(R.id.login_placeholder, SplashScreenFragment.newInstance()).commit();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //Set Shared prefs for possible admin Login
        SharedPreferences sharedPref = getSharedPreferences("ADMIN_DATA" , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (email.equals("admin@gmail.com") || email.equals("bperkins99@gmail.com")){
            editor.putBoolean("ADMIN_BOOL", true);
            editor.commit();
        }else {
            editor.putBoolean("ADMIN_BOOL", false);
            editor.commit();
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            //Save userdata for fast login
                            SharedPreferences sharedPref = getSharedPreferences("USER_DATA" , Context.MODE_PRIVATE);
                            DataHelper.saveUserData(sharedPref, currentUser.getDisplayName(), email, password);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            SpinnerFragment fragment = new SpinnerFragment();
                            if (hasNetwork){
                                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Login Failed");
                                builder.setMessage("Wrong Username or password. " +
                                        "Try again or tap Forgot password to reset it. " +
                                        "Or Tap on Register to create a new Account");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                                        getFragmentManager().beginTransaction().replace(R.id.login_placeholder, LoginFragment.newInstance()).commit();

                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else{
                                Toast.makeText(context, "Please check your network connection", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                });

    }

    @Override
    public void register() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    @Override
    public void forgot() {
        final EditText forgotEmailET;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Email to Reset Password");
        forgotEmailET = new EditText(this);
        builder.setView(forgotEmailET);
        builder.setPositiveButton("Reset Password", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String email = forgotEmailET.getText().toString().trim();
                boolean validEmail = DataHelper.isValidEmail(email);
                if (validEmail){
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Check Email to Reset", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(LoginActivity.this, "Enter a valid Email", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog forgotDialog = builder.create();
        forgotDialog.show();

    }


}
