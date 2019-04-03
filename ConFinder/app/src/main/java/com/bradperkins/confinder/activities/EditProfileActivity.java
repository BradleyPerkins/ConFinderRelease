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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.fragments.EditProfileFragment;
import com.bradperkins.confinder.objects.Con;
import com.bradperkins.confinder.utils.DataHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class EditProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ArrayList<Con> conList;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (conList == null){
            conList = new ArrayList<>();
        }

        getFragmentManager().beginTransaction().replace(R.id.edit_profile_placeholder, EditProfileFragment.newInstance()).commit();


        setupMenu();
    }

    private void setupMenu() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        DrawerLayout drawer = findViewById(R.id.drawer_layout_edit_profile);
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout_edit_profile);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent profileIntent = new Intent(this, ProfileActivity.class);
            profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(profileIntent);
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_profile, menu);
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
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
            case R.id.nav_fav:
                //Reload Activity with Favorite con file

                break;
            case R.id.nav_map:
                Intent mapIntent = new Intent(this, MapActivity.class);
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
                startActivity(commentIntent);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_edit_profile);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



}
