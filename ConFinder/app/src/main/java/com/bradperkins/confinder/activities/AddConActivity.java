package com.bradperkins.confinder.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.fragments.AddConFragment;
import com.bradperkins.confinder.fragments.SpinnerFragment;
import com.bradperkins.confinder.objects.Con;
import com.bradperkins.confinder.utils.DataHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AddConActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AddConFragment.AddConListener {

    private FirebaseUser currentUser;

    private DatabaseReference mRef;

    private ArrayList<String> titleList;

    private ArrayList<Con> conList = null;
    private int position = 0;
    private String objStr = "";
    private boolean isEdit = false;
    private boolean isHist = false;

    private String imageUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_con);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        int result = RESULT_OK;


        int ALL_PERMISSIONS = 101;

        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            conList = (ArrayList<Con>) getIntent().getSerializableExtra("EDIT_LIST");
            position = getIntent().getIntExtra("EDIT_POS", 0);
            objStr = getIntent().getStringExtra("EDIT_OBJ");
            isEdit = getIntent().getBooleanExtra("EDIT_BOOL", false);
            isHist = getIntent().getBooleanExtra("HIST_BOOL", false);
            //If Coming from history of removed con
            if (isHist){
                isEdit = false;
            }
        }

        titleList = new ArrayList<>();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();


        //build the titleList for new cons
        mRef.child("conventions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot consSnapshot : dataSnapshot.getChildren()) {
                    String data = String.valueOf(consSnapshot.getKey());
                    titleList.add(data);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        getFragmentManager().beginTransaction().replace(R.id.add_con_placeholder, AddConFragment.newInstance(conList, position, objStr, result)).commit();

        setupMenu();
    }

    private void setupMenu() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        DrawerLayout drawer = findViewById(R.id.drawer_layout_add_con);
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
        SharedPreferences adminPref = getSharedPreferences("ADMIN_DATA" ,MODE_PRIVATE);
        boolean isAdmin = adminPref.getBoolean("ADMIN_BOOL", false);

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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_add_con);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_con, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_main:
                Intent mainIntent = new Intent(this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
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
                profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(profileIntent);

                break;
            case R.id.nav_logout:
                //Sign out out of firebase
                DataHelper.loggingOutDialog(this);
                break;

            //ADMIN FEATURES
            case R.id.nav_history:
                Intent histIntent = new Intent(this, MainActivity.class);
                histIntent.putExtra("HIST_BOOL", true);
                histIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(histIntent);
                break;

            case R.id.nav_admin_comments:
                Intent commentIntent = new Intent(this, CommentsActivity.class);
                commentIntent.putExtra("CON_TITLE", "Admin Reports");
                startActivity(commentIntent);
                break;
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout_add_con);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void submitCon(final String title, final String venue, final String address,
                          final String city, final String state, final double zip, final String website,
                          final String tickets, final String day1, final String day2, final String day3,
                          final String day4, String image, final double id, final int attending, final int likes,
                          final String date, Uri imgUri, final String conId) {

        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        imageUrl = "";
        String imgTitle = title + timeStamp;

        if (imgUri !=null){
            //Start Progress Spinner
            getFragmentManager().beginTransaction().add(R.id.add_con_placeholder, SpinnerFragment.newInstance()).commit();
            if (imgUri.toString() == "edit"){
                imageUrl = conList.get(position).getImage();
                mRef.child("conventions").child(objStr).removeValue();
                uploadNewConData("Con Just Edited!!!", title, venue,
                        address, city, state, zip, website, tickets, day1, day2, day3, day4,
                        imageUrl, id, attending, likes, date, conId);

                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
                getFragmentManager().beginTransaction().remove(SpinnerFragment.newInstance()).commit();

            }

            //Start progress
            final StorageReference ref = storageReference.child("images").child(imgTitle);
            final UploadTask uploadTask = ref.putFile(imgUri);
            uploadTask.addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUrl = uri;
                            imageUrl = String.valueOf(downloadUrl);
                            //Stop progreess bar
                            getFragmentManager().beginTransaction().remove(SpinnerFragment.newInstance()).commit();
                            if (isEdit){
                                //Remove old Con then add updated
                                mRef.child("conventions").child(objStr).removeValue();
                                uploadNewConData("Con Just Edited!!!", title, venue,
                                        address, city, state, zip, website, tickets, day1, day2, day3, day4,
                                        imageUrl, id, attending, likes, date, conId);

                                //Nav back
                                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(mainIntent);

                            } else {
                                uploadNewConData("New Con Added!!!", title, venue,
                                        address, city, state, zip, website, tickets, day1, day2, day3, day4,
                                        imageUrl, id, attending, likes, date, conId);

                                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(mainIntent);
                            }
                        }
                    });
                }
            });
        } else{
            if (isEdit && !isHist){
                //Remove old Con then add updated
                uploadNewConData("Con Just Edited!!! " + DataHelper.dateNotifyStamp(), title, venue,
                        address, city, state, zip, website, tickets, day1, day2, day3, day4,
                        imageUrl, id, attending, likes, date, conId);

                mRef.child("conventions").child(objStr).removeValue();

                //Nav back
                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);

            }if (isEdit && isHist ){
                //Remove old Con then add updated
                uploadNewConData("Con Just Edited!!! "  + DataHelper.dateNotifyStamp(), title, venue,
                        address, city, state, zip, website, tickets, day1, day2, day3, day4,
                        imageUrl, id, attending, likes, date, conId);

                //Nav back
                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);

            } else {
                uploadNewConData("New Con Added!!! "  + DataHelper.dateNotifyStamp(), title, venue,
                        address, city, state, zip, website, tickets, day1, day2, day3, day4,
                        imageUrl, id, attending, likes, date, conId);

                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
            }
        }
    }

    public void uploadNewConData(String messageToAdmin, String title, String venue, String address, String city, String state,
                                  double zip, String website, String tickets, String day1, String day2, String day3, String day4,
                                  String image, double id, int attending, int likes, String date, String conid) {
        String admin = currentUser.getUid();
        String lastConTitle = titleList.get(titleList.size() - 1);
        int newConTitleInt = Integer.parseInt(lastConTitle) + 1;
        String conTitle = String.valueOf(newConTitleInt);

        //Get long and lat from address
        LatLng latLng = DataHelper.getLocationFromAddress(this,address + " " + city + ", " + state + " " + zip);

        double lat = latLng.latitude;
        double lng = latLng.longitude;

        String timeStamp = new SimpleDateFormat("MMddyyHHmm").format(new Date());

        mRef.child("conventions").child(conTitle).child("address").setValue(address);
        mRef.child("conventions").child(conTitle).child("building").setValue(venue);
        mRef.child("conventions").child(conTitle).child("city").setValue(city);
        mRef.child("conventions").child(conTitle).child("date").setValue(date);
        mRef.child("conventions").child(conTitle).child("hours1").setValue(day1);
        mRef.child("conventions").child(conTitle).child("hours2").setValue(day2);
        mRef.child("conventions").child(conTitle).child("hours3").setValue(day3);
        mRef.child("conventions").child(conTitle).child("hours4").setValue(day4);
        mRef.child("conventions").child(conTitle).child("id").setValue(id);
        mRef.child("conventions").child(conTitle).child("image").setValue(image);
        mRef.child("conventions").child(conTitle).child("state").setValue(state);
        mRef.child("conventions").child(conTitle).child("tickets").setValue(tickets);
        mRef.child("conventions").child(conTitle).child("title").setValue(title);
        mRef.child("conventions").child(conTitle).child("url").setValue(website);
        mRef.child("conventions").child(conTitle).child("zip").setValue(zip);
        mRef.child("conventions").child(conTitle).child("latitude").setValue(lat);
        mRef.child("conventions").child(conTitle).child("longitude").setValue(lng);
        mRef.child("conventions").child(conTitle).child("likes").setValue(likes);
        mRef.child("conventions").child(conTitle).child("attending").setValue(attending);
        mRef.child("conventions").child(conTitle).child("admin").setValue(admin);
        mRef.child("conventions").child(conTitle).child("conid").setValue(conid);

        //Send Report to Admin of new Cons
        //ADD TO REPORT
        String user = currentUser.getDisplayName() + "-" + title + "-" + DataHelper.timeStamp();
        mRef.child("reports").child(user).child("address").setValue(address);
        mRef.child("reports").child(user).child("building").setValue(venue);
        mRef.child("reports").child(user).child("city").setValue(city);
        mRef.child("reports").child(user).child("date").setValue(date);
        mRef.child("reports").child(user).child("hours1").setValue(day1);
        mRef.child("reports").child(user).child("hours2").setValue(day2);
        mRef.child("reports").child(user).child("hours3").setValue(day3);
        mRef.child("reports").child(user).child("hours4").setValue(day4);
        mRef.child("reports").child(user).child("id").setValue(id);
        mRef.child("reports").child(user).child("image").setValue(image);
        mRef.child("reports").child(user).child("state").setValue(state);
        mRef.child("reports").child(user).child("tickets").setValue(tickets);
        mRef.child("reports").child(user).child("title").setValue(title);
        mRef.child("reports").child(user).child("url").setValue(website);
        mRef.child("reports").child(user).child("zip").setValue(zip);
        mRef.child("reports").child(user).child("latitude").setValue(lat);
        mRef.child("reports").child(user).child("longitude").setValue(lng);
        mRef.child("reports").child(user).child("likes").setValue(likes);
        mRef.child("reports").child(user).child("attending").setValue(attending);
        mRef.child("reports").child(user).child("admin").setValue(currentUser.getUid());
        mRef.child("reports").child(user).child("report").setValue(messageToAdmin + "@" + timeStamp);
        mRef.child("reports").child(user).child("conid").setValue(conid);
    }


}
