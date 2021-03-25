package com.simpla.sechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.simpla.sechat.Extensions.AuthHelper;
import com.simpla.sechat.Extensions.EventBusDataEvents;
import com.simpla.sechat.Extensions.Helper;
import com.simpla.sechat.Extensions.PreferencesHelper;
import com.simpla.sechat.Extensions.UniversalImageLoader;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class InfoActivity extends AppCompatActivity {
    private String uid;
    private TextView fullName,code,delete,report,block,mediaNumber;
    private CircleImageView circleImageView;
    private ImageView mediaButton,back;
    private ProgressBar progressBar;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch aSwitch;
    private FrameLayout frameLayout;
    private ConstraintLayout constraintLayout;
    private int mediaControl = 0;
    private AlertDialog.Builder ad;
    private FirebaseAuth mAuth;
    private Helper helper;
    private FirebaseAuth.AuthStateListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesHelper.setTheme(InfoActivity.this);
        setContentView(R.layout.activity_info);
        getIntentData();
    }

    private void getIntentData(){
        Intent intent = getIntent();
        uid = intent.getStringExtra("infoUid");
        findIds();
    }

    private void findIds() {
        ad = new AlertDialog.Builder(this);
        progressBar = findViewById(R.id.info_progress);
        fullName = findViewById(R.id.info_name);
        report = findViewById(R.id.info_report);
        block = findViewById(R.id.info_block);
        delete = findViewById(R.id.info_delete);
        mediaNumber = findViewById(R.id.info_media_num);
        mediaButton = findViewById(R.id.info_media_button);
        circleImageView = findViewById(R.id.info_pp);
        aSwitch = findViewById(R.id.info_mute);
        frameLayout = findViewById(R.id.info_frame);
        constraintLayout = findViewById(R.id.info_layout);
        code = findViewById(R.id.info_code);
        back = findViewById(R.id.info_back);
        mAuth = FirebaseAuth.getInstance();
        listener = new AuthHelper().AuthListener2(InfoActivity.this);
        helper = new Helper();
        setListeners();
    }

    private void setListeners() {
        back.setOnClickListener(view -> onBackPressed());
        delete.setOnClickListener(v -> {
            ad.setTitle(R.string.delete);
            ad.setMessage(R.string.delete_convo);
            ad.setIcon(R.drawable.ic_info);
            ad.setPositiveButton(R.string.no, (dialog, which) -> ad.create().cancel());
            ad.setNegativeButton(R.string.delete_user, (dialog, which) -> {
                if(mAuth.getCurrentUser() == null) return;
                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("messages").child(mAuth.getCurrentUser().getUid());
                DatabaseReference myRef  = FirebaseDatabase.getInstance().getReference().child("messagesFastAccess").child(mAuth.getCurrentUser().getUid());
                final DatabaseReference fRef = FirebaseDatabase.getInstance().getReference().child("messagesMedia");
                mRef.child(uid).removeValue();
                myRef.child(uid).removeValue();
                fRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(mAuth.getCurrentUser().getUid()).exists()){
                            if(snapshot.child(mAuth.getCurrentUser().getUid()).child(uid).exists()){
                                fRef.child(mAuth.getCurrentUser().getUid()).child(uid).removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(InfoActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                startActivity(new Intent(InfoActivity.this,NavigateActivity.class));
            });
            ad.create().show();
        });
        mediaButton.setOnClickListener(v -> {
            if(mediaControl==1){
                frameLayout.setVisibility(View.VISIBLE);
                constraintLayout.setVisibility(View.GONE);
                EventBus.getDefault().postSticky(new EventBusDataEvents.sendMediaPosition(uid));
                getSupportFragmentManager().beginTransaction().replace(R.id.info_frame,new InfoMediaFragment()).commit();
            }
        });
        report.setOnClickListener(v -> {
            final AlertDialog alertDialog = ad.create();
            alertDialog.setTitle(getResources().getString(R.string.report));
            alertDialog.setMessage(getResources().getString(R.string.reason_report));
            LinearLayout linearLayout = new LinearLayout(InfoActivity.this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final LinearLayout layout = new LinearLayout(InfoActivity.this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            final EditText text = new EditText(InfoActivity.this);
            text.setHint(getResources().getString(R.string.reason));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,3);
            text.setLayoutParams(lp);
            Button done = new Button(InfoActivity.this);
            LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,1);
            done.setLayoutParams(lp1);
            done.setText(getResources().getString(R.string.done));
            layout.addView(text);
            layout.addView(done);
            layout.setVisibility(View.GONE);
            Button b1 = new Button(InfoActivity.this);
            b1.setAllCaps(false);
            b1.setBackgroundColor(getResources().getColor(R.color.white));
            b1.setTextColor(getResources().getColor(R.color.black));
            b1.setText(getResources().getString(R.string.inappropriate_msg));
            Button b5 = new Button(InfoActivity.this);
            b5.setAllCaps(false);
            b5.setBackgroundColor(getResources().getColor(R.color.white));
            b5.setTextColor(getResources().getColor(R.color.black));
            b5.setText(getResources().getString(R.string.other));
            linearLayout.addView(b1);
            linearLayout.addView(b5);
            linearLayout.addView(layout);
            alertDialog.setView(linearLayout);
            alertDialog.show();
            b5.setOnClickListener(v13 -> layout.setVisibility(View.VISIBLE));
            b1.setOnClickListener(v12 -> {
                alertDialog.cancel();
                report(getResources().getString(R.string.inappropriate_msg));
            });
            done.setOnClickListener(v1 -> {
                if(text.getText().toString().isEmpty()){
                    alertDialog.cancel();
                    report("empty");
                }else{
                    alertDialog.cancel();
                    report(text.getText().toString());
                }
            });
        });
        block.setOnClickListener(v -> {
            if(mAuth.getCurrentUser() == null) return;
            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(mAuth.getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int info = helper.blockInfo(snapshot,uid);
                            if(info == 0) helper.unBlock(InfoActivity.this,uid);
                            else helper.block(InfoActivity.this,uid);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(InfoActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(mAuth.getCurrentUser() == null) return;
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("messagesFastAccess")
                    .child(mAuth.getCurrentUser().getUid()).child(uid);
            if(isChecked){
                mRef.child("mute").setValue(true);
            }else{
                mRef.child("mute").removeValue();
            }
        });
        loadInfos();
    }

    private void report(String reason){
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference();
        String key = dRef.child("reports").push().getKey();
        if(mAuth.getCurrentUser() == null || key == null) return;
        Map<String, String> time = ServerValue.TIMESTAMP;
        HashMap<String,Object> userInfo = new HashMap<>();
        userInfo.put("userID",uid);
        dRef.child("reports").child(key).child("mUserInfo").setValue(userInfo);
        HashMap<String,Object> reportInfo = new HashMap<>();
        reportInfo.put("userID",mAuth.getCurrentUser().getUid());
        reportInfo.put("reason",reason);
        reportInfo.put("time",time);
        dRef.child("reports").child(key).child("reportInfo").setValue(reportInfo);
    }

    private void loadInfos(){
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference();
        dRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    fullName.setText(String.valueOf(snapshot.child("nickname").getValue()));
                    code.setText(String.valueOf(snapshot.child("code").getValue()));
                    UniversalImageLoader.setImage(String.valueOf(snapshot.child("imageURL").getValue()),circleImageView,progressBar,"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InfoActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        dRef.child("messagesMedia").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mAuth.getCurrentUser() == null) return;
                if(snapshot.child(mAuth.getCurrentUser().getUid()).exists()){
                    if(snapshot.child(mAuth.getCurrentUser().getUid()).child(uid).exists()){
                        mediaNumber.setText(String.valueOf(snapshot.child(mAuth.getCurrentUser().getUid()).child(uid).getChildrenCount()));
                        mediaControl = 1;
                    }else mediaNumber.setText("0");
                }else mediaNumber.setText("0");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InfoActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        if(mAuth.getCurrentUser() == null) return;
        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int info = helper.blockInfo(snapshot,uid);
                        if(info == 0) block.setText(getResources().getString(R.string.unblock));
                        else block.setText(getResources().getString(R.string.block_user));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(InfoActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(listener != null) mAuth.removeAuthStateListener(listener);
    }
}
