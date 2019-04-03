package com.bradperkins.confinder.fragments;

import android.Manifest;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.objects.Con;
import com.bradperkins.confinder.utils.CustomAdapter;
import com.bradperkins.confinder.utils.DataCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.content.Context.LOCATION_SERVICE;


public class ConListFragment extends ListFragment implements LocationListener {

    private static final int REQUEST_LOCATION_PERMISSION = 0x01001;
    private LocationManager locationManager;
    private boolean requestUpdates = false;

    private double currLongitude;
    private double currLatitude;

    private SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    private static final String ARG_CONS = "ARG_CONS";
    private ArrayList<Con> conList;
    private ListClickListener mListener;
    public static CustomAdapter adapter;

    private EditText searchEt;

    public ConListFragment() {
    }

    public static ConListFragment newInstance(ArrayList<Con> conList) {
        ConListFragment fragment = new ConListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONS, conList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getUserLoc();
        sharedPref = getContext().getSharedPreferences("SORT_PREF" , Context.MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    //Sorting Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_sort_date:

                //TODO set user prefs true or false
                boolean sortDist = false;
                boolean sortDate = true;

                editor.putBoolean("SORT_DATE", true);
                editor.commit();

                Comparator comp1 = new Comparator<Con>() {
                    public int compare(Con o1, Con o2) {
                        return (int) (o1.getId() - o2.getId());
                    }
                };
                if (conList != null) {
                    Collections.sort(conList, comp1);
                    adapter.notifyDataSetChanged();
                }

                break;
            case R.id.action_sort_distance:

                //TODO set user prefs true or false
                sortDist = true;
                sortDate = false;

                editor.putBoolean("DATE", false);
                editor.commit();
                Comparator comp2 = new Comparator<Con>() {
                    public int compare(Con o1, Con o2) {
                        return (int) (o1.getDistance() - o2.getDistance());
                    }
                };

                if (conList != null) {
                    Collections.sort(conList, comp2);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_con_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        conList = (ArrayList<Con>) getArguments().getSerializable(ARG_CONS);
        ImageView searchBtn = getView().findViewById(R.id.search_btn);
        searchEt = getView().findViewById(R.id.search_et);

        adapter = new CustomAdapter(getActivity(), conList);

//        //Ads
//        MobileAds.initialize(getActivity(), "ca-app-pub-4730800757487119~9906768001");
//
//        AdView adView = getView().findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        adView.loadAd(adRequest);

//        getListView().setDividerHeight(5);
        setListAdapter(adapter);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!searchEt.getText().toString().isEmpty()) {
                    mListener.searchList(searchEt.getText().toString().trim());
                    searchEt.setText("");
                }
            }
        });
        searchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    if (!searchEt.getText().toString().isEmpty()) {
                        mListener.searchList(searchEt.getText().toString().trim());
                        searchEt.setText("");
                    }
                    handled = true;
                }
                return handled;
            }
        });

    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mListener.conPosition(position, conList);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        getUserLoc();
        conList = DataCache.loadConData(getActivity());
        setListAdapter(adapter);

        if (context instanceof ListClickListener){
            mListener = (ListClickListener) context;
        }
    }

    public interface ListClickListener{
        void conPosition(int pos, ArrayList<Con> conList);
        void searchList(String search);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private void getUserLoc(){
        locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            Location lastKnown = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);

            if (lastKnown != null){
                currLongitude = lastKnown.getLongitude();
                currLatitude = lastKnown.getLatitude();
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
//        updateLocation();

    }

    //Gets the Current Location
    public void updateLocation(){
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && !requestUpdates){

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    2000, 16, (LocationListener) this);
//            conFrag = ConListFragment.newInstance();
//            getFragmentManager().beginTransaction().replace(R.id.main_placeholder, conFrag).commit();
            requestUpdates = true;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocation();
        currLongitude = location.getLongitude();
        currLatitude = location.getLatitude();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
