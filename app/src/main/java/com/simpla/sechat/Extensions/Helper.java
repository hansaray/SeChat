package com.simpla.sechat.Extensions;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.simpla.sechat.R;

import java.util.HashMap;
import java.util.Map;

public class Helper {

    public void setEmptyView(View empty, Context context, Activity activity, String title, int image){
        ImageView emptyImage = empty.findViewById(R.id.empty_view_image);
        TextView emptyTitle = empty.findViewById(R.id.empty_view_title);
        emptyTitle.setText(title);
        emptyImage.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),image,activity.getTheme()));
    }

    public void messageHelper(String type,String txt, String uid,boolean imageControl,String myName,String theirName) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference();
        if(mAuth.getCurrentUser() == null) return;
        if(type.equals("media")){
            if(imageControl) type = "photo";
            else type = "video";
        }
        if(type.equals("text")){
            try {
                txt = AESEncryption.encrypt(txt);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        String messageKey = dRef.child("messages").child(mAuth.getCurrentUser().getUid()).child(uid)
                .push().getKey();
        if(messageKey == null) return;
        Map<String, String> time = ServerValue.TIMESTAMP;
        HashMap<String,Object> messageSender = new HashMap<>();
        messageSender.put("message",txt);
        messageSender.put("seen",true);
        messageSender.put("time",time);
        messageSender.put("type",type);
        messageSender.put("user_id",mAuth.getCurrentUser().getUid());
        HashMap<String,Object> messageReceiver = new HashMap<>();
        messageReceiver.put("message",txt);
        messageReceiver.put("seen",false);
        messageReceiver.put("time",time);
        messageReceiver.put("type",type);
        messageReceiver.put("user_id",mAuth.getCurrentUser().getUid());
        dRef.child("messages").child(mAuth.getCurrentUser().getUid()).child(uid)
                .child(messageKey).setValue(messageSender);
        dRef.child("messages").child(uid).child(mAuth.getCurrentUser().getUid())
                .child(messageKey).setValue(messageReceiver);
        HashMap<String ,Object > messageFastAccessSender = new HashMap<>();
        messageFastAccessSender.put("time",time);
        messageFastAccessSender.put("seen",true);
        messageFastAccessSender.put("last_message",txt);
        messageFastAccessSender.put("typing",false);
        messageFastAccessSender.put("type",type);
        messageFastAccessSender.put("name",theirName);//their name
        HashMap<String ,Object > messageFastAccessReceiver = new HashMap<>();
        messageFastAccessReceiver.put("time",time);
        messageFastAccessReceiver.put("seen",false);
        messageFastAccessReceiver.put("last_message",txt);
        messageFastAccessReceiver.put("type",type);
        messageFastAccessReceiver.put("name",myName);//my name
        dRef.child("messagesFastAccess").child(mAuth.getCurrentUser().getUid()).child(uid)
                .setValue(messageFastAccessSender);
        dRef.child("messagesFastAccess").child(uid).child(mAuth.getCurrentUser().getUid())
                .setValue(messageFastAccessReceiver);
        if(type.equals("photo") || type.equals("video")){
            dRef.child("messagesMedia").child(mAuth.getCurrentUser().getUid()).child(uid)
                    .child(messageKey).child(type).setValue(txt);
            dRef.child("messagesMedia").child(uid).child(mAuth.getCurrentUser().getUid())
                    .child(messageKey).child(type).setValue(txt);
        }
    }

    public int blockInfo(DataSnapshot snapshot,String uid){
        if(snapshot.child("block").exists()){
            boolean check = true;
            for(DataSnapshot d : snapshot.child("block").getChildren()){
                if(String.valueOf(d.getKey()).equals(uid)){
                    if(String.valueOf(d.getValue()).equals("me") || String.valueOf(d.getValue()).equals("both"))
                        check = false;
                    break;
                }
            }
            if(check) return 1;
            else return 0;
        }else return 1;
    }

    public void block(Context mContext,String uid){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null) return;
        final String myUid = mAuth.getCurrentUser().getUid();
        final DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("users");
        dRef.child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("block").exists()){
                    for(DataSnapshot d : snapshot.child("block").getChildren()){
                        if(String.valueOf(d.getKey()).equals(uid)){
                            dRef.child(myUid).child("block").child(uid).setValue("both");
                            dRef.child(uid).child("block").child(myUid).setValue("both");
                            break;
                        }else blockHelper(uid,myUid,dRef,snapshot);
                    }
                }else blockHelper(uid,myUid,dRef,snapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void unBlock(Context mContext,String uid){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null) return;
        final String myUid = mAuth.getCurrentUser().getUid();
        final DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("users");
        dRef.child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot d : snapshot.child("block").getChildren()){
                    if(String.valueOf(d.getKey()).equals(uid)){
                        if(String.valueOf(d.getValue()).equals("both")){
                            dRef.child(myUid).child("block").child(uid).setValue("them");
                            dRef.child(uid).child("block").child(myUid).setValue("me");
                        }else{
                            dRef.child(myUid).child("block").child(uid).removeValue();
                            dRef.child(uid).child("block").child(myUid).removeValue();
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void blockHelper(String uid,String myUid,DatabaseReference dRef,DataSnapshot snapshot){
        dRef.child(myUid).child("block").child(uid).setValue("me");
        dRef.child(uid).child("block").child(myUid).setValue("them");
        if(snapshot.child("follow").exists()){
            for(DataSnapshot d1 : snapshot.child("follow").getChildren()){
                if(String.valueOf(d1.getKey()).equals(uid)){
                    dRef.child(myUid).child("follow").child(uid).removeValue();
                    dRef.child(uid).child("followers").child(myUid).removeValue();
                    break;
                }
            }
        }
        if(snapshot.child("followers").exists()){
            for(DataSnapshot d1 : snapshot.child("followers").getChildren()){
                if(String.valueOf(d1.getKey()).equals(uid)){
                    dRef.child(uid).child("follow").child(myUid).removeValue();
                    dRef.child(myUid).child("followers").child(uid).removeValue();
                    break;
                }
            }
        }
    }

}
