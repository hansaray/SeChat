package com.simpla.sechat.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simpla.sechat.Extensions.Helper;
import com.simpla.sechat.Objects.LocationObject;
import com.simpla.sechat.R;

import java.util.ArrayList;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationAdapterHolder>{

    private Context mContext;
    private ArrayList<LocationObject> list;

    public LocationAdapter(Context mContext, ArrayList<LocationObject> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @NonNull
    @Override
    public LocationAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location,parent,false);
        return new LocationAdapter.LocationAdapterHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationAdapterHolder holder, int position) {
        holder.setData(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class LocationAdapterHolder extends RecyclerView.ViewHolder{
        private TextView locationName;
        private CardView cardView;
        private ImageView image;
        public LocationAdapterHolder(@NonNull View itemView) {
            super(itemView);
            locationName = itemView.findViewById(R.id.li_name);
            cardView = itemView.findViewById(R.id.li_card);
            image = itemView.findViewById(R.id.li_image);
        }

        private void setData(final LocationObject locationObject){
            locationName.setText(locationObject.getLocationName());
            if(locationObject.getLocationName().equals(mContext.getResources().getString(R.string.send_exact_location))){
                image.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.ic_exact_location,mContext.getTheme()));
            }else{
                image.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.ic_location,mContext.getTheme()));
            }
            cardView.setOnClickListener(v -> sendLocation(locationObject));
        }

        private void sendLocation(LocationObject locationObject){
            final String uid = locationObject.getUid();
            final String messageText = locationObject.getLocationName()+","+locationObject.getLatitude()+","+locationObject.getLongLatitude();
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            if(mAuth.getCurrentUser() == null) return;
            FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String myName = String.valueOf(snapshot.child(mAuth.getCurrentUser().getUid()).child("nickname").getValue());
                    String theirName = String.valueOf(snapshot.child(uid).child("nickname").getValue());
                    new Helper().messageHelper("location",messageText,uid,false,myName,theirName);
                    ((Activity)mContext).finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

