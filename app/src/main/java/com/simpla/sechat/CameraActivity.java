package com.simpla.sechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Mode;
import com.otaliastudios.cameraview.gesture.Gesture;
import com.otaliastudios.cameraview.gesture.GestureAction;
import com.simpla.sechat.Extensions.EventBusDataEvents;
import com.simpla.sechat.Extensions.PreferencesHelper;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private FrameLayout frameLayout;
    private ImageView capturePicture,captureVideo,videoOn,videoOff;
    private CameraView cameraView;
    //public static int orientation;
    private long startTime = 0L;
    Handler customHandler = new Handler(Looper.getMainLooper());
    long timeInMilliseconds = 0L;
    private TextView timer;
    private ConstraintLayout photoControl,videoControl;
    private Button cameraSide,cameraSideVideo;
    private String uid = "";
    private int a_type = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesHelper.setTheme(CameraActivity.this);
        setContentView(R.layout.activity_camera);
        if(checkCameraHardware(getApplicationContext()))
        getIntentData();
    }

    private void getIntentData(){
        Intent intent = getIntent();
        a_type = intent.getIntExtra("activity",0);
        uid = intent.getStringExtra("uid");
        findIds();
    }

    private void findIds(){
        frameLayout = findViewById(R.id.camera_frame_layout);
        capturePicture = findViewById(R.id.camera_capture_p);
        captureVideo = findViewById(R.id.camera_take_video);
        videoOn = findViewById(R.id.camera_c_video);
        videoOff = findViewById(R.id.camera_c_photo);
        timer = findViewById(R.id.camera_timer);
        videoControl = findViewById(R.id.camera_video_layout);
        photoControl = findViewById(R.id.camera_photo_layout);
        cameraSide = findViewById(R.id.camera_side_p);
        cameraSideVideo = findViewById(R.id.camera_side_video);
        cameraView = findViewById(R.id.camera_view);
        smallSettings();
        setListeners();
    }

    private void smallSettings(){
        cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM);
        cameraView.mapGesture(Gesture.TAP,GestureAction.AUTO_FOCUS);
        cameraView.setLifecycleOwner(this);
        cameraView.setMode(Mode.PICTURE);
        photoControl.setVisibility(View.VISIBLE);
        videoControl.setVisibility(View.GONE);
        if(a_type == 1){
            videoOn.setVisibility(View.GONE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListeners() {
        final File takenVideo = getOutputMediaFile(MEDIA_TYPE_VIDEO);
        capturePicture.setOnClickListener(
                v -> cameraView.takePicture()
        );
        captureVideo.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                assert takenVideo != null;
                cameraView.takeVideo(takenVideo);
                return true;
            }else if(event.getAction()==MotionEvent.ACTION_UP){
                cameraView.stopVideo();
                return true;
            }
            return false;
        });
        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                super.onPictureTaken(result);
                byte[] data = result.getData();
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null){
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    MediaTaken(pictureFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onVideoTaken(@NonNull VideoResult result) {
                super.onVideoTaken(result);
                MediaTaken(takenVideo);
            }

            @Override
            public void onVideoRecordingStart() {
                super.onVideoRecordingStart();
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread,0);
                timer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onVideoRecordingEnd() {
                super.onVideoRecordingEnd();
                customHandler.removeCallbacks(updateTimerThread);
                timer.setVisibility(View.GONE);
            }
        });
        videoOn.setOnClickListener(v -> {
            cameraView.setMode(Mode.VIDEO);
            photoControl.setVisibility(View.GONE);
            videoControl.setVisibility(View.VISIBLE);
        });
        videoOff.setOnClickListener(v -> {
            cameraView.setMode(Mode.PICTURE);
            photoControl.setVisibility(View.VISIBLE);
            videoControl.setVisibility(View.GONE);
        });
        cameraSide.setOnClickListener(new View.OnClickListener() {
            int i = 0;
            @Override
            public void onClick(View v) {
                if(i%2==0){
                    cameraView.setFacing(Facing.FRONT);
                }else{
                    cameraView.setFacing(Facing.BACK);
                }
                i++;
            }
        });
        cameraSideVideo.setOnClickListener(new View.OnClickListener() {
            int a = 0;
            @Override
            public void onClick(View v) {
                if(a%2==0){
                    cameraView.setFacing(Facing.FRONT);
                }else{
                    cameraView.setFacing(Facing.BACK);
                }
                a++;
            }
        });
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    private static File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()+"/DCIM/Camera");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }

    private void MediaTaken(File mediaFile) {
        if(mediaFile != null){
            String fileType = mediaFile.getAbsolutePath().substring(mediaFile.getAbsolutePath().lastIndexOf("."));
            cameraView.setVisibility(View.GONE);
            photoControl.setVisibility(View.GONE);
            videoControl.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);
            boolean isItImage = true;
            if(fileType.equalsIgnoreCase(".mp4")){
                isItImage = false;
            }
            EventBus.getDefault().postSticky(new EventBusDataEvents.sendFile(
                    mediaFile.getAbsolutePath(),isItImage));
            EventBus.getDefault().postSticky(new EventBusDataEvents.sendInfo(
                    uid,a_type));
            getSupportFragmentManager().beginTransaction().replace(R.id.camera_frame_layout,new CameraNextFragment()).commit();
        }
    }

    private Runnable updateTimerThread = new Runnable() {
        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        @Override
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis()-startTime;
            int secs = (int) (timeInMilliseconds/1000);
            int mins = secs/60;
            secs = secs % 60;
            //int milliseconds = (int) timeInMilliseconds % 1000;
            timer.setText(""+mins+":"+String.format("%02d",secs));
            customHandler.postDelayed(this,0);
        }
    };
}