package com.simpla.sechat;

import android.Manifest;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.simpla.sechat.Adapters.GalleryAdapter;
import com.simpla.sechat.Extensions.EventBusDataEvents;
import com.simpla.sechat.Extensions.GetPhotosVideos;
import com.simpla.sechat.Extensions.PreferencesHelper;
import com.simpla.sechat.Extensions.RunTimePermissions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends RunTimePermissions {

    private int type = 0;
    private ArrayList<String> folders = new ArrayList<>();
    private ArrayList<String> folderNames = new ArrayList<>();
    private Spinner spinner;
    private String chosenFilePath;
    private boolean isItaImage;
    private TextView camera;
    private ImageView close,next;
    private static final int REQUEST_CODE = 100;
    private RecyclerView recyclerView;
    private int a = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesHelper.setTheme(GalleryActivity.this);
        setContentView(R.layout.activity_gallery);
        getIntentData();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        type = intent.getIntExtra("activity",0);
        findIds();
    }

    private void findIds() {
        camera = findViewById(R.id.gallery_camera);
        recyclerView = findViewById(R.id.gallery_rw);
        close = findViewById(R.id.gallery_close);
        next = findViewById(R.id.gallery_next);
        spinner = findViewById(R.id.gallery_spinner);
        setSpinnerAndItsData();
    }

    private void setSpinnerAndItsData() {
        String root = Environment.getExternalStorageDirectory().getPath();
        String cameraPhotos = root + "/DCIM/Camera";
        String downloadPhotos = root + "/Download";
        String whatsappPhotos = root + "/whatsApp/Media/WhatsApp Images";
        String whatsappVideos = root + "/whatsApp/Media/WhatsApp Video";
        String screenshots = root + "/PICTURES/Screenshots";
        String twitterpics = root + "/PICTURES/Twitter";
        String instagrampics = root + "/PICTURES/Instagram";
        String messengerpics = root + "/PICTURES/Messenger";
        String sechatpics = root + "/DCIM/PetZone";
        String snapchatpics = root + "/Snapchat";

        folderNames.add(getResources().getString(R.string.all_media));
        folderNames.add(getResources().getString(R.string.all_videos));

        if(GetPhotosVideos.folderCheck(cameraPhotos)){
            folders.add(cameraPhotos);
            folderNames.add(getResources().getString(R.string.camera));
        }
        if(GetPhotosVideos.folderCheck(screenshots)){
            folders.add(screenshots);
            folderNames.add(getResources().getString(R.string.screenshots));
        }
        if(GetPhotosVideos.folderCheck(downloadPhotos)){
            folders.add(downloadPhotos);
            folderNames.add(getResources().getString(R.string.downloads));
        }
        if(GetPhotosVideos.folderCheck(sechatpics)){
            folders.add(sechatpics);
            folderNames.add(getResources().getString(R.string.app_name));
        }
        if(GetPhotosVideos.folderCheck(instagrampics)){
            folders.add(instagrampics);
            folderNames.add("Instagram");
        }
        if(GetPhotosVideos.folderCheck(messengerpics)){
            folders.add(messengerpics);
            folderNames.add("Messenger");
        }
        if(GetPhotosVideos.folderCheck(whatsappPhotos)){
            folders.add(whatsappPhotos);
            folderNames.add("WhatsApp Video");
        }
        if(GetPhotosVideos.folderCheck(whatsappVideos)){
            folders.add(whatsappVideos);
            folderNames.add("WhatsApp");
        }
        if(GetPhotosVideos.folderCheck(snapchatpics)){
            folders.add(snapchatpics);
            folderNames.add("Snapchat");
        }
        if(GetPhotosVideos.folderCheck(twitterpics)){
            folders.add(twitterpics);
            folderNames.add("Twitter");
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, folderNames);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                if(position == 0){
                    SetupRw(GetPhotosVideos.loadAllPhotos(getApplicationContext()));
                }else if(position == 1){
                    SetupRw(GetPhotosVideos.loadAllVideos(getApplicationContext()));
                }else{
                    SetupRw(GetPhotosVideos.GetFiles(folders.get(position-2)));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setListeners();
        takeAction();
    }

    private void setListeners() {
        close.setOnClickListener(v -> onBackPressed());
        camera.setOnClickListener(v -> {
            if(Build.VERSION.SDK_INT>=23){
                String[] requiredPermissions={Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
                GalleryActivity.super.askPermission(requiredPermissions,REQUEST_CODE);
            }else{
                goToCamera();
            }
        });
    }

    private void takeAction() {
        if(type == 1){ //SignUpActivity
            next.setOnClickListener(v -> {
                if(a == 1){
                    EventBus.getDefault().postSticky(new EventBusDataEvents.sendUri(
                            Uri.fromFile(new File(chosenFilePath)),chosenFilePath));
                    finish();
                }
            });
        }else if(type == 2){ //Send media in the chat
            next.setOnClickListener(v -> {
                if(a == 1){
                    EventBus.getDefault().postSticky(new EventBusDataEvents.sendMediaToMesssage(
                            chosenFilePath,isItaImage));
                    finish();
                }
            });
        }else if(type == 3){ //Change Picture
            next.setOnClickListener(v -> {
                if(a == 1){
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    if(mAuth.getCurrentUser() == null) return;
                    final StorageReference sRef = FirebaseStorage.getInstance().getReference().child("profilephotos")
                            .child(mAuth.getCurrentUser().getUid());
                    UploadTask uploadTask = sRef.putFile(Uri.fromFile(new File(chosenFilePath)));
                    uploadTask.continueWithTask(task12 ->
                            sRef.getDownloadUrl()).addOnCompleteListener(task13 -> {
                        Uri downloadURL = task13.getResult();
                        if(downloadURL != null)
                        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid())
                                .child("imageURL").setValue(downloadURL.toString());
                        finish();
                    });
                }
            });
        }
    }

    private void SetupRw(final ArrayList<String> files){
        GalleryAdapter adapter = new GalleryAdapter(GalleryActivity.this, files);
        recyclerView.setAdapter(adapter);
        GridLayoutManager layoutManager = new GridLayoutManager(GalleryActivity.this,4);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(30);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
        if(files.size() != 0){
            chosenFilePath = files.get(0);
            pictureOrVideo(chosenFilePath);
        }
    }

    private void pictureOrVideo(String filePath){
        String fileType = filePath.substring(filePath.lastIndexOf("."));
        if(!fileType.isEmpty()){
            if(fileType.equalsIgnoreCase(".mp4") || fileType.equalsIgnoreCase(".mov")){
                isItaImage = false;
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(this, Uri.parse("file://"+filePath));
                retriever.release();
            }else{
                isItaImage = true;
            }
        }
    }

    private void goToCamera(){
        startActivity(new Intent(GalleryActivity.this,CameraActivity.class).putExtra("activity", type));
    }

    @Override
    public void permissionGranted(int requestCode) {
        if(REQUEST_CODE == requestCode ){
            goToCamera();
        }
    }

    @Subscribe
    public void EventBusTakeImage(EventBusDataEvents.sendMediaPosition photoPosition){
        chosenFilePath = photoPosition.getPosition();
        if(chosenFilePath != null){
            a = 1;
            pictureOrVideo(chosenFilePath);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}