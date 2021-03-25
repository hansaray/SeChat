package com.simpla.sechat.Extensions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.simpla.sechat.MainActivity;
import com.simpla.sechat.NavigateActivity;

public class AuthHelper {

    private Context mContext;
    private Activity activity;

    //Setting AutListener to check if there is an existing user already
    public FirebaseAuth.AuthStateListener AuthListener(Activity activity) {
        this.mContext = activity.getApplicationContext();
        this.activity = activity;
        SharedPreferences settings = mContext.getApplicationContext().getSharedPreferences("SeChat_settings",0);
        boolean userCheck = settings.getBoolean("loggedIn",false);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(userCheck){
            if(mAuth.getCurrentUser() != null){
                intentNavigate();
                return null;
            }else{
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("loggedIn",false);
                editor.apply();
                return firebaseAuth -> {
                    FirebaseUser myUser = FirebaseAuth.getInstance().getCurrentUser();
                    if(myUser != null){
                        intentNavigate();
                    }
                };
            }
        }else{
            return firebaseAuth -> {
                FirebaseUser myUser = FirebaseAuth.getInstance().getCurrentUser();
                if(myUser != null){
                    intentNavigate();
                }
            };
        }
    }

    private void intentNavigate(){//Jump to main page if there is an existing user
        fcmToken();
        activity.startActivity(new Intent(mContext, NavigateActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        activity.finish();
    }

    //––––––––––––––––––––––––––––––––––––––––––––––––
    //Setting FCM token
    private void fcmToken(){
        SharedPreferences settings = mContext.getApplicationContext().getSharedPreferences("SeChat_settings",0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("loggedIn",true);
        editor.apply();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(!task.isSuccessful()) return;
            String token = task.getResult();
            writeToStorage(token);
        });
    }

    private void writeToStorage(String token){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("fcm_token").setValue(token);
        }
    }
    //––––––––––––––––––––––––––––––––––––––––––––––––
    //Setting AuthListener to check if there is an existing user(Activities allowed only with user)
    public FirebaseAuth.AuthStateListener AuthListener2(Activity activity) {
        this.mContext = activity.getApplicationContext();
        this.activity = activity;
        SharedPreferences settings = mContext.getApplicationContext().getSharedPreferences("SeChat_settings",0);
        boolean userCheck = settings.getBoolean("loggedIn",false);
        SharedPreferences.Editor editor = settings.edit();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(userCheck){
            if(mAuth.getCurrentUser() == null){
                intentMain(editor);
            }
            return firebaseAuth -> {
                FirebaseUser myUser = FirebaseAuth.getInstance().getCurrentUser();
                if(myUser == null){
                    intentMain(editor);
                }
            };
        }else{
            return firebaseAuth -> {
                FirebaseUser myUser = FirebaseAuth.getInstance().getCurrentUser();
                if(myUser == null){
                    intentMain(editor);
                }
            };
        }
    }

    private void intentMain(SharedPreferences.Editor editor){
        editor.putBoolean("loggedIn",false);
        editor.apply();
        activity.startActivity(new Intent(mContext, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        activity.finish();
    }
}
