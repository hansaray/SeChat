package com.simpla.sechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simpla.sechat.Extensions.AuthHelper;
import com.simpla.sechat.Extensions.PreferencesHelper;
import com.simpla.sechat.Extensions.UniversalImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

public class NavigateActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseAuth mAuth;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private String name,code;
    private DatabaseReference mRef;
    private boolean darkCheck;
    public static Boolean activityControl = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        darkCheck = PreferencesHelper.setTheme(NavigateActivity.this);
        setContentView(R.layout.activity_navigate);
        handleIntents();
        findIds();
    }

    private void handleIntents(){
        Intent intent = getIntent();
        String uid = intent.getStringExtra("clickedUserId");
        if(uid != null)
            startActivity(new Intent(NavigateActivity.this,MessageActivity.class)
                    .putExtra("messageUid",uid)
                    .putExtra("notControl",1));
        if(intent.getExtras() == null)
            getSupportFragmentManager().beginTransaction().replace(R.id.navigate_frame,new ChatsFragment()).commit();

    }

    private void findIds() {
        mAuth = FirebaseAuth.getInstance();
        listener = new AuthHelper().AuthListener2(NavigateActivity.this);
        bottomNavigationView = findViewById(R.id.navigate_bnv);
        navigationView = findViewById(R.id.navigate_nv);
        drawerLayout = findViewById(R.id.navigate_drawer);
        if(mAuth.getCurrentUser() == null) return;
        mRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        handleDrawerNavigation();
        handleBottomNavigation();
    }

    private void handleDrawerNavigation() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout
                ,0,0);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        MenuItem menuItem = navigationView.getMenu().findItem(R.id.dm_dark);
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch aSwitch = (Switch) menuItem.getActionView().findViewById(R.id.dark_switch);
        aSwitch.setChecked(darkCheck);
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences settings = getApplicationContext().getSharedPreferences("SeChat_settings",0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("darkMode",isChecked);
            editor.apply();
            drawerLayout.closeDrawer(GravityCompat.START);
            recreate();
        });
        loadInfo();
    }

    private void loadInfo() {
        if(mAuth.getCurrentUser() == null) return;
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    name = String.valueOf(snapshot.child("nickname").getValue());
                    code = String.valueOf(snapshot.child("code").getValue());
                    String imageUrl = String.valueOf(snapshot.child("imageURL").getValue());
                    View layout = navigationView.inflateHeaderView(R.layout.layout_header);
                    if(layout != null){
                        CircleImageView image = layout.findViewById(R.id.header_image);
                        UniversalImageLoader.setImage(imageUrl,image,null,"");
                        image.setOnClickListener(view -> {
                            drawerLayout.closeDrawer(GravityCompat.START);
                            startActivity(new Intent(NavigateActivity.this,GalleryActivity.class)
                                    .putExtra("activity",3));
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NavigateActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.bm_chats);
        getSupportFragmentManager().beginTransaction().replace(R.id.navigate_frame,new ChatsFragment()).commit();
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch(item.getItemId()){
                case R.id.bm_chats:
                    getSupportFragmentManager().beginTransaction().replace(R.id.navigate_frame,new ChatsFragment()).commit();
                    return true;
                case R.id.bm_friends:
                    getSupportFragmentManager().beginTransaction().replace(R.id.navigate_frame,new FriendsFragment()).commit();
                    return true;
                default:
                    return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityControl = true;
        mAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        activityControl = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityControl = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityControl = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listener != null) mAuth.removeAuthStateListener(listener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            finishAffinity();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        LayoutInflater layoutInflater = LayoutInflater.from(NavigateActivity.this);
        View promptView = layoutInflater.inflate(R.layout.layout_alert, null);
        final AlertDialog alertD = new AlertDialog.Builder(NavigateActivity.this).create();
        TextView cTxt = promptView.findViewById(R.id.la_code);
        ConstraintLayout layout = promptView.findViewById(R.id.la_name_layout);
        alertD.setView(promptView);
        switch (item.getItemId()){
            case R.id.dm_name:
                cTxt.setVisibility(View.GONE);
                layout.setVisibility(View.VISIBLE);
                TextView nTxt = promptView.findViewById(R.id.la_name_txt);
                EditText edit = promptView.findViewById(R.id.la_name_edit);
                Button btn = promptView.findViewById(R.id.la_name_button);
                nTxt.setText(getResources().getString(R.string.current_name)+ " " +name);
                btn.setOnClickListener(view -> {
                    if(edit.getText() != null && edit.getText().toString().length() >= 2)
                        mRef.child("nickname").setValue(edit.getText().toString());
                    else Toast.makeText(this, getResources().getString(R.string.short_name), Toast.LENGTH_SHORT).show();
                });
                alertD.show();
                break;
            case R.id.dm_code:
                cTxt.setVisibility(View.VISIBLE);
                layout.setVisibility(View.GONE);
                cTxt.setText(code);
                alertD.show();
                break;
            case R.id.dm_logout:
                if(mAuth.getCurrentUser() != null) mAuth.signOut();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}