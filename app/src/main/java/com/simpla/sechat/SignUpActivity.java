package com.simpla.sechat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.simpla.sechat.Extensions.AuthHelper;
import com.simpla.sechat.Extensions.EventBusDataEvents;
import com.simpla.sechat.Extensions.PreferencesHelper;
import com.simpla.sechat.Extensions.RunTimePermissions;
import com.simpla.sechat.Extensions.UniversalImageLoader;
import com.simpla.sechat.Objects.UserObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends RunTimePermissions {

    private EditText name,email,pass;
    private Button signUp;
    private TextView back;
    private CircleImageView imageView;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 100;
    private Uri chosenPhotoUri = null;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesHelper.setTheme(SignUpActivity.this);
        setContentView(R.layout.activity_sign_up);
        findIds();
    }

    private void findIds() {
        mAuth = FirebaseAuth.getInstance();
        listener = new AuthHelper().AuthListener(SignUpActivity.this);
        name = findViewById(R.id.su_name);
        email = findViewById(R.id.su_email);
        pass = findViewById(R.id.su_password);
        signUp = findViewById(R.id.su_button);
        back = findViewById(R.id.su_back);
        imageView = findViewById(R.id.su_image);
        setListeners();
    }

    private void setListeners(){
        back.setOnClickListener(view -> onBackPressed());
        signUp.setOnClickListener(view -> signUpProcess());
        imageView.setOnClickListener(view -> {
            if(Build.VERSION.SDK_INT>=23){
                String[] permissions = new String[2];
                boolean check = false;
                if(ContextCompat.checkSelfPermission(
                        SignUpActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED){
                    permissions[0] = Manifest.permission.READ_EXTERNAL_STORAGE;
                    check = true;
                }
                if(ContextCompat.checkSelfPermission(
                        SignUpActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED){
                    permissions[1] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                    check = true;
                }
                if(check) askPermission(permissions);
                else pickPhoto();
            }else{
                pickPhoto();
            }
        });
    }

    private void signUpProcess() {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        String n = name.getText().toString();
        String e = email.getText().toString();
        String p = pass.getText().toString();
        if(!n.isEmpty() && !e.isEmpty() && !p.isEmpty()){
            if(n.length() < 2){ //name cannot be equal or shorter than 1 character
                Toast.makeText(SignUpActivity.this
                        , getResources().getString(R.string.short_name), Toast.LENGTH_SHORT).show();
            }else if(!e.contains("@")){ //invalid email
                Toast.makeText(SignUpActivity.this
                        , getResources().getString(R.string.invalid_mail), Toast.LENGTH_SHORT).show();
            }else if(p.length() < 6){ //password cannot be shorter than 6 character
                Toast.makeText(SignUpActivity.this
                        , getResources().getString(R.string.short_pass), Toast.LENGTH_SHORT).show();
            }else{ //inputs are good
                mRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            boolean check = false;
                            for(DataSnapshot d: snapshot.getChildren()){
                                if(String.valueOf(d.child("email").getValue()).equals(e)){
                                    check = true;
                                    break;
                                }
                            }
                            if(!check) saveNewUser(e,p);
                            else Toast.makeText(SignUpActivity.this
                                        , getResources().getString(R.string.email_exist), Toast.LENGTH_SHORT).show();
                        }else saveNewUser(e,p); //There is no user, save new user
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SignUpActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }else Toast.makeText(SignUpActivity.this
                , getResources().getString(R.string.fill_blanks), Toast.LENGTH_SHORT).show();
    }

    private void saveNewUser(String mail,String password) {
        String default_pp = "https://firebasestorage.googleapis.com/v0/b/sechat-94dc7.appspot.com/o/profilephotos%2Fdefault_profile_picture.jpg?alt=media&token=effb0773-42e1-496e-b8a9-07d87e197d02";
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(mail,password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("users");
                assert FirebaseAuth.getInstance().getCurrentUser() != null;
                String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if(chosenPhotoUri == null){
                    UserObject object = new UserObject(name.getText().toString(), mail, default_pp);
                    completeRegistration(mRef,key,object);
                }else{
                    final StorageReference sRef = FirebaseStorage.getInstance().getReference().child("profilephotos")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    UploadTask uploadTask = sRef.putFile(chosenPhotoUri);
                    uploadTask.continueWithTask(task12 ->
                            sRef.getDownloadUrl()).addOnCompleteListener(task13 -> {
                        Uri downloadURL = task13.getResult();
                        UserObject object;
                        if (downloadURL != null)
                            object = new UserObject(name.getText().toString(), mail, downloadURL.toString());
                        else object = new UserObject(name.getText().toString(), mail, default_pp);
                        long tsLong = System.currentTimeMillis()/1000;
                        String ts = String.valueOf(tsLong);
                        String code = key.substring(0,5) + ts.substring(0,5);
                        object.setCode(code);
                        completeRegistration(mRef,key,object);
                    });
                }
            }else Toast.makeText(SignUpActivity.this
                        , getResources().getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
        });
    }

    private void completeRegistration(DatabaseReference mRef,String key, UserObject object){
        mRef.child(key).setValue(object).addOnCompleteListener(task1 -> {
            if(task1.isSuccessful()){
                SharedPreferences settings = getApplicationContext().getSharedPreferences("SeChat_settings",0);
                SharedPreferences.Editor editor = settings.edit();
                new PreferencesHelper().newSave(editor,SignUpActivity.this);
                startActivity(new Intent(SignUpActivity.this,NavigateActivity.class));
            }else{
                Toast.makeText(SignUpActivity.this
                        , getResources().getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
                assert FirebaseAuth.getInstance().getCurrentUser() != null;
                FirebaseAuth.getInstance().getCurrentUser().delete();
            }
        });
    }

    private void askPermission(String[] permissions){
        SignUpActivity.super.askPermission(permissions, READ_EXTERNAL_STORAGE_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void permissionGranted(int requestCode) {
        if(READ_EXTERNAL_STORAGE_REQUEST_CODE == requestCode){
            pickPhoto();
        }
    }

    public void pickPhoto(){
        startActivity(new Intent(SignUpActivity.this,GalleryActivity.class)
                .putExtra("activity",1));
    }

    @Subscribe
    public void EventBusTakeImage(EventBusDataEvents.sendUri uri){
        chosenPhotoUri = uri.getUri();
        String path = uri.getPath();
        if(path != null)
            UniversalImageLoader.setImage(path,imageView,null,"file:/");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(listener);
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if(listener != null) mAuth.removeAuthStateListener(listener);
    }

}