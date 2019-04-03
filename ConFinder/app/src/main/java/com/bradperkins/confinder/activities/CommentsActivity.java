package com.bradperkins.confinder.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.fragments.CommentsFragment;
import com.bradperkins.confinder.objects.Comments;
import com.bradperkins.confinder.objects.Con;
import com.bradperkins.confinder.objects.ConReports;
import com.bradperkins.confinder.utils.DataCache;
import com.bradperkins.confinder.utils.DataHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class CommentsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener , CommentsFragment.CommentListener{

    private FirebaseUser currentUser;

    private DatabaseReference mRef;

    private Comments comments;
    private ConReports conReports;
    private ArrayList<Comments> commentsList;
    private ArrayList<ConReports> reportsList;

    private String conTitle;
    private String conId;
    private String screenType;
    private boolean inActivity = false;


    private SharedPreferences deletePref;
    private String notInList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        inActivity = true;

        deletePref = getSharedPreferences("DELETE_DATA" ,getApplication().MODE_PRIVATE);

        final CommentsFragment frag = new CommentsFragment();

        ArrayList<Con> conList = DataCache.loadConData(this);

        ArrayList<Comments> cList = new ArrayList<>();

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            //pull list and position
            conTitle = getIntent().getStringExtra("CON_TITLE");
            conId = getIntent().getStringExtra("CON_ID");
            screenType = getIntent().getStringExtra("SCREEN_TYPE");
            cList = (ArrayList<Comments>) getIntent().getSerializableExtra("COM_LIST");
        }


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();

        if (commentsList == null) {
            commentsList = new ArrayList<>();
        }

        if (reportsList == null) {
            reportsList = new ArrayList<>();
        }

        //if coming wanting admin reports
        if (conTitle.equals("Admin Reports")){
            mRef = mFirebaseDatabase.getReference();
            mRef.child("reports").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    reportsList.clear();
                    conReports = new ConReports();
                    reportsList = new ArrayList<>();
                    for (final DataSnapshot consSnapshot : dataSnapshot.getChildren()) {
                        conReports = consSnapshot.getValue(ConReports.class);
                        reportsList.add(conReports);
                    }
                    if (!inActivity) {
                        Log.d("Comments.TAG", "Not In activity");
                    } else {
                        for (int i = 0; i < reportsList.size(); i++) {
                            String title = reportsList.get(i).getTitle();
                            String report = reportsList.get(i).getReport();

                            String segments[] = new String[0];
                            if (segments != null) {
                                segments = report.split("@");

                                String time = segments[segments.length - 1];
                                String reportMsg = segments[segments.length - 2];

                                String timeDate = time.substring(0, Math.min(time.length(), 6));
                                String timeDateFormat = DataHelper.dateFormatter(Double.parseDouble(timeDate));

                                commentsList.add(new Comments(title + "  " + timeDateFormat, time, reportMsg));
                            }
                        }

                        Comparator comp = new Comparator<Comments>() {
                            public int compare(Comments o2, Comments o1) {
                                return (int) (Integer.parseInt(o1.getPosted()) - Integer.parseInt(o2.getPosted()));
                            }
                        };
                        if (commentsList != null) {
                            Collections.sort(commentsList, comp);
                        }

                        getFragmentManager().beginTransaction().replace(R.id.comments_placeholder, frag.newInstance(commentsList, "reports", conTitle)).commit();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }

            });

//            for (int i = 0; i < reportsList.size(); i++) {
//                String title = reportsList.get(i).getTitle();
//                String report = reportsList.get(i).getReport();
//
//                String segments[] = new String[0];
////                            if (segments != null) {
//                segments = report.split("@");
//
//                String time = segments[segments.length - 1];
//                String reportMsg = segments[segments.length - 2];
//
//                String timeDate = time.substring(0, Math.min(time.length(), 6));
//                String timeDateFormat = DataHelper.dateFormatter(Double.parseDouble(timeDate));
//
//                commentsList.add(new Comments(title + "  " + timeDateFormat, time, reportMsg));
////                            }
//            }
//
//
//            //change time to int
//
//            Comparator comp = new Comparator<Comments>() {
//                public int compare(Comments o2, Comments o1) {
//                    return (int) (Integer.parseInt(o1.getPosted()) - Integer.parseInt(o2.getPosted()));
//                }
//            };
//            if (commentsList != null) {
//                Collections.sort(commentsList, comp);
//            }




//            getFragmentManager().beginTransaction().replace(R.id.comments_placeholder, frag.newInstance(commentsList, "reports", conTitle)).commit();
        }

        else if (screenType.equals("comments")){
            //Pull Comment Data
            mRef = mFirebaseDatabase.getReference();
            mRef.child("comments").child(conId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    commentsList.clear();
                    comments = new Comments();
                    commentsList = new ArrayList<>();
                    for (final DataSnapshot consSnapshot : dataSnapshot.getChildren()) {
                        comments = consSnapshot.getValue(Comments.class);
                        commentsList.add(comments);
                    }
                    getFragmentManager().beginTransaction().replace(R.id.comments_placeholder, frag.newInstance(commentsList, "comments", conTitle)).commit();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else if (screenType.equals("likes")){
            getFragmentManager().beginTransaction().replace(R.id.comments_placeholder, frag.newInstance(cList, "likes", conTitle)).commit();

        } else if (screenType.equals("attending")){

            getFragmentManager().beginTransaction().replace(R.id.comments_placeholder, frag.newInstance(cList, "attending", conTitle)).commit();
        }

        createMenu();
    }

    @Override
    protected void onStop() {
        super.onStop();
        inActivity = false;
    }

    private void createMenu() {
        //Menu items
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        DrawerLayout drawer = findViewById(R.id.drawer_layout_comments);
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

        SharedPreferences adminPref = getSharedPreferences("ADMIN_DATA" ,this.MODE_PRIVATE);
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout_comments);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.report:
//                Toast.makeText(this, "Comment reported", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout_comments);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void addComment(String comment) {
        //Get current time stamp
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String messageTime = new SimpleDateFormat("MM/dd/yyyy hh:mm a").format(new Date());

        mRef.child("comments").child(conId).child(timeStamp).child("username").setValue(currentUser.getDisplayName());
        mRef.child("comments").child(conId).child(timeStamp).child("posted").setValue(messageTime);
        mRef.child("comments").child(conId).child(timeStamp).child("message").setValue(comment);
    }

    @Override
    public void conPosition(int pos, ArrayList<Comments> conList) {

        if (conTitle.equals("Admin Reports")){
            //get obj
            DataHelper.getChild(conList.get(pos).getUsername(), this);
            String actualObj = deletePref.getString("DELETE_KEY", null);

            Log.d("XXXXX", "OBJ - " + actualObj + " Not in- " + notInList);

        }



    }

}
