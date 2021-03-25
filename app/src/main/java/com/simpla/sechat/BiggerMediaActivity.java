package com.simpla.sechat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.simpla.sechat.Extensions.PreferencesHelper;
import com.simpla.sechat.Extensions.UniversalImageLoader;
import com.universalvideoview.UniversalVideoView;

public class BiggerMediaActivity extends AppCompatActivity {

    private String filePath,type;
    private ImageView image;
    private ProgressBar progressBar;
    private UniversalVideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesHelper.setTheme(BiggerMediaActivity.this);
        setContentView(R.layout.activity_bigger_media);
        getIntentData();
    }

    private void getIntentData(){
        Intent intent = getIntent();
        filePath = intent.getStringExtra("biggerMediaFilePath");
        type = intent.getStringExtra("biggerMediaType");
        findIds();
    }

    private void findIds(){
        ImageView back = findViewById(R.id.bgm_back);
        image = findViewById(R.id.bgm_image);
        progressBar = findViewById(R.id.bgm_progress);
        videoView = findViewById(R.id.bgm_video);
        back.setOnClickListener(v -> {
            videoView.stopPlayback();
            onBackPressed();
        });
        loadData();
    }

    private void loadData() {
        if(type != null){
            if(type.equals("image") || type.equals("photo")){
                videoView.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
                UniversalImageLoader.setImage(filePath,image,progressBar,"");
            }else{
                videoView.setVisibility(View.VISIBLE);
                image.setVisibility(View.GONE);
                videoView.setVideoURI(Uri.parse(filePath));
                videoView.start();
            }
        }else{
            videoView.setVisibility(View.GONE);
            image.setVisibility(View.VISIBLE);
            image.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.loading_picture,getTheme()));
        }
    }

}