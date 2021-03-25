package com.simpla.sechat.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simpla.sechat.Extensions.AESEncryption;
import com.simpla.sechat.Extensions.TimeConverter;
import com.simpla.sechat.Extensions.UniversalImageLoader;
import com.simpla.sechat.MessageActivity;
import com.simpla.sechat.Objects.ChatObject;
import com.simpla.sechat.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatAdapterHolder> {
    private Context mContext;
    private ArrayList<ChatObject> list;

    public ChatAdapter(Context mContext, ArrayList<ChatObject> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @NonNull
    @Override
    public ChatAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat,parent,false);
        return new ChatAdapterHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapterHolder holder, int position) {
        holder.setData(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ChatAdapterHolder extends RecyclerView.ViewHolder{
        private CircleImageView imageView;
        private TextView username,lastMessage,time;
        private ImageView notSeen;
        private ConstraintLayout constraintLayout;

        public ChatAdapterHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.chat_image);
            username = itemView.findViewById(R.id.chat_nickname);
            lastMessage = itemView.findViewById(R.id.chat_text);
            time = itemView.findViewById(R.id.chat_time);
            notSeen = itemView.findViewById(R.id.chat_not_seen);
            constraintLayout = itemView.findViewById(R.id.chat_layout);
        }

        @SuppressLint("SetTextI18n")
        public void setData(final ChatObject object) {
            String message = object.getLast_message();
            switch (object.getType()) {
                case "text":
                    try{
                        message = AESEncryption.decrypt(message);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    message = message.replace("\n", " ");
                    message = message.trim();
                    if (object.getLast_message().length() > 30) {
                        lastMessage.setText(message.substring(0, 30) + "...");
                    } else {
                        lastMessage.setText(message);
                    }
                    break;
                case "photo":
                    lastMessage.setText(R.string.sent_photo);
                    break;
                case "video":
                    lastMessage.setText(R.string.sent_video);
                    break;
                case "location":
                    lastMessage.setText(itemView.getResources().getString(R.string.sent_location));
                    break;
            }
            time.setText(TimeConverter.getTimeAgoForShorterTime(object.getTime(),mContext));
            if(!object.getSeen()){
                notSeen.setVisibility(View.VISIBLE);
                username.setTypeface(null, Typeface.BOLD);
            }else{
                notSeen.setVisibility(View.GONE);
                username.setTypeface(null,Typeface.NORMAL);
            }

            constraintLayout.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, MessageActivity.class).putExtra("mUid",object.getUser_id());
                if(FirebaseAuth.getInstance().getCurrentUser() == null) return;
                FirebaseDatabase.getInstance().getReference().child("messagesFastAccess")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(object.getUser_id())
                        .child("seen").setValue(true).addOnCompleteListener(task -> mContext.startActivity(intent));
            });
            getUserInfos(object.getUser_id(),object.getControl());
        }

        private void getUserInfos(String user_id,int control) {
            if(control==1){
                FirebaseDatabase.getInstance().getReference().child("users").child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getValue() != null){
                            username.setText(String.valueOf(snapshot.child("nickname").getValue()));
                            UniversalImageLoader.setImage(String.valueOf(snapshot.child("imageURL").getValue()),imageView,null,"");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                FirebaseDatabase.getInstance().getReference().child("users").child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getValue() != null){
                            username.setText(String.valueOf(snapshot.child("nickname").getValue()));
                            UniversalImageLoader.setImage(String.valueOf(snapshot.child("imageURL").getValue()),imageView,null,"");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
