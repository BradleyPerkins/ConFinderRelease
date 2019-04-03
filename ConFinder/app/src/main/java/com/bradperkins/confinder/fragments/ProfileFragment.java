package com.bradperkins.confinder.fragments;


import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.objects.Con;
import com.bradperkins.confinder.utils.DataCache;

import java.util.ArrayList;


public class ProfileFragment extends Fragment {

    private static final String ARG_NAME = "ARG_NAME";
    private static final String ARG_EMAIL = "ARG_EMAIL";

    private static final String FILE_LIKES = "likes.dat";

    private ProfileListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String name, String email) {
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_EMAIL, email);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences notifyPref = getActivity().getSharedPreferences("NOTIFY_PREF" ,getContext().MODE_PRIVATE);
        boolean isNotified = notifyPref.getBoolean("NOTIFY_PREF_BOOL", false);

        if (getArguments() != null){
            String name = getArguments().getString(ARG_NAME);
            String email = getArguments().getString(ARG_EMAIL);

            TextView edit = getView().findViewById(R.id.edit_prof_btn);
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.editProfile();
                }
            });

            TextView nameTV = getView().findViewById(R.id.prof_name);
            nameTV.setText(name);
            TextView emailTV = getView().findViewById(R.id.prof_email);
            emailTV.setText(email);

            ArrayList<String> likesList =DataCache.loadUserConData(getContext(), FILE_LIKES);
            TextView likeTV = getView().findViewById(R.id.prof_num_likes);
            int like = likesList.size();
            likeTV.setText(like + "  Likes ");

            ArrayList<Con> favList =DataCache.loadFavConData(getContext());
            TextView favTV = getView().findViewById(R.id.prof_num_favs);
            int fav = favList.size();
            favTV.setText(fav + "  Favorite Cons ");

            //Switch
            Switch alertSwitch = getView().findViewById(R.id.notifiy_switch);
            alertSwitch.setChecked(isNotified);
            alertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mListener.conAlerts(b);
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProfileFragment.ProfileListener){
            mListener = (ProfileFragment.ProfileListener) context;
        }
    }

    public interface ProfileListener {
        void editProfile();
        void conAlerts(boolean isNotify);

    }

}
