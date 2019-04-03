package com.bradperkins.confinder.fragments;


import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.activities.ProfileActivity;
import com.bradperkins.confinder.utils.DataHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment {

    private String newName;
    private String newEmail;

    private EditText emailET;
    private EditText usernameET;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    public static EditProfileFragment newInstance() {
        Bundle args = new Bundle();
        EditProfileFragment fragment = new EditProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        emailET = getView().findViewById(R.id.edit_prof_email_et);

        usernameET = getView().findViewById(R.id.edit_prof_username_et);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save_changes:
                final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                final FirebaseUser user = mAuth.getCurrentUser();

                newEmail = emailET.getText().toString().trim();
                newName = usernameET.getText().toString().trim();

                if (newEmail.isEmpty() && newName.isEmpty()){
                    Toast.makeText(getContext(), "Please enter changes", Toast.LENGTH_SHORT).show();
                    break;
                }

                if (newEmail.isEmpty()){
                    newEmail = user.getEmail();

                }

                if (newName.isEmpty()){
                    //Check to see if available
                    newName = user.getDisplayName();
                }

                //check for valid email
                boolean emailValid = DataHelper.isValidEmail(newEmail);
                if (!emailValid){
                    Toast.makeText(getContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
                } else {

                    new AlertDialog.Builder(getContext())
                            .setTitle("Save")
                            .setMessage("Save the changes to your profile?")
                            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    //Check if username already exists
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(newName).build();
                                    user.updateProfile(profileUpdates);
                                    user.updateEmail(newEmail);

                                    Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                    //Send data back to Profile
                                    profileIntent.putExtra("PROFILE_NEW_NAME", newName);
                                    profileIntent.putExtra("PROFILE_NEW_EMAIL", newEmail);
                                    profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(profileIntent);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();

                    break;

                }


        }

        return super.onOptionsItemSelected(item);
    }

}
