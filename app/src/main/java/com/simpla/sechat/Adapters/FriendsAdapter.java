package com.simpla.sechat.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simpla.sechat.AddActivity;
import com.simpla.sechat.Extensions.Helper;
import com.simpla.sechat.Extensions.UniversalImageLoader;
import com.simpla.sechat.MessageActivity;
import com.simpla.sechat.Objects.FriendsObject;
import com.simpla.sechat.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsAdapterHolder> {

    private Context mContext;
    private ArrayList<FriendsObject> list;

    public FriendsAdapter(Context mContext, ArrayList<FriendsObject> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @NonNull
    @Override
    public FriendsAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friends,parent,false);
        return new FriendsAdapterHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsAdapterHolder holder, int position) {
        holder.setData(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class FriendsAdapterHolder extends RecyclerView.ViewHolder {

        private CircleImageView imageView;
        private TextView name;
        private ImageView menu;
        private ConstraintLayout layout;
        private DatabaseReference mRef;
        private FirebaseAuth mAuth;

        public FriendsAdapterHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.friends_image);
            name = itemView.findViewById(R.id.friends_nickname);
            menu = itemView.findViewById(R.id.friends_menu);
            layout = itemView.findViewById(R.id.friends_layout);
            mRef = FirebaseDatabase.getInstance().getReference();
            mAuth = FirebaseAuth.getInstance();
        }

        private void setData(FriendsObject object){
            name.setText(object.getNickname());
            UniversalImageLoader.setImage(object.getImage(),imageView,null,"");
            menu.setOnClickListener(view -> {
                if(object.getType() == 1) setMenuData(object); //FriendsFragment
                else if(object.getType() == 2) addFriend(object); //AddActivity.Add friend
                else if(object.getType() == 21) newMessage(object); //AddActivity.New message
            });
            if(object.getType() == 2){
                menu.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.ic_add_friends,mContext.getTheme()));
                if(mAuth.getCurrentUser() == null) return;
                mRef.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child("myList").exists()){
                            for(DataSnapshot d: snapshot.child("myList").getChildren()){
                                if(object.getCode().equals(d.getValue())){
                                    menu.setVisibility(View.GONE);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }else if(object.getType() == 21)
                menu.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.ic_add_message,mContext.getTheme()));
        }

        private void setMenuData(FriendsObject object){
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View promptView = layoutInflater.inflate(R.layout.layout_friends_menu, null);
            final AlertDialog alertD = new AlertDialog.Builder(mContext).create();
            TextView send = promptView.findViewById(R.id.fm_send);
            TextView delete = promptView.findViewById(R.id.fm_delete);
            TextView block = promptView.findViewById(R.id.fm_block);
            send.setOnClickListener(view1 -> mContext.startActivity(new Intent(mContext, MessageActivity.class)
                    .putExtra("mUid",object.getUid())));
            delete.setOnClickListener(view1 -> {
                if(mAuth.getCurrentUser() == null) return;
                mRef.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            if(snapshot.child("myList").child(object.getUid()).exists()) {
                                mRef.child(mAuth.getCurrentUser().getUid()).child("myList")
                                        .child(object.getUid()).removeValue();
                                layout.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
            block.setOnClickListener(view1 -> {
                if(mAuth.getCurrentUser() == null) return;
                mRef.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            if(snapshot.child("myList").child(object.getUid()).exists()) {
                                int info = new Helper().blockInfo(snapshot,object.getUid());
                                if(info == 0) new Helper().unBlock(mContext,object.getUid());
                                else new Helper().block(mContext,object.getUid());
                                layout.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
            alertD.setView(promptView);
            alertD.show();
        }

        private void addFriend(FriendsObject object){
            if(mAuth.getCurrentUser() == null) return;
            mRef.child("users").child(mAuth.getCurrentUser().getUid()).child("myList").child(object.getUid()).setValue(object.getCode());
            AddActivity activity = (AddActivity) mContext;
            activity.finish();
        }

        private void newMessage(FriendsObject object) {
            mContext.startActivity(new Intent(mContext, MessageActivity.class).putExtra("mUid",object.getUid()));
        }
    }
}
