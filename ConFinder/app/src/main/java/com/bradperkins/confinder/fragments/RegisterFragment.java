package com.bradperkins.confinder.fragments;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.utils.DataHelper;
import com.bradperkins.confinder.utils.FormUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {

    private RegListener mListener;

    private boolean validEmail;
    private boolean validPass1;
    private boolean validPass2;
    private boolean validName;

    private EditText regEmail;
    private EditText regPass1;
    private EditText regPass2;
    private EditText regUsername;

    public RegisterFragment() {
        // Required empty public constructor
    }

    public static RegisterFragment newInstance() {
        Bundle args = new Bundle();
        RegisterFragment fragment = new RegisterFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        regEmail = getView().findViewById(R.id.reg_email_et);
        regUsername = getView().findViewById(R.id.reg_username_et);
        regPass1 = getView().findViewById(R.id.reg_pass1_et);
        regPass2 = getView().findViewById(R.id.reg_pass2_et);

        Button regBtn = getView().findViewById(R.id.reg_account_btn);
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = regUsername.getText().toString().trim();
                String email = regEmail.getText().toString().trim();
                String pw1 = regPass1.getText().toString().trim();
                String pw2 = regPass2.getText().toString().trim();

                if (DataHelper.isValidEmail(email)) {
                    if (FormUtils.isValidUsername(username)) {
                        if (FormUtils.isValidPassword(pw1)) {
                            if (FormUtils.passwordCheck(pw1, pw2)) {
                                mListener.register(email, pw1, username);
                            } else {
                                regPass2.setError("Password must match");
                            }
                        } else {
                            regPass1.setError("Password needs to be at least 7 characters");
                        }
                    } else {
                        regUsername.setError("Username needs to be at least 7 characters");
                    }
                }else{
                    regEmail.setError("Enter A Valid Email Address");
                }

//                if (FormUtils.isValidUsername(username)) {
//                    if (DataHelper.isValidEmail(email)) {
//                        if (FormUtils.isValidPassword(pw1)) {
//                            if (FormUtils.passwordCheck(pw1, pw2)) {
//                                mListener.register(email, pw1, username);
//                            } else {
//                                regPass2.setError("Password must match");
//                            }
//                        } else {
//                            regPass1.setError("Password needs to be at least 7 characters");
//                        }
//                    } else {
//                        regEmail.setError("Enter A Valid Email Address");
//                    }
//                }else{
//                    regUsername.setError("Username needs to be at least 7 characters");
//                }
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RegListener){
            mListener = (RegListener) context;
        }
    }

    public interface RegListener {
        void register(String email, String pass, String username);

    }
}
