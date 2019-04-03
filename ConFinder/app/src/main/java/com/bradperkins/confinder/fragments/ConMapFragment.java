package com.bradperkins.confinder.fragments;

// Date 12/7/18
// 
// Bradley Perkins

// AID - 1809

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.activities.ConDetailActivity;
import com.bradperkins.confinder.activities.MainActivity;
import com.bradperkins.confinder.objects.Con;
import com.bradperkins.confinder.utils.DataCache;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

// PerkinsBradley_CE
public class ConMapFragment extends MapFragment implements OnMapReadyCallback, LocationListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    private static final int REQUEST_LOCATION_PERMISSION = 0x01001;
    private static final String ARG_MAP = "ARG_MAP";
    private boolean requestUpdates = false;

    private double currLongitude;
    private double currLatitude;
    private int markerPos;
    private String urlImage;

    private boolean hasBeenOpen = false;

    private ArrayList<Con> conList;

    private GoogleMap mMap;
    LocationManager locationManager;

    public static ConMapFragment newInstance(String mapType) {
        Bundle args = new Bundle();
        ConMapFragment fragment = new ConMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_map_normal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.action_map_hybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        conList = DataCache.loadConData(getActivity());

        getMapAsync(this);

        //Get Current Location
        locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(this);
        mMap.setOnInfoWindowClickListener(this);

        zoomInCamera();
        addMapMarkers();
    }

    private void addMapMarkers() {

        for (int i=0; i<conList.size(); i++){
            LatLng markerLatLng = new LatLng(conList.get(i).getLatitude(), conList.get(i).getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(markerLatLng)
                    .title(conList.get(i).getTitle())
                    .snippet(conList.get(i).getBuilding()));
        }

        //Current position
        LatLng currentLatLng = new LatLng(currLatitude, currLongitude);
        mMap.addMarker(new MarkerOptions()
                .position(currentLatLng)
                .title("You Are Here!")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    private void zoomInCamera() {
        if (mMap == null){
            return;
        }
        LatLng currentLatLng = new LatLng(currLatitude, currLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(currLatitude, currLongitude)).zoom(5).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.setOnInfoWindowClickListener(this);

    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View contents = LayoutInflater.from(getActivity())
                .inflate(R.layout.con_info_window, null);
        ((TextView)contents.findViewById(R.id.marker_name_tv)).setText(marker.getTitle());

        int pos = getItemPos(marker.getTitle());

        ((TextView)contents.findViewById(R.id.marker_info_tv)).setText(marker.getSnippet() + "\n" + conList.get(pos).getDate());

        ImageView ivThumbnail = contents.findViewById(R.id.marker_banner_iv);
//
//        String placeholder = "https://firebasestorage.googleapis.com/v0/b/comic-shop-finder.appspot.com/o/temp1.png?alt=media&token=323d2764-5606-4ca0-95b9-ad469e115f3e";
//        String imageLocation = conList.get(pos).getImage();
//
//        if (imageLocation == null || imageLocation == "" || imageLocation.isEmpty()){
//            Picasso.with(getActivity()).load(placeholder).fit().into(ivThumbnail);
//        }else {
//            Picasso.with(getActivity()).load(imageLocation).fit().into(ivThumbnail);
//        }
        return contents;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        int pos = getItemPos(marker.getTitle());

        if (pos >= 0) {
            Intent intent = new Intent(getActivity(), ConDetailActivity.class);
            intent.putExtra(MainActivity.LIST_EXTRA, conList);
            intent.putExtra(MainActivity.POSITION_EXTRA, pos);
            startActivity(intent);
        }
    }

    //Gets the Current Location
    public void updateLocation(){
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && !requestUpdates){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    2000, 16, (LocationListener) this);
            requestUpdates = true;
        }
    }

    //Grab marker index of tapped item
    private int getItemPos(String item) {
        int i = 0;
        for (i=0; i<conList.size(); i++) {
            if(conList.get(i).getTitle().equalsIgnoreCase(item)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocation();
        currLongitude = location.getLongitude();
        currLatitude = location.getLatitude();

        LatLng currentLatLng = new LatLng(currLatitude, currLongitude);
        mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You Are Here!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));

        //Center camera on Current Position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(currLatitude, currLongitude)).zoom(10).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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
