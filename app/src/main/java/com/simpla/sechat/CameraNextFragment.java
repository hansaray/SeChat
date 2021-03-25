package com.simpla.sechat;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.controls.Mode;
import com.simpla.sechat.Extensions.EventBusDataEvents;
import com.simpla.sechat.Extensions.UniversalImageLoader;
import com.universalvideoview.UniversalVideoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class CameraNextFragment extends Fragment {

    private String chosenFilePath = "";
    private Boolean isItaImage;
    private int activity;
    private ImageView imageView,backButton;
    private TextView sendButton;
    private CameraView cameraView;
    private UniversalVideoView universalVideoView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_camera_next,container,false);
        findIds(v);
        return v;
    }

    private void findIds(View v) {
        imageView = v.findViewById(R.id.camera_next_image);
        backButton = v.findViewById(R.id.camera_next_back);
        sendButton = v.findViewById(R.id.camera_next_send);
        cameraView = requireActivity().findViewById(R.id.camera_view);
        universalVideoView = v.findViewById(R.id.camera_next_video);
        smallSettings();
    }

    private void smallSettings() {
        if(activity == 1) sendButton.setText(getResources().getString(R.string.next));
        else sendButton.setText(getResources().getString(R.string.send));
        if(isItaImage){
            universalVideoView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            UniversalImageLoader.setImage(chosenFilePath,imageView,null,"file:/");
        }else{
            universalVideoView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            universalVideoView.setVideoURI(Uri.parse("file://"+chosenFilePath));
            universalVideoView.start();
        }
        setListeners();
    }

    private void setListeners() {
        backButton.setOnClickListener(v -> {
            requireActivity().findViewById(R.id.camera_frame_layout).setVisibility(View.GONE);
            cameraView.setVisibility(View.VISIBLE);
            if(isItaImage){
                requireActivity().findViewById(R.id.camera_photo_layout).setVisibility(View.VISIBLE);
                requireActivity().findViewById(R.id.camera_video_layout).setVisibility(View.GONE);
                cameraView.setMode(Mode.PICTURE);
            }else{
                requireActivity().findViewById(R.id.camera_photo_layout).setVisibility(View.GONE);
                requireActivity().findViewById(R.id.camera_video_layout).setVisibility(View.VISIBLE);
                cameraView.setMode(Mode.VIDEO);
            }
        });
        sendButton.setOnClickListener(v -> {
            if(activity==1){
                EventBus.getDefault().post(new EventBusDataEvents.sendMediaPosition(chosenFilePath));
                GalleryActivity activity = new GalleryActivity();
                activity.finish();
            }else{
                EventBus.getDefault().postSticky(new EventBusDataEvents.sendMediaToMesssage(chosenFilePath,isItaImage));
            }
            requireActivity().finish();
        });
    }


    @Subscribe(sticky = true)
    public void EventBusTakeImage(EventBusDataEvents.sendFile chosenMedia){
        chosenFilePath = chosenMedia.getPath();
        isItaImage = chosenMedia.isItImage();
    }

    @Subscribe(sticky = true)
    public void EventBusTakeUid(EventBusDataEvents.sendInfo info){
        activity = info.getActivity();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }
}
