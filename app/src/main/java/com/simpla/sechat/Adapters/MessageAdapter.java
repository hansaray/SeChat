package com.simpla.sechat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.simpla.sechat.BiggerLocationActivity;
import com.simpla.sechat.BiggerMediaActivity;
import com.simpla.sechat.Extensions.AESEncryption;
import com.simpla.sechat.Extensions.TimeConverter;
import com.simpla.sechat.Extensions.UniversalImageLoader;
import com.simpla.sechat.Objects.MessageObject;
import com.simpla.sechat.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageAdapterHolder>  {

    private Context mContext;
    private ArrayList<MessageObject> allMessages;

    public MessageAdapter(Context mContext, ArrayList<MessageObject> allMessages) {
        this.mContext = mContext;
        this.allMessages = allMessages;
    }

    @NonNull
    @Override
    public MessageAdapter.MessageAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View myView = null;
        if(viewType==1){
            myView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sender,parent,false);
        }else if(viewType==2){
            myView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_receiver,parent,false);
        }
        assert myView != null;
        return new MessageAdapter.MessageAdapterHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MessageAdapterHolder holder, int position) {
        holder.setData(allMessages.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        if(FirebaseAuth.getInstance().getCurrentUser() == null) return 2;
        if(allMessages.get(position).getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            return 1;
        }else{
            return 2;
        }
    }

    @Override
    public int getItemCount() {
        return allMessages.size();
    }

    public class MessageAdapterHolder extends RecyclerView.ViewHolder {
        private TextView textView,locationName,time;
        private ImageView imageView,playVideo;
        private ProgressBar progressBar;
        private ConstraintLayout locationLayout;

        public MessageAdapterHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.msg_txt);
            imageView = itemView.findViewById(R.id.msg_img);
            progressBar = itemView.findViewById(R.id.msg_progress);
            playVideo = itemView.findViewById(R.id.msg_video);
            locationLayout = itemView.findViewById(R.id.msg_location_layout);
            locationName = itemView.findViewById(R.id.msg_location_name);
            time = itemView.findViewById(R.id.msg_time);
        }

        private void setData(final MessageObject object) {
            time.setText(TimeConverter.getTimeAgoForShorterTime(object.getTime(),mContext));
            switch (object.getType()) {
                case "text":
                    String msg ="";
                    try {
                        msg = AESEncryption.decrypt(object.getMessage());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    textView.setText(msg);
                    imageView.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                    break;
                case "photo":
                    UniversalImageLoader.setImage(object.getMessage(), imageView, progressBar, "");
                    setListener(object);
                    break;
                case "video":
                    setListener(object);
                    try {
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                progressBar.setVisibility(View.VISIBLE);
                                playVideo.setVisibility(View.GONE);
                                String videoFilePath = object.getMessage();
                                Bitmap bitmap;
                                MediaMetadataRetriever mediaMetadataRetriever = null;
                                try {
                                    mediaMetadataRetriever = new MediaMetadataRetriever();
                                    mediaMetadataRetriever.setDataSource(videoFilePath, new HashMap<>());
                                    bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST);
                                } finally {
                                    if (mediaMetadataRetriever != null) {
                                        mediaMetadataRetriever.release();
                                    }
                                }
                                if (bitmap != null) {
                                    imageView.setImageBitmap(bitmap);
                                    progressBar.setVisibility(View.GONE);
                                    playVideo.setVisibility(View.VISIBLE);
                                }
                            }
                        };
                        thread.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "location":
                    textView.setVisibility(View.GONE);
                    imageView.setVisibility(View.GONE);
                    locationLayout.setVisibility(View.VISIBLE);
                    final ArrayList<String> locationInfo = new ArrayList<>(Arrays.asList(object.getMessage().split(",")));
                    locationName.setText(locationInfo.get(0));
                    locationLayout.setOnClickListener(v -> mContext.startActivity(new Intent(mContext, BiggerLocationActivity.class)
                            .putExtra("biggerLocationLat", locationInfo.get(1))
                            .putExtra("biggerLocationLng", locationInfo.get(2))
                            .putExtra("biggerLocationName", locationInfo.get(0))));
                    break;
            }
        }

        private void setListener(MessageObject object){
            textView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            imageView.setOnClickListener(v -> mContext.startActivity(new Intent(mContext, BiggerMediaActivity.class)
                    .putExtra("biggerMediaFilePath", object.getMessage())
                    .putExtra("biggerMediaType", "image")));
        }
    }
}
