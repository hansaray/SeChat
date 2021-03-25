package com.simpla.sechat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.simpla.sechat.Extensions.AuthHelper;
import com.simpla.sechat.Extensions.PreferencesHelper;
import com.simpla.sechat.Extensions.UniversalImageLoader;

public class MainActivity extends AppCompatActivity {

    private Button login, signUp;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesHelper.setTheme(MainActivity.this);
        setContentView(R.layout.activity_main);
        findIds();
        initImageLoader();
    }

    private void findIds(){
        mAuth = FirebaseAuth.getInstance();
        listener = new AuthHelper().AuthListener(MainActivity.this);
        login = findViewById(R.id.login);
        signUp = findViewById(R.id.signUp);
        setListeners();
    }

    private void setListeners(){
        login.setOnClickListener(view -> startActivity(new Intent(MainActivity.this,LoginActivity.class)));
        signUp.setOnClickListener(view -> startActivity(new Intent(MainActivity.this,SignUpActivity.class)));
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(getApplicationContext());
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listener != null) mAuth.removeAuthStateListener(listener);
    }
}