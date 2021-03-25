package com.simpla.sechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.simpla.sechat.Adapters.MessageAdapter;
import com.simpla.sechat.Extensions.AuthHelper;
import com.simpla.sechat.Extensions.EventBusDataEvents;
import com.simpla.sechat.Extensions.GetPhotosVideos;
import com.simpla.sechat.Extensions.Helper;
import com.simpla.sechat.Extensions.PreferencesHelper;
import com.simpla.sechat.Extensions.RunTimePermissions;
import com.simpla.sechat.Objects.MessageObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;

public class MessageActivity extends RunTimePermissions {

    private String uid,messageLastID = "",messageIDcontrol = "",locationUid ="",theirName ="",myName ="";
    private TextView fullname,blockMy,block,searchTxt;
    private ImageView infoButton,cameraButton,libraryButton,locationButton,sendButton,back;
    private EditText messageEditText;
    private SearchView searchView;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseAuth mAuth;
    private DatabaseReference dRef,yRef,y2Ref;
    private ArrayList<MessageObject> allMessages,searchList;
    private RecyclerView recyclerView,searchRw;
    private MessageAdapter adapter,sAdapter;
    private SwipeRefreshLayout refreshLayout;
    private int messageNumberPerPage = 10,messagePosition = 0,messageMorePosition=0,notControl = 0;
    private ChildEventListener childEventListener;
    private ConstraintLayout typingLayout,seenLayout,textLayout,blockLayout;
    private Boolean seenControl = false,imageControl = false;
    private AlertDialog.Builder ad;
    private AlertDialog alert;
    private static final int REQUEST_CODE1 = 100,REQUEST_CODE2 = 200;
    public static Boolean activityControl = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesHelper.setTheme(MessageActivity.this);
        setContentView(R.layout.activity_message);
        getIntentData();
    }

    private void getIntentData(){
        Intent intent1 = getIntent();
        locationUid = intent1.getStringExtra("locationUid");
        notControl = intent1.getIntExtra("notControl",0);
        if(locationUid != null){
            uid = intent1.getStringExtra("locationUid");
        }else{
            uid = intent1.getStringExtra("mUid");
        }
        findIds();
    }

    private void findIds(){
        mAuth = FirebaseAuth.getInstance();
        listener = new AuthHelper().AuthListener2(MessageActivity.this);
        typingLayout = findViewById(R.id.m_typing_layout);
        seenLayout = findViewById(R.id.m_seen_layout);
        refreshLayout = findViewById(R.id.m_swipe);
        fullname = findViewById(R.id.m_name);
        messageEditText = findViewById(R.id.m_edit_txt);
        infoButton = findViewById(R.id.m_info);
        cameraButton = findViewById(R.id.m_camera);
        libraryButton = findViewById(R.id.m_library);
        locationButton = findViewById(R.id.m_location);
        sendButton = findViewById(R.id.m_send);
        recyclerView = findViewById(R.id.m_rw);
        searchRw = findViewById(R.id.m_search_rw);
        textLayout = findViewById(R.id.m_text_layout);
        blockLayout = findViewById(R.id.m_block);
        blockMy = findViewById(R.id.m_block_msg_my);
        block = findViewById(R.id.m_block_msg);
        back = findViewById(R.id.m_back);
        searchView = findViewById(R.id.m_search);
        searchTxt = findViewById(R.id.m_search_txt);
        allMessages = new ArrayList<>();
        searchList = new ArrayList<>();
        dRef = FirebaseDatabase.getInstance().getReference();
        if(mAuth.getCurrentUser() == null) return;
        yRef = FirebaseDatabase.getInstance().getReference().child("messagesFastAccess")
                .child(mAuth.getCurrentUser().getUid()).child(uid);
        y2Ref = FirebaseDatabase.getInstance().getReference().child("messagesFastAccess")
                .child(uid).child(mAuth.getCurrentUser().getUid());
        ad = new AlertDialog.Builder(this);
        loadInfos();
        smallSettings();
    }

    private void smallSettings() {
        alert = ad.create();
        ProgressBar input = new ProgressBar(MessageActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(lp);
        alert.setView(input);
        alert.setCancelable(false);
        sendButton.setEnabled(false);
        searchView.setOnClickListener(v1 -> {
            searchTxt.setVisibility(View.GONE);
            searchView.setIconified(false);
        });
        searchTxt.setText(getResources().getString(R.string.search_msg));
        blockControl();
        setupRw();
        setupSearch();
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(searchView.getQuery().toString().isEmpty()){
                    searchList.clear();
                    refreshLayout.setVisibility(View.VISIBLE);
                    searchRw.setVisibility(View.GONE);
                }else{
                    Search(newText);
                    refreshLayout.setVisibility(View.GONE);
                    searchRw.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });
        searchView.setOnCloseListener(() -> {
            searchTxt.setVisibility(View.VISIBLE);
            return false;
        });
    }

    private void setupRw(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        searchRw.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
        adapter = new MessageAdapter(this,allMessages);
        sAdapter = new MessageAdapter(this,searchList);
        recyclerView.setAdapter(adapter);
        searchRw.setAdapter(sAdapter);
        setListeners();
    }

    private void setListeners(){
        back.setOnClickListener(view -> {
            if(imageControl != null || locationUid != null || notControl==1){
                finish();
                startActivity(new Intent(MessageActivity.this,NavigateActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
            }else{
                onBackPressed();
            }
        });
        refreshLayout.setOnRefreshListener(() -> {
            if(mAuth.getCurrentUser() == null) return;
            dRef.child("messages").child(mAuth.getCurrentUser().getUid()).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getChildrenCount()>allMessages.size()){
                        messageMorePosition = 0;
                        loadMoreMessages();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MessageActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            refreshLayout.setRefreshing(false);
        });
        sendButton.setOnClickListener(v -> {
            if(mAuth.getCurrentUser() == null) return;
            String text = messageEditText.getText().toString();
            sendMessage(text,"text");
        });
        messageEditText.addTextChangedListener(new TextWatcher() {
            Boolean typing = false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = getGraphemeCount(s.toString());
                if(!s.toString().isEmpty() && s.toString().trim().length() == 1 || length>0){
                    sendButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_send,getTheme()));
                    sendButton.setEnabled(true);
                    typing=true;
                    yRef.child("typing").setValue(true);
                }else if(typing && s.toString().equals("")){
                    sendButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_send_2,getTheme()));
                    sendButton.setEnabled(false);
                    typing = false;
                    yRef.child("typing").setValue(false);
                }
            }
        });
        libraryButton.setOnClickListener(v -> {
            if(Build.VERSION.SDK_INT>=23){
                String[] requiredPermissions={Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
                MessageActivity.super.askPermission(requiredPermissions,REQUEST_CODE1);
            }else{
                LibraryResult();
            }
        });
        cameraButton.setOnClickListener(v -> {
            if(Build.VERSION.SDK_INT>=23){
                String[] requiredPermissions={Manifest.permission.READ_EXTERNAL_STORAGE
                        ,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
                MessageActivity.super.askPermission(requiredPermissions,REQUEST_CODE2);
            }else{
                CameraResult();
            }
        });
        locationButton.setOnClickListener(v -> {
            Intent intent = new Intent(MessageActivity.this,LocationActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.putExtra("locationUid",uid);
            startActivity(intent);
        });
        fullname.setOnClickListener(v -> {
            /*Intent intent = new Intent(MessageActivity.this,ClickedUserActivity.class);
            intent.putExtra("uid",uid);
            startActivity(intent);*/
        });
        infoButton.setOnClickListener(v -> {
            Intent intent = new Intent(MessageActivity.this,InfoActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.putExtra("infoUid",uid);
            startActivity(intent);
        });
        loadMessages();
    }

    private void Search(final String searchText) {
        if(mAuth.getCurrentUser() == null) return;
        dRef.child("messages").child(mAuth.getCurrentUser().getUid()).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    searchList.clear();
                    sAdapter.notifyDataSetChanged();
                    for(DataSnapshot d: snapshot.getChildren()){
                        if(String.valueOf(d.child("message").getValue()).contains(searchText)
                                || String.valueOf(d.child("message").getValue()).equals(searchText)){
                            /*MessageObject object = new MessageObject(String.valueOf(d.child("message").getValue())
                                    ,Boolean.parseBoolean(String.valueOf(d.child("seen").getValue()))
                                    ,Long.parseLong(String.valueOf(d.child("time").getValue()))
                                    ,String.valueOf(d.child("type").getValue()),String.valueOf(d.child("user_id").getValue()));*/
                            MessageObject object = d.getValue(MessageObject.class);
                            searchList.add(object);
                            sAdapter.notifyItemInserted(searchList.size()-1);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessageActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void permissionGranted(int requestCode) {
        if(REQUEST_CODE1==requestCode){
            LibraryResult();
        }else if(REQUEST_CODE2==requestCode){
            CameraResult();
        }
    }

    private void LibraryResult(){
        startActivity(new Intent(MessageActivity.this,GalleryActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).putExtra("activity",2));
    }

    private void CameraResult(){
        startActivity(new Intent(MessageActivity.this,CameraActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).putExtra("activity",2));
    }

    private void blockControl(){
        if(mAuth.getCurrentUser() == null) return;
        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child("block").exists()){
                            for(DataSnapshot d : snapshot.child("block").getChildren()){
                                if(String.valueOf(d.getKey()).equals(uid)){
                                    if(String.valueOf(d.getValue()).equals("me") || String.valueOf(d.getValue()).equals("both")){
                                        blockLayout.setVisibility(View.VISIBLE);
                                        textLayout.setVisibility(View.GONE);
                                        blockMy.setVisibility(View.VISIBLE);
                                        block.setVisibility(View.GONE);
                                    }else{
                                        blockLayout.setVisibility(View.VISIBLE);
                                        textLayout.setVisibility(View.GONE);
                                        blockMy.setVisibility(View.GONE);
                                        block.setVisibility(View.VISIBLE);
                                    }
                                    break;
                                }else{
                                    blockLayout.setVisibility(View.GONE);
                                    textLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MessageActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private ValueEventListener typingListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.getValue() != null){
                if(snapshot.getValue().toString().equalsIgnoreCase("true")){
                    typingLayout.setVisibility(View.VISIBLE);
                    typingLayout.startAnimation(AnimationUtils.loadAnimation(MessageActivity.this,android.R.anim.fade_in));
                    if(seenControl){
                        seenLayout.setVisibility(View.GONE);
                    }
                }else if(snapshot.getValue().toString().equalsIgnoreCase("false")){
                    typingLayout.setVisibility(View.GONE);
                    typingLayout.startAnimation(AnimationUtils.loadAnimation(MessageActivity.this,android.R.anim.fade_out));
                    if(seenControl){
                        seenLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(MessageActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void loadMoreMessages(){
        if(mAuth.getCurrentUser() == null) return;
        dRef.child("messages").child(mAuth.getCurrentUser().getUid()).child(uid).orderByKey().endAt(messageLastID)
                .limitToLast(messageNumberPerPage).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {
                        MessageObject object = snapshot.getValue(MessageObject.class);
                        if(!messageIDcontrol.equals(snapshot.getKey())){
                            allMessages.add(messageMorePosition++,object);
                        }else{
                            messageIDcontrol=messageLastID;
                        }
                        if(messageMorePosition==1){
                            messageLastID = snapshot.getKey();
                        }
                        adapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messageNumberPerPage);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MessageActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMessages() {
        if(mAuth.getCurrentUser() == null) return;
        int messagePageNumber = 1;
        childEventListener = dRef.child("messages").child(mAuth.getCurrentUser().getUid()).child(uid).limitToLast(messagePageNumber*messageNumberPerPage).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {
                MessageObject object = snapshot.getValue(MessageObject.class);
                allMessages.add(object);
                if(messagePosition==0){
                    messageLastID = snapshot.getKey();
                    messageIDcontrol = snapshot.getKey();
                }
                messagePosition++;
                updateSeen(snapshot.getKey());
                updateSeen2(snapshot.getKey());
                adapter.notifyItemInserted(allMessages.size()-1);
                recyclerView.scrollToPosition(allMessages.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessageActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSeen2(final String key) {
        if(mAuth.getCurrentUser() == null) return;
        final DatabaseReference oRef = FirebaseDatabase.getInstance().getReference().child("messages");
        oRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(uid).child(mAuth.getCurrentUser().getUid()).exists()){
                    oRef.child(uid).child(mAuth.getCurrentUser().getUid()).child(key)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.child("seen").exists() && snapshot.child("user_id").exists()){
                                        if(String.valueOf(snapshot.child("seen").getValue()).equalsIgnoreCase("true")
                                                && String.valueOf(snapshot.child("user_id").getValue())
                                                .equals(mAuth.getCurrentUser().getUid())){
                                            seenLayout.setVisibility(View.VISIBLE);
                                            seenControl = true;
                                        }else{
                                            seenControl = false;
                                            seenLayout.setVisibility(View.GONE);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(MessageActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessageActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSeen(String key) {
        if(mAuth.getCurrentUser() == null) return;
        FirebaseDatabase.getInstance().getReference().child("messages").child(mAuth.getCurrentUser().getUid()).child(uid)
                .child(key).child("seen").setValue(true).addOnCompleteListener(task ->
                FirebaseDatabase.getInstance().getReference().child("messagesFastAccess").child(mAuth.getCurrentUser().getUid()).child(uid)
                    .child("seen").setValue(true));
    }

    private void loadInfos(){
        dRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                theirName = String.valueOf(snapshot.child(uid).child("nickname").getValue());
                fullname.setText(theirName);
                if(mAuth.getCurrentUser() == null) return;
                myName = String.valueOf(snapshot.child(mAuth.getCurrentUser().getUid()).child("nickname").getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessageActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void uploadToStorage(String filePath){
        final StorageReference sRef = FirebaseStorage.getInstance().getReference();
        final FirebaseUser mUser = mAuth.getCurrentUser();
        final Uri filePath1 = Uri.parse("file://"+filePath);
        if(mUser == null || filePath1.getLastPathSegment() == null) return;
        final UploadTask uploadTask = sRef.child("messagesMedia").child(mUser.getUid()).child(uid).
                child(filePath1.getLastPathSegment()).putFile(filePath1);
        uploadTask.continueWithTask(task1 ->
                sRef.child("messagesMedia").child(mUser.getUid()).child(uid).child(filePath1.getLastPathSegment())
                        .getDownloadUrl()).addOnCompleteListener(task12 -> {
            if (task12.isSuccessful()) {
                Uri downloadURL = task12.getResult();
                sendMessage(downloadURL.toString(),"media");
            }
        }).addOnFailureListener(e -> {
            alert.cancel();
            Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private int getGraphemeCount(String s) {
        BreakIterator boundary = BreakIterator.getCharacterInstance(Locale.ROOT);
        boundary.setText(s);
        boundary.first();
        int result = 0;
        while (boundary.next() != BreakIterator.DONE) {
            ++result;
        }
        return result;

    }

    private void sendMessage(String txt,String type) {
        new Helper().messageHelper(type,txt,uid,imageControl,myName,theirName);
        if(type.equals("text")) messageEditText.setText("");
        alert.cancel();
    }

    @Subscribe
    public void EventBusTakeImage(EventBusDataEvents.sendMediaToMesssage media){
        String filePath = media.getPath();
        imageControl = media.isItImage();
        if(filePath != null ){
            alert.show();
            if(imageControl){
                GetPhotosVideos.compressImageFile(MessageActivity.this,getApplicationContext(),filePath);
            }else{
                GetPhotosVideos.compressVideoFile(MessageActivity.this,filePath);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        activityControl = false;
        if(mAuth.getCurrentUser() == null) return;
        dRef.child("messages").child(mAuth.getCurrentUser().getUid()).child(uid).removeEventListener(childEventListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        activityControl = true;
        y2Ref.child("typing").addValueEventListener(typingListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityControl = false;
        yRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("typing")){
                    yRef.child("typing").setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        yRef.child("typing").removeEventListener(typingListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityControl = true;
        mAuth.addAuthStateListener(listener);
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listener != null) mAuth.removeAuthStateListener(listener);
        EventBus.getDefault().unregister(this);
    }
}