package com.bradperkins.confinder.fragments;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.objects.Con;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ConDetailFragment extends Fragment {

    private static final String ARG_CONS = "ARG_CONS";
    private static final String ARG_POSITION = "ARG_POSITION";
    private static final String ARG_ISFAV = "ARG_ISFAV";
    private static final String ARG_ISLIKE = "ARG_ISLIKE";
    private static final String ARG_ISATTEND = "ARG_ISATTEND";

    private String conTitle;

    private int likesCount;
    private int attendCount;

    private boolean isLike = false;
    private boolean isFav = false;
    private boolean isAttend = false;

    private int likesListSize;

    private ConDetailListener mListener;

    public ConDetailFragment() {
        // Required empty public constructor
    }

    public static ConDetailFragment newInstance(ArrayList<Con> conList, int pos, boolean isFav, boolean isLike, boolean isAttend) {
        Bundle args = new Bundle();

        args.putInt(ARG_POSITION, pos);
        args.putBoolean(ARG_ISFAV, isFav);
        args.putBoolean(ARG_ISLIKE, isLike);
        args.putBoolean(ARG_ISATTEND, isAttend);
        args.putSerializable(ARG_CONS, conList);
        ConDetailFragment fragment = new ConDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_con_detail, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayList<Con> conList = (ArrayList<Con>) getArguments().getSerializable(ARG_CONS);
        int position = getArguments().getInt(ARG_POSITION);
        isFav = getArguments().getBoolean(ARG_ISFAV);
        isLike = getArguments().getBoolean(ARG_ISLIKE);
        isAttend = getArguments().getBoolean(ARG_ISATTEND);


        conTitle = conList.get(position).getTitle();
        String date = conList.get(position).getDate();
        String building = conList.get(position).getBuilding();
        String address = conList.get(position).getAddress();
        String dateTime = conList.get(position).getDate();
        likesCount =  conList.get(position).getLikes();
        attendCount = conList.get(position).getAttending();

        ImageView conIV = getView().findViewById(R.id.con_iv);
        ImageView commentsBtn = getView().findViewById(R.id.comment_btn);
        final ImageView attendBtn = getView().findViewById(R.id.attend_btn);

        final ImageView likeBtn = getView().findViewById(R.id.like_btn);
        final ImageView favBtn = getView().findViewById(R.id.fav_btn);
        TextView ticketsBtn = getView().findViewById(R.id.tickets_btn);
        TextView directionsBtn = getView().findViewById(R.id.directions_btn);

        final TextView likesTV = getView().findViewById(R.id.likes_tv);
        final TextView conTitleTV = getView().findViewById(R.id.con_title_tv);
        TextView dateTV = getView().findViewById(R.id.date_tv);
        TextView buildingTV = getView().findViewById(R.id.building_tv);
        TextView addressTV = getView().findViewById(R.id.address_tv);
        TextView cityZipTV = getView().findViewById(R.id.city_state_zip_tv);
        TextView dateTimeTV = getView().findViewById(R.id.dates_time_tv);
        final TextView attendTV = getView().findViewById(R.id.attend_tv);

        String placeholder = getString(R.string.placeholder_url);
        String imageLocation = conList.get(position).getImage();
        if (imageLocation == "" || imageLocation == null || imageLocation.isEmpty()){
            Picasso.with(getContext()).load(placeholder).centerInside().fit().into(conIV);
        }else {
            Picasso.with(getContext()).load(imageLocation).centerInside().fit().into(conIV);
        }
        likesTV.setText(likesCount + " - Likes by users");
        attendTV.setText(attendCount + " - Users Attending");
        conTitleTV.setText(conTitle);
        dateTV.setText(date);
        buildingTV.setText(building);
        addressTV.setText(address + " \n" + conList.get(position).getCity() + " "
                + conList.get(position).getState() + ", "
                + ((int)conList.get(position).getZip()));
        cityZipTV.setText("");
        dateTimeTV.setText(conList.get(position).getHours1()
                + " \n" + conList.get(position).getHours2()
                + " \n" + conList.get(position).getHours3()
                + " \n" + conList.get(position).getHours4());

        ///LIKE
        if (isLike){
            likeBtn.setImageResource(R.drawable.ic_favorite_red);
        } else{
            likeBtn.setImageResource(R.drawable.ic_favorite_border_red);
        }

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (!isLike){
                likesCount++;
                isLike = true;
                mListener.likeCon(isLike, conTitle);
                likesTV.setText(likesCount + " - User Likes");
                likeBtn.setImageResource(R.drawable.ic_favorite_red);
            } else {
                likesCount--;
                isLike = false;
                mListener.likeCon(isLike, conTitle);
                likesTV.setText(likesCount + " - User Likes");
                likeBtn.setImageResource(R.drawable.ic_favorite_border_red);
            }
            }
        });

        ////FAV
        if (isFav){
            favBtn.setImageResource(R.drawable.ic_star_black);
        } else{
            favBtn.setImageResource(R.drawable.ic_star_border_black);
        }

        favBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!isFav){
                isFav = true;
                mListener.favCon(isFav, conTitle);
                favBtn.setImageResource(R.drawable.ic_star_black);
            } else {
                isFav = false;
                mListener.favCon(isFav, conTitle);
                favBtn.setImageResource(R.drawable.ic_star_border_black);
            }
            }
        });

        //Attend Button
        if (isAttend){
            attendBtn.setImageResource(R.drawable.ic_attend_mark_24dp);
        } else{
            attendBtn.setImageResource(R.drawable.ic_attend_24dp);
        }

        attendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isAttend){
                    attendCount++;
                    isAttend = true;
                    mListener.attendCon(isAttend, conTitle);
                    attendTV.setText(attendCount + " - Users Attending");
                    attendBtn.setImageResource(R.drawable.ic_attend_mark_24dp);
                } else {
                    attendCount--;
                    isAttend = false;
                    mListener.attendCon(isAttend, conTitle);
                    attendTV.setText(attendCount + " - Users Attending");
                    attendBtn.setImageResource(R.drawable.ic_attend_24dp);
                }
            }
        });

        //Comments
        commentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.comment();
            }
        });

        //Tickets
        ticketsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.tickets();
            }
        });

        //Directions
        directionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.directions();
            }
        });

        //Con Site
        conTitleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.conURL();
            }
        });

        likesTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.likesListNav();
            }
        });

        attendTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.attendingListNav();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ConDetailListener){
            mListener = (ConDetailListener) context;
        }
    }

    public interface ConDetailListener {
        void likeCon(boolean like, String title);
        void favCon(boolean fav, String title);
        void attendCon(boolean attend, String title);
        void attendingListNav();
        void tickets();
        void conURL();
        void directions();
        void comment();
        void likesListNav();

    }

}


















