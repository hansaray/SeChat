package com.simpla.sechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simpla.sechat.Extensions.AuthHelper;
import com.simpla.sechat.Extensions.PreferencesHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText email,pass;
    private Button login;
    private TextView forgot,back;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesHelper.setTheme(LoginActivity.this);
        setContentView(R.layout.activity_login);
        findIds();
    }

    private void findIds(){
        mAuth = FirebaseAuth.getInstance();
        listener = new AuthHelper().AuthListener(LoginActivity.this);
        email =  findViewById(R.id.login_email);
        pass = findViewById(R.id.login_password);
        login = findViewById(R.id.login_button);
        forgot = findViewById(R.id.login_forgot);
        back = findViewById(R.id.login_back);
        progressBar = findViewById(R.id.login_progress);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.mainBlue),
                android.graphics.PorterDuff.Mode.MULTIPLY);
        setListeners();
    }

    private void setListeners(){
        login.setOnClickListener(view -> loginProcess());
        forgot.setOnClickListener(view -> {
            LayoutInflater layoutInflater = LayoutInflater.from(LoginActivity.this);
            View promptView = layoutInflater.inflate(R.layout.layout_forgot_alert, null);
            final AlertDialog alertD = new AlertDialog.Builder(LoginActivity.this).create();
            Button send = promptView.findViewById(R.id.f_alert_send);
            EditText txt = promptView.findViewById(R.id.f_alert_email);
            if(email.getText() != null && !email.getText().toString().isEmpty() && email.getText().toString().contains("@"))
                txt.setText(email.getText().toString());
            send.setOnClickListener(view1 -> {
                if(txt.getText() != null && !txt.getText().toString().isEmpty())
                    checkTheUser(txt.getText().toString());
                else
                    Toast.makeText(this, getResources().getString(R.string.fill_blanks), Toast.LENGTH_SHORT).show();
            });
            alertD.setView(promptView);
            alertD.show();
        });
        back.setOnClickListener(view -> onBackPressed());
    }

    private void loginProcess() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        String e = email.getText().toString();
        String p = pass.getText().toString();
        if(!e.isEmpty() && !p.isEmpty()){
            if(e.length() < 2) helper(getResources().getString(R.string.short_name));
            else if(p.length() < 6) helper(getResources().getString(R.string.short_pass));
            else{
                mRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            boolean check = false;
                            String final_address = "";
                            for(DataSnapshot d: snapshot.getChildren()){
                                if(String.valueOf(d.child("email").getValue()).equals(e)
                                        || String.valueOf(d.child("nickname").getValue()).equals(e)){
                                    final_address = String.valueOf(d.child("email").getValue());
                                    check = true;
                                    break;
                                }
                            }
                            if(check){
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(final_address,p)
                                        .addOnCompleteListener(task -> {
                                    if(task.isSuccessful()){
                                        new PreferencesHelper().existingSave(LoginActivity.this);
                                        startActivity(new Intent(LoginActivity.this,NavigateActivity.class));
                                    }else Toast.makeText(LoginActivity.this
                                            , getResources().getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                });
                            }else helper(getResources().getString(R.string.email_not_exist));
                        }else helper(getResources().getString(R.string.email_not_exist));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        helper(error.getMessage());
                    }
                });
            }
        }else helper(getResources().getString(R.string.fill_blanks));
    }

    private void helper(String txt){
        Toast.makeText(LoginActivity.this, txt, Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
    }

    //Forgot Password Part
    private void checkTheUser(final String e_address) {
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userExist = false;
                for(DataSnapshot d : snapshot.getChildren()){
                    if(d.child("email").exists() && String.valueOf(d.child("email").getValue()).equalsIgnoreCase(e_address)){
                        resetPassword(e_address);
                        userExist = true;
                        break;
                    }
                }
                if(!userExist){
                    Toast.makeText(LoginActivity.this
                            , getResources().getString(R.string.email_not_exist), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetPassword(String address){
        FirebaseAuth.getInstance().sendPasswordResetEmail(address).addOnCompleteListener(task -> {
            if(task.isSuccessful())
                Toast.makeText(LoginActivity.this
                        , getResources().getString(R.string.forgot_sent), Toast.LENGTH_LONG).show();
            else
                Toast.makeText(LoginActivity.this
                        , getResources().getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();

        });
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