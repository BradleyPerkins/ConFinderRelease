package com.bradperkins.confinder.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.fragments.ConListFragment;
import com.bradperkins.confinder.objects.Con;
import com.bradperkins.confinder.utils.DataCache;
import com.bradperkins.confinder.utils.DataHelper;
import com.bradperkins.confinder.utils.NotificationReceiver;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        ConListFragment.ListClickListener, LocationListener {

    private static final String TAG = "MainActivity.TAG";

    public static final String LIST_EXTRA = "MainActivity.LIST_EXTRA";
    public static final String POSITION_EXTRA = "MainActivity.POSITION_EXTRA";
    public static final String LAT_EXTRA = "MainActivity.LAT_EXTRA";
    public static final String LNG_EXTRA = "MainActivity.LNG_EXTRA";

    private FirebaseUser currentUser;

    private DatabaseReference mRef;

    private static final int REQUEST_LOCATION_PERMISSION = 0x01001;
    private LocationManager locationManager;
    private boolean requestUpdates = false;

    private double currLongitude;
    private double currLatitude;

    private Con con;

    private ArrayList<String> titles;
    private ArrayList<Con> searchList;
    private ArrayList<Con> historyList;
    private ArrayList<Con> conList;
    private ConListFragment conFrag;
    private boolean isAdmin;
    private boolean isHistNav;
    private boolean searchNav;
    private boolean sortDate;


    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ads
        MobileAds.initialize(this, "ca-app-pub-4730800757487119~2912262045");
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        adView.loadAd(adRequest);


        SharedPreferences adminPref = getSharedPreferences("ADMIN_DATA" ,this.MODE_PRIVATE);
        isAdmin = adminPref.getBoolean("ADMIN_BOOL", false);

        //Sort Prefs
        SharedPreferences sortPref = getSharedPreferences("SORT_PREF" ,this.MODE_PRIVATE);
        sortDate = sortPref.getBoolean("SORT_DATE", true);

        SharedPreferences welcomePref = getSharedPreferences("WELCOME_BOOL" , Context.MODE_PRIVATE);
        boolean hasBeenWelcomed = welcomePref.getBoolean("WELCOME", false);
        boolean notifyActivated = welcomePref.getBoolean("NOTIFY", false);

        if (!notifyActivated){
            startNotification();
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            isHistNav = getIntent().getBooleanExtra("HIST_BOOL", false);
        }

        getUserLoc();
        updateLocation();

        if (!hasBeenWelcomed){
            welcomeDisplay();
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();

        if (conList == null) {
            conList = new ArrayList<>();
        }

        if (isHistNav){
            isHistNav = false;
            historyList = DataCache.loadHistoryData(this);
            conFrag = ConListFragment.newInstance(historyList);
            getFragmentManager().beginTransaction().replace(R.id.main_placeholder, conFrag).commit();
        }else {
            //Pull local data for offline
            if (!DataHelper.hasNetwork(this)) {
                conList = DataCache.loadConData(this);
                ArrayList<Con> masterList = DataCache.loadMasterList(this);
            } else {
                firebaseData();
                if (isAdmin){
                    if (historyList == null) {
                        historyList = new ArrayList<>();
                    }
                    //pull history from Database
                    firebaseHistoryData();
                }
            }
            conList = DataCache.loadConData(this);
            conFrag = ConListFragment.newInstance(conList);
            getFragmentManager().beginTransaction().replace(R.id.main_placeholder, conFrag).commit();
        }
        setUpMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //initiate the notification of fav cons
    private void startNotification() {
        SharedPreferences sharedPref = getSharedPreferences("WELCOME_BOOL" , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("NOTIFY", true);
        editor.commit();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 30);
        Intent notifyIntent = new Intent(this, NotificationReceiver.class);
        notifyIntent.setAction("CON_NOTIFICATION");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 100, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void firebaseHistoryData() {
        mRef.child("history").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pullHistoryData(dataSnapshot);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void firebaseData() {
        mRef = FirebaseDatabase.getInstance().getReference();
        mRef.child("conventions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pullData(dataSnapshot);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void pullData(DataSnapshot dataSnapshot) {
        con = new Con();
        conList = new ArrayList<>();
        int day = DataHelper.nextDay();
        for (final DataSnapshot consSnapshot : dataSnapshot.getChildren()) {
            con = consSnapshot.getValue(Con.class);
            if (con.getId() >= day){
                conList.add(con);
                double distance = DataHelper.distance(currLatitude, currLongitude, con.getLatitude(), con.getLongitude(), "M");
                con.setDistance(distance);
            }
        }

        if (!sortDate){
            Comparator comp = new Comparator<Con>() {
                public int compare(Con o1, Con o2) {
                    return (int) (o1.getDistance() - o2.getDistance());
                }
            };
            if (conList != null) {
                Collections.sort(conList, comp);
            }
        } else{
            Comparator comp2 = new Comparator<Con>() {
                public int compare(Con d1, Con d2) {
                    return (int) (d1.getId() - d2.getId());
                }
            };
            if (conList != null) {
                Collections.sort(conList, comp2);
            }
        }

        DataCache.saveMasterList(this, conList);

        //Save con data to file
        DataCache.saveConData(this, conList);
        conList = DataCache.loadConData(this);
        ConListFragment.adapter.notifyDataSetChanged();
    }

    private void pullHistoryData(DataSnapshot dataSnapshot) {
        con = new Con();
        historyList = new ArrayList<>();
        for (final DataSnapshot consSnapshot : dataSnapshot.getChildren()) {
            con = consSnapshot.getValue(Con.class);
            historyList.add(con);
            double distance = DataHelper.distance(currLatitude, currLongitude, con.getLatitude(), con.getLongitude(), "M");
            con.setDistance(distance);
        }
        DataCache.saveHistoryData(this, historyList);
    }

    @Override
    public void onBackPressed() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_main);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_main:
                Intent mainIntent = new Intent(this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
                break;
            case R.id.nav_fav:
                Intent favIntent = new Intent(this, FavoriteActivity.class);
                favIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(favIntent);
                break;
            case R.id.nav_map:
                Intent mapIntent = new Intent(this, MapActivity.class);
                mapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mapIntent);
                break;
            case R.id.nav_add_con:
                Intent addIntent = new Intent(this, AddConActivity.class);
                addIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(addIntent);
                break;
            case R.id.nav_profile:
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(profileIntent);
                break;
            case R.id.nav_logout:
                DataHelper.loggingOutDialog(this);
                break;

            //ADMIN FEATURES
            case R.id.nav_history:
                historyList = DataCache.loadHistoryData(this);
                isHistNav = true;
                conFrag = ConListFragment.newInstance(historyList);
                getFragmentManager().beginTransaction().replace(R.id.main_placeholder, conFrag).commit();
                break;
            case R.id.nav_admin_comments:
                Intent commentIntent = new Intent(this, CommentsActivity.class);
                commentIntent.putExtra("CON_TITLE", "Admin Reports");
                commentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(commentIntent);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_main);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void conPosition(int pos, ArrayList<Con> conList) {
        Intent intent = new Intent(MainActivity.this, ConDetailActivity.class);
        intent.putExtra(MainActivity.LIST_EXTRA, conList);
        intent.putExtra(MainActivity.POSITION_EXTRA, pos);

        if (isHistNav){
            intent.putExtra("HISTORY_EXTRA", true);
        }
        startActivity(intent);
    }

    @Override
    public void searchList(String search) {
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        if (titles == null){ titles = new ArrayList<>(); }
        titles.clear();
        if (searchList == null){ searchList = new ArrayList<>(); }
        searchList.clear();
        con = new Con();
        String searchLower = search.toLowerCase();
        //Search by zip
        if (!searchLower.equals("")) {
            if (searchLower.matches("[0-9]+")) {
                for (int i = 0; i < conList.size(); i++) {
                    String zip = String.valueOf(conList.get(i).getZip());
                    if (zip.contains(searchLower)) {
                        con = conList.get(i);
                        searchList.add(con);
                        createsearchTitleList(searchList);
                    }
                }
            } else {
                for (int i = 0; i < conList.size(); i++) {
                    //Search Title
                    if (conList.get(i).getTitle().toLowerCase().contains(searchLower)  && !titles.contains(conList.get(i).getConid())) {
                        con = conList.get(i);
                        searchList.add(con);
                        createsearchTitleList(searchList);
                    }
                    //State abrev search
                    if (conList.get(i).getState().toLowerCase().contains(searchLower) && !titles.contains(conList.get(i).getConid())) {
                        con = conList.get(i);
                        searchList.add(con);
                        createsearchTitleList(searchList);
                    }
                    //State full search
                    String stateAbrev = DataHelper.stateConvertor(searchLower);
                    if (conList.get(i).getState().toLowerCase().contains(stateAbrev) && !titles.contains(conList.get(i).getConid())) {
                        con = conList.get(i);
                        searchList.add(con);
                        createsearchTitleList(searchList);
                    }

                }
            }

            if (searchList.size() == 0) {
                Toast.makeText(this, "No Cons found matching search text", Toast.LENGTH_SHORT).show();
            } else{
                conFrag = ConListFragment.newInstance(searchList);
                getFragmentManager().beginTransaction().replace(R.id.main_placeholder, conFrag).commit();
            }
        }

    }

    private void createsearchTitleList(ArrayList<Con> searchList){
        for (int i=0; i<searchList.size(); i++){
            titles.add(searchList.get(i).getConid());
        }
    }

    //Setup the menu
    private void setUpMenu() {
        //Menu items
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView userNameTV = headerView.findViewById(R.id.nav_username_tv);
        TextView emailTV = headerView.findViewById(R.id.nav_email_tv);
        userNameTV.setText(currentUser.getDisplayName());
        emailTV.setText(currentUser.getEmail());

        TextView adminTV = headerView.findViewById(R.id.admin_label_tv);
        Menu navMenu = navigationView.getMenu();

        if (isAdmin){
            adminTV.setVisibility(View.VISIBLE);
            navMenu.findItem(R.id.nav_history).setVisible(true);
            navMenu.findItem(R.id.nav_admin_comments).setVisible(true);
        } else {
            adminTV.setVisibility(View.INVISIBLE);
            navMenu.findItem(R.id.nav_history).setVisible(false);
            navMenu.findItem(R.id.nav_admin_comments).setVisible(false);
        }

        navigationView.setNavigationItemSelectedListener(this);

    }

    //Welcome Display
    private void welcomeDisplay(){
        getUserLoc();
        SharedPreferences sharedPref = getSharedPreferences("WELCOME_BOOL" , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("WELCOME", true);
        editor.commit();

        getUserLoc();
        updateLocation();

        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Hi there!")
                .setMessage("Welcome To ConFinder \nTap OK To View Some Great Cons. \nThanks For Joining!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getUserLoc();
                        updateLocation();
                        firebaseData();
                        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainIntent);
                    }
                }).show();
    }

    //Get User Location
    private void getUserLoc() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location lastKnown = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){

            if (lastKnown != null){
                currLongitude = lastKnown.getLongitude();
                currLatitude = lastKnown.getLatitude();
            }
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    //Gets the Current Location
    public void updateLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && !requestUpdates){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    2000, 16, (LocationListener) this);
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
    public void onStatusChanged(String s, int i, Bundle bundle) { }

    @Override
    public void onProviderEnabled(String s) { }

    @Override
    public void onProviderDisabled(String s) { }

}
