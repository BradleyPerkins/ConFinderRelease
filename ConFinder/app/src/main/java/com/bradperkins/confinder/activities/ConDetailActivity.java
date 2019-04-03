package com.bradperkins.confinder.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.fragments.ConDetailFragment;
import com.bradperkins.confinder.objects.Comments;
import com.bradperkins.confinder.objects.Con;
import com.bradperkins.confinder.objects.ConKey;
import com.bradperkins.confinder.utils.DataCache;
import com.bradperkins.confinder.utils.DataHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ConDetailActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ConDetailFragment.ConDetailListener{

    private static final String TAG = "ConDetailActivity.TAG";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private DatabaseReference mRef;
    private DatabaseReference mLikeRef;

    private ArrayList<ConKey> keyList;

    private ArrayList<Con> conList;
    private ArrayList<Con> favList;

    private ArrayList<String> posList;
    private ArrayList<String> likesList;
    private ArrayList<String> attendList;

    private ArrayList<Comments> userLikedList;
    private ArrayList<Comments> userAttendingList;

    private static final String FILE_LIKES = "likes.dat";
    private static final String FILE_ATTEND = "attend.dat";


    private boolean isAdmin;
    private boolean isAuthor = false;
    private boolean isHistNav = false;
    private boolean dataChange = false;

    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_con_detail);

        SharedPreferences adminPref = getSharedPreferences("ADMIN_DATA" ,this.MODE_PRIVATE);
        isAdmin = adminPref.getBoolean("ADMIN_BOOL", false);

        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();
        mLikeRef = mFirebaseDatabase.getReference();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //Reflect whats on the database
        ArrayList<Con> masterList = DataCache.loadMasterList(this);
        likesList = DataCache.loadUserConData(this, FILE_LIKES);
        attendList = DataCache.loadUserConData(this, FILE_ATTEND);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            conList = (ArrayList<Con>) getIntent().getSerializableExtra(MainActivity.LIST_EXTRA);
            position = getIntent().getIntExtra(MainActivity.POSITION_EXTRA, 0);

            DataHelper.getChild(conList.get(position).getTitle(), this);

            isHistNav = getIntent().getBooleanExtra("HISTORY_EXTRA", false);
        }

        //If userUID is admin , then  reveal the itmens in menu
        if (conList.get(position).getAdmin().equals(currentUser.getUid())){
            isAuthor = true;
        }

        //Creates the posList
        posListMaker();

        //Checks to see if in favlist, attending and like
        boolean isFav = posList.contains(conList.get(position).getTitle());
        boolean isLike = likesList.contains(conList.get(position).getTitle());
        boolean isAttending = attendList.contains(conList.get(position).getTitle());
        Log.d(TAG, "Attending: " + isAttending);

        //Menu Items
        setupMenu();

        //get child obj for db
        DataHelper.getChild(conList.get(position).getTitle(), this);

        //Likes Value Listener
        mRef.child("likes").child(conList.get(position).getConid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Comments comments = new Comments();
                userLikedList = new ArrayList<>();
                for (final DataSnapshot consSnapshot : dataSnapshot.getChildren()) {
                    String user = String.valueOf(consSnapshot.child("username").getValue());
                    comments = new Comments(user, "", "");
                    userLikedList.add(comments);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Attending Value Listener
        mRef.child("attending").child(conList.get(position).getConid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Comments comments = new Comments();
                userAttendingList = new ArrayList<>();
                for (final DataSnapshot consSnapshot : dataSnapshot.getChildren()) {
                    String user = String.valueOf(consSnapshot.child("username").getValue());
                    comments = new Comments(user, "", "");
                    //add users to list
                    userAttendingList.add(comments);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        getFragmentManager().beginTransaction().replace(R.id.detail_placeholder, ConDetailFragment.newInstance(conList, position, isFav, isLike, isAttending)).commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_detail);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            if (dataChange){
                Intent mainIntent = new Intent(this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        MenuItem editItem = menu.findItem(R.id.edit_con);
        MenuItem deleteItem = menu.findItem(R.id.delete_con);
        MenuItem addItem = menu.findItem(R.id.re_add_con);

        if (isAuthor || isAdmin){
            editItem.setVisible(true);
            deleteItem.setVisible(true);
            addItem.setVisible(false);

        } else {
            editItem.setVisible(false);
            deleteItem.setVisible(false);
            addItem.setVisible(false);
        }


        if(isAdmin && isHistNav){
            addItem.setVisible(true);
        }

        if (isHistNav){
            deleteItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //if admin add delete and edit con option
            case R.id.edit_con:
                Intent addIntent = new Intent(getApplicationContext(), AddConActivity.class);
                SharedPreferences deletePref = getSharedPreferences("DELETE_DATA" ,getApplication().MODE_PRIVATE);
                String obj = deletePref.getString("DELETE_KEY", null);
                addIntent.putExtra("EDIT_OBJ", obj);
                addIntent.putExtra("EDIT_LIST", conList);
                addIntent.putExtra("EDIT_POS", position);
                addIntent.putExtra("EDIT_BOOL", true);
                addIntent.putExtra("HIST_BOOL", isHistNav);

                startActivity(addIntent);
                break;

            case R.id.delete_con:
                final EditText deleteET;
                final String conTitle = conList.get(position).getTitle();
                AlertDialog.Builder removeBuilder = new AlertDialog.Builder(this);
                removeBuilder.setTitle("Remove "  + conTitle + "?");
                deleteET = new EditText(this);
                removeBuilder.setView(deleteET);
                deleteET.setHint("Reason For Removal");
                removeBuilder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //also remove from favorites list
                        SharedPreferences deletePref = getSharedPreferences("DELETE_DATA" ,getApplication().MODE_PRIVATE);
                        String obj = deletePref.getString("DELETE_KEY", null);

                        if (!deleteET.getText().toString().isEmpty()){
                            addConToHistory("CON REMOVAL!!! - " + deleteET.getText().toString().trim(), conTitle);
                            //Remove con from Firebases main list
                            if (isAdmin){
                                mRef.child("conventions").child(obj).removeValue();
                            }

                            Toast.makeText(ConDetailActivity.this, conTitle + " Remove Request will be evaluated.", Toast.LENGTH_SHORT).show();
                            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(mainIntent);
                        } else {
                            Toast.makeText(ConDetailActivity.this, "Please fill out a Reason For Deleting", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                removeBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog removeDialog = removeBuilder.create();
                removeDialog.show();
                break;

            case R.id.report:
                final EditText reportET;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Enter problem with "  + conList.get(position).getTitle());
                reportET = new EditText(this);
                builder.setView(reportET);
                reportET.setHint("Whats the problem?");

                builder.setPositiveButton("Submit Report", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
                        String title = conList.get(position).getTitle() + timeStamp;
                        String conTitle = conList.get(position).getTitle();
                        String user = currentUser.getDisplayName() + timeStamp;
                        Toast.makeText(ConDetailActivity.this, "Thanks, Problem Reported for " + conList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
                        if (!reportET.getText().toString().isEmpty()){
                            addConToHistory(reportET.getText().toString().trim(), conTitle);
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog reportDialog = builder.create();
                reportDialog.show();
                break;

            case R.id.share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check this Con out!");
                shareIntent.putExtra(Intent.EXTRA_TEXT, conList.get(position).getUrl());
                startActivity(Intent.createChooser(shareIntent, "Share Con"));
                break;


            case R.id.re_add_con:
                reUploadNewConData(conList.get(position));
//                Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                shareIntent.setType("text/plain");
//                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check this Con out!");
//                shareIntent.putExtra(Intent.EXTRA_TEXT, conList.get(position).getUrl());
//                startActivity(Intent.createChooser(shareIntent, "Share Con"));
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void addConToHistory(String reportedText, String conTitle){
        String timeStamp = new SimpleDateFormat("MMddyyHHmm").format(new Date());
        String user = timeStamp + "_" + conTitle;

        //ADD TO REPORT
        mRef.child("reports").child(user).child("address").setValue(conList.get(position).getAddress());
        mRef.child("reports").child(user).child("building").setValue(conList.get(position).getBuilding());
        mRef.child("reports").child(user).child("city").setValue(conList.get(position).getCity());
        mRef.child("reports").child(user).child("date").setValue(conList.get(position).getDate());
        mRef.child("reports").child(user).child("hours1").setValue(conList.get(position).getHours1());
        mRef.child("reports").child(user).child("hours2").setValue(conList.get(position).getHours2());
        mRef.child("reports").child(user).child("hours3").setValue(conList.get(position).getHours3());
        mRef.child("reports").child(user).child("hours4").setValue(conList.get(position).getHours4());
        mRef.child("reports").child(user).child("id").setValue(conList.get(position).getId());
        mRef.child("reports").child(user).child("image").setValue(conList.get(position).getImage());
        mRef.child("reports").child(user).child("state").setValue(conList.get(position).getState());
        mRef.child("reports").child(user).child("tickets").setValue(conList.get(position).getTickets());
        mRef.child("reports").child(user).child("title").setValue(conList.get(position).getTitle());
        mRef.child("reports").child(user).child("url").setValue(conList.get(position).getUrl());
        mRef.child("reports").child(user).child("zip").setValue(conList.get(position).getZip());
        mRef.child("reports").child(user).child("latitude").setValue(conList.get(position).getLatitude());
        mRef.child("reports").child(user).child("longitude").setValue(conList.get(position).getLongitude());
        mRef.child("reports").child(user).child("likes").setValue(conList.get(position).getLikes());
        mRef.child("reports").child(user).child("attending").setValue(conList.get(position).getAttending());
        mRef.child("reports").child(user).child("admin").setValue(conList.get(position).getAdmin());
        mRef.child("reports").child(user).child("report").setValue(reportedText + "@" + timeStamp);
        mRef.child("reports").child(user).child("conid").setValue(conList.get(position).getConid());

        //ADD TO HISTORY
        mRef.child("history").child(conTitle).child("address").setValue(conList.get(position).getAddress());
        mRef.child("history").child(conTitle).child("building").setValue(conList.get(position).getBuilding());
        mRef.child("history").child(conTitle).child("city").setValue(conList.get(position).getCity());
        mRef.child("history").child(conTitle).child("date").setValue(conList.get(position).getDate());
        mRef.child("history").child(conTitle).child("hours1").setValue(conList.get(position).getHours1());
        mRef.child("history").child(conTitle).child("hours2").setValue(conList.get(position).getHours2());
        mRef.child("history").child(conTitle).child("hours3").setValue(conList.get(position).getHours3());
        mRef.child("history").child(conTitle).child("hours4").setValue(conList.get(position).getHours4());
        mRef.child("history").child(conTitle).child("id").setValue(conList.get(position).getId());
        mRef.child("history").child(conTitle).child("image").setValue(conList.get(position).getImage());
        mRef.child("history").child(conTitle).child("state").setValue(conList.get(position).getState());
        mRef.child("history").child(conTitle).child("tickets").setValue(conList.get(position).getTickets());
        mRef.child("history").child(conTitle).child("title").setValue(conList.get(position).getTitle());
        mRef.child("history").child(conTitle).child("url").setValue(conList.get(position).getUrl());
        mRef.child("history").child(conTitle).child("zip").setValue(conList.get(position).getZip());
        mRef.child("history").child(conTitle).child("latitude").setValue(conList.get(position).getLatitude());
        mRef.child("history").child(conTitle).child("longitude").setValue(conList.get(position).getLongitude());
        mRef.child("history").child(conTitle).child("likes").setValue(conList.get(position).getLikes());
        mRef.child("history").child(conTitle).child("attending").setValue(conList.get(position).getAttending());
        mRef.child("history").child(conTitle).child("admin").setValue(conList.get(position).getAdmin());
        mRef.child("history").child(conTitle).child("conid").setValue(conList.get(position).getConid());
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_main:
                Intent mainIntent = new Intent(this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
                break;
            case R.id.nav_map:
                Intent mapIntent = new Intent(this, MapActivity.class);
                mapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mapIntent);
                break;
            case R.id.nav_fav:
                Intent favIntent = new Intent(this, FavoriteActivity.class);
                favIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(favIntent);
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
                Intent histIntent = new Intent(this, MainActivity.class);
                histIntent.putExtra("HIST_BOOL", true);
                histIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(histIntent);
                break;

            case R.id.nav_admin_comments:
                Intent commentIntent = new Intent(this, CommentsActivity.class);
                commentIntent.putExtra("CON_TITLE", "Admin Reports");
                commentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(commentIntent);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_detail);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Con Detail Listeners
    @Override
    public void likeCon(boolean like, String title) {
        dataChange = true;
        conList = DataCache.loadConData(this);
        SharedPreferences deletePref = getSharedPreferences("DELETE_DATA" ,getApplication().MODE_PRIVATE);
        String obj = deletePref.getString("DELETE_KEY", null);
        int numLikes = userLikedList.size();
        if (like){
            numLikes++;
            //adds count to database
            mRef.child("conventions").child(obj).child("likes").setValue(numLikes);
            //adds user to like list
            mLikeRef.child("likes").child(conList.get(position).getConid())
                    .child(mAuth.getUid()).child("username").setValue(mAuth.getCurrentUser().getDisplayName());
            likesList = DataCache.loadUserConData(this, FILE_LIKES);
            likesList.add(title);
            DataCache.saveUserConData(this, likesList, FILE_LIKES);
            Toast.makeText(this, "You Liked " + title, Toast.LENGTH_SHORT).show();
        } else{
            numLikes--;
            mRef.child("conventions").child(obj).child("likes").setValue(numLikes);
            mRef.child("likes").child(conList.get(position).getConid()).child(mAuth.getUid()).removeValue();
            likesList = DataCache.loadUserConData(this, FILE_LIKES);
            likesList.remove(title);
            DataCache.saveUserConData(this, likesList, FILE_LIKES);
            Toast.makeText(this, "You Unliked " + title, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void favCon(boolean fav, String title) {
        posListMaker();
        favList = DataCache.loadFavConData(this);
        ArrayList<Con> c = conList;
        if (fav){
            Con con = new Con(c.get(position).getAddress(),  c.get(position).getBuilding(), c.get(position).getCity(), c.get(position).getDate(),
                    c.get(position).getHours1(), c.get(position).getHours2(), c.get(position).getHours3(), c.get(position).getHours4(),
                    c.get(position).getId(), c.get(position).getImage(), c.get(position).getLatitude(), c.get(position).getLongitude(),
                    c.get(position).getState(), c.get(position).getTickets(), c.get(position).getTitle(), c.get(position).getUrl(),
                    c.get(position).getZip(), c.get(position).getDistance(), c.get(position).getLikes(), c.get(position).getAdmin(), c.get(position).getAttending(), c.get(position).getConid());
            favList.add(con);
            DataCache.saveFavConData(this, favList);
            Toast.makeText(this, "Added " + title + "To Favorites", Toast.LENGTH_SHORT).show();
        } else {
            //get the current position of item in favList
            int pos = getConPos(title);
            favList.remove(pos);
            DataCache.saveFavConData(this, favList);
            Toast.makeText(this, "Removed " + title + "From Favorites", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void attendCon(boolean attend, String title) {
        dataChange = true;
        conList = DataCache.loadConData(this);
        SharedPreferences deletePref = getSharedPreferences("DELETE_DATA" ,getApplication().MODE_PRIVATE);
        String obj = deletePref.getString("DELETE_KEY", null);
        int numAttends = userAttendingList.size();
        if (attend){
            numAttends++;
            //adds count to database
            mRef.child("conventions").child(obj).child("attending").setValue(numAttends);
            //adds user to attend list
            mRef.child("attending").child(conList.get(position).getConid())
                    .child(mAuth.getUid()).child("username").setValue(mAuth.getCurrentUser().getDisplayName());

            attendList = DataCache.loadUserConData(this, FILE_ATTEND);
            attendList.add(title);
            DataCache.saveUserConData(this, attendList, FILE_ATTEND);

            Toast.makeText(this, "Your Attending " + title, Toast.LENGTH_SHORT).show();
        } else{
            numAttends--;
            mRef.child("conventions").child(obj).child("attending").setValue(numAttends);
            mRef.child("attending").child(conList.get(position).getConid()).child(mAuth.getUid()).removeValue();

            attendList = DataCache.loadUserConData(this, FILE_ATTEND);
            attendList.remove(title);
            DataCache.saveUserConData(this, attendList, FILE_ATTEND);

            Toast.makeText(this, "No longer attending " + title, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void tickets() {
        //Webview to tickets Url
        Uri uriUrl = Uri.parse(conList.get(position).getTickets());
        boolean isValid = URLUtil.isValidUrl(String.valueOf(uriUrl));
        if (isValid){
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
        }
    }

    @Override
    public void conURL() {
        //Webview to Con Url
        Uri uriUrl = Uri.parse(conList.get(position).getUrl());
        boolean isValid = URLUtil.isValidUrl(String.valueOf(uriUrl));
        if (isValid){
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
        }
    }

    @Override
    public void directions() {
        //open directions to location using Lat and Lng
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + conList.get(position).getLatitude() + "," + conList.get(position).getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    @Override
    public void comment() {
        //Nav to comments List
        Intent commentIntent = new Intent(this, CommentsActivity.class);
        commentIntent.putExtra("CON_TITLE", conList.get(position).getTitle());
        commentIntent.putExtra("CON_ID", conList.get(position).getConid());
        commentIntent.putExtra("SCREEN_TYPE", "comments");
        startActivity(commentIntent);
    }

    @Override
    public void likesListNav() {
        final Intent intent = new Intent(this, CommentsActivity.class);
        intent.putExtra("SCREEN_TYPE", "likes");
        intent.putExtra("COM_LIST", userLikedList);
        intent.putExtra("CON_ID", conList.get(position).getConid());
        intent.putExtra("CON_TITLE", conList.get(position).getTitle());
        startActivity(intent);
    }

    @Override
    public void attendingListNav() {
        Intent intent = new Intent(this, CommentsActivity.class);
        intent.putExtra("SCREEN_TYPE", "attending");
        intent.putExtra("COM_LIST", userAttendingList);
        intent.putExtra("CON_ID", conList.get(position).getConid());
        intent.putExtra("CON_TITLE", conList.get(position).getTitle());
        startActivity(intent);
    }

    private void posListMaker(){
        posList = new ArrayList<>();
        favList = DataCache.loadFavConData(this);
        for (int i=0; i<favList.size(); i++){
            String title = favList.get(i).getTitle();
            posList.add(title);
        }
    }

    private int getConPos(String con) {
        for (int i = 0; i < posList.size(); i++) {
            if (con.equals(posList.get(i))){
                return i;
            }
        }
        return -1;
    }

    private int getConPosition(String con, ArrayList<Con> mList) {
        for (int i = 0; i < mList.size(); i++) {
            if (con.equals(mList.get(i).getTitle())){
                return i;
            }
        }
        return -1;
    }

    private void setupMenu(){
        //Menu Items
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        DrawerLayout drawer = findViewById(R.id.drawer_layout_detail);
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

    public void reUploadNewConData(Con con) {
        //Get long and lat from address
        LatLng latLng = DataHelper.getLocationFromAddress(this,con.getAddress() + " " + con.getCity() + ", " + con.getState() + " " + con.getZip());

        double lat = latLng.latitude;
        double lng = latLng.longitude;

        String conTitle = conList.get(position).getConid();
        mRef.child("conventions").child(conTitle).child("address").setValue(con.getAddress());
        mRef.child("conventions").child(conTitle).child("building").setValue(con.getBuilding());
        mRef.child("conventions").child(conTitle).child("city").setValue(con.getCity());
        mRef.child("conventions").child(conTitle).child("date").setValue(con.getDate());
        mRef.child("conventions").child(conTitle).child("hours1").setValue(con.getHours1());
        mRef.child("conventions").child(conTitle).child("hours2").setValue(con.getHours2());
        mRef.child("conventions").child(conTitle).child("hours3").setValue(con.getHours3());
        mRef.child("conventions").child(conTitle).child("hours4").setValue(con.getHours4());
        mRef.child("conventions").child(conTitle).child("id").setValue(con.getId());
        mRef.child("conventions").child(conTitle).child("image").setValue(con.getImage());
        mRef.child("conventions").child(conTitle).child("state").setValue(con.getState());
        mRef.child("conventions").child(conTitle).child("tickets").setValue(con.getTickets());
        mRef.child("conventions").child(conTitle).child("title").setValue(con.getTitle());
        mRef.child("conventions").child(conTitle).child("url").setValue(con.getUrl());
        mRef.child("conventions").child(conTitle).child("zip").setValue(con.getZip());
        mRef.child("conventions").child(conTitle).child("latitude").setValue(lat);
        mRef.child("conventions").child(conTitle).child("longitude").setValue(lng);
        mRef.child("conventions").child(conTitle).child("likes").setValue(con.getLikes());
        mRef.child("conventions").child(conTitle).child("attending").setValue(con.getAttending());
        mRef.child("conventions").child(conTitle).child("admin").setValue(con.getAdmin());
        mRef.child("conventions").child(conTitle).child("conid").setValue(con.getConid());

        //Send Report to Admin of new Cons
        //ADD TO REPORT
        String user = currentUser.getDisplayName() + "-" + con.getTitle() + "-" + DataHelper.timeStamp();
        mRef.child("reports").child(user).child("address").setValue(con.getAddress());
        mRef.child("reports").child(user).child("building").setValue(con.getBuilding());
        mRef.child("reports").child(user).child("city").setValue(con.getCity());
        mRef.child("reports").child(user).child("date").setValue(con.getDate());
        mRef.child("reports").child(user).child("hours1").setValue(con.getHours1());
        mRef.child("reports").child(user).child("hours2").setValue(con.getHours2());
        mRef.child("reports").child(user).child("hours3").setValue(con.getHours3());
        mRef.child("reports").child(user).child("hours4").setValue(con.getHours4());
        mRef.child("reports").child(user).child("id").setValue(con.getId());
        mRef.child("reports").child(user).child("image").setValue(con.getImage());
        mRef.child("reports").child(user).child("state").setValue(con.getState());
        mRef.child("reports").child(user).child("tickets").setValue(con.getTickets());
        mRef.child("reports").child(user).child("title").setValue(con.getTitle());
        mRef.child("reports").child(user).child("url").setValue(con.getUrl());
        mRef.child("reports").child(user).child("zip").setValue(con.getZip());
        mRef.child("reports").child(user).child("latitude").setValue(lat);
        mRef.child("reports").child(user).child("longitude").setValue(lng);
        mRef.child("reports").child(user).child("likes").setValue(con.getLikes());
        mRef.child("reports").child(user).child("attending").setValue(con.getAttending());
        mRef.child("reports").child(user).child("admin").setValue(con.getAdmin());
        mRef.child("reports").child(user).child("report").setValue("Re Added to list");
        mRef.child("reports").child(user).child("report").setValue("Re Added " + con.getTitle() + " to list." + " " + DataHelper.dateNotifyStamp());
        mRef.child("reports").child(user).child("conid").setValue(con.getConid());


        Toast.makeText(this, con.getTitle() + " added back to Main List", Toast.LENGTH_SHORT).show();

        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);

    }


}
