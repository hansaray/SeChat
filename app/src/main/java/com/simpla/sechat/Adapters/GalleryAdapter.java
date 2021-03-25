package com.simpla.sechat.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.simpla.sechat.Extensions.EventBusDataEvents;
import com.simpla.sechat.Extensions.GridImageView;
import com.simpla.sechat.Extensions.UniversalImageLoader;
import com.simpla.sechat.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryAdapterHolder> {

    private Context mContext;
    private ArrayList<String> list;
    private MediaMetadataRetriever retriever = null;

    public GalleryAdapter(Context mContext, ArrayList<String> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @NonNull
    @Override
    public GalleryAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_image,parent,false);
        return new GalleryAdapterHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryAdapterHolder holder, int position) {
        holder.setData(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class GalleryAdapterHolder extends RecyclerView.ViewHolder{
        private ProgressBar progressBar;
        private GridImageView imageView;
        private TextView textView;
        private ConstraintLayout layout;

        public GalleryAdapterHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.grid_progress);
            imageView = itemView.findViewById(R.id.grid_image);
            textView = itemView.findViewById(R.id.grid_timer);
            layout = itemView.findViewById(R.id.gridLayout);
        }

        private void setData(String filePath){
            String fileType = filePath.substring(filePath.lastIndexOf("."));
            if(fileType.equalsIgnoreCase(".mp4")||fileType.equalsIgnoreCase(".mov")){
                try{
                    textView.setVisibility(View.VISIBLE);
                    retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(mContext, Uri.parse("file://"+filePath));
                    String videoTime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    long videoTimeLong = 0;
                    if(videoTime != null) videoTimeLong = Long.parseLong(videoTime);
                    retriever.release();
                    textView.setText(convertDuration(videoTimeLong));
                    UniversalImageLoader.setImage(filePath,imageView,progressBar,"file:/");
                }finally {
                    if(retriever != null){
                        retriever.release();
                    }
                }
            }else{
                textView.setVisibility(View.GONE);
                UniversalImageLoader.setImage(filePath,imageView,progressBar,"file:/");
            }
            layout.setOnClickListener(v -> {
                layout.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
                EventBus.getDefault().post(new EventBusDataEvents.sendMediaPosition(filePath));
            });
        }

        @SuppressLint("DefaultLocale")
        private String convertDuration(long duration) {
            String out;
            long second = duration / 1000 % 60;
            long minute = duration / (1000*60) % 60;
            long hour = duration / (1000*60*60) % 24;
            if(hour>0){
                out = String.format("%02d:%02d:%02d",hour,minute,second);
            }else{
                out = String.format("%02d:%02d",minute,second);
            }
            return out;
        }
    }

}
