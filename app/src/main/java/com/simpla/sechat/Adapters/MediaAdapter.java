package com.simpla.sechat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simpla.sechat.BiggerMediaActivity;
import com.simpla.sechat.Extensions.UniversalImageLoader;
import com.simpla.sechat.Objects.MediaObject;
import com.simpla.sechat.R;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaAdapterHolder> {

    private Context mContext;
    private List<MediaObject> list;

    public MediaAdapter(Context mContext, List<MediaObject> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @NonNull
    @Override
    public MediaAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_image,parent,false);
        return new MediaAdapterHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MediaAdapterHolder holder, int position) {
        holder.setData(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MediaAdapterHolder extends RecyclerView.ViewHolder{
        private ImageView imageView,type;
        private ProgressBar progressBar;

        public MediaAdapterHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.grid_image);
            progressBar = itemView.findViewById(R.id.grid_progress);
            type= itemView.findViewById(R.id.grid_type);
        }

        private void setData(MediaObject object){
            if(object.getType().equalsIgnoreCase("video")) type.setVisibility(View.VISIBLE);
            UniversalImageLoader.setImage(object.getUrl(),imageView,progressBar,"");
            imageView.setOnClickListener(v -> mContext.startActivity(new Intent(mContext, BiggerMediaActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    .putExtra("biggerMediaFilePath",object.getUrl()).putExtra("biggerMediaType",object.getType())));
        }
    }

}
