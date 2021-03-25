package com.simpla.sechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simpla.sechat.Adapters.FriendsAdapter;
import com.simpla.sechat.Extensions.AuthHelper;
import com.simpla.sechat.Extensions.Helper;
import com.simpla.sechat.Extensions.PreferencesHelper;
import com.simpla.sechat.Objects.FriendsObject;

import java.util.ArrayList;

public class AddActivity extends AppCompatActivity {
    private RecyclerView recyclerView,searchRw;
    private FriendsAdapter adapter;
    private ArrayList<FriendsObject> list;
    private View empty;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private SearchView searchView;
    private TextView searchTxt,title;
    private AppBarLayout appBar;
    private ImageView add,back;
    private boolean activity = false;
    private FirebaseAuth.AuthStateListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesHelper.setTheme(AddActivity.this);
        setContentView(R.layout.layout_list);
        getIntentData();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        activity = intent.getBooleanExtra("activity",false);
        findIds();
    }

    private void findIds() {
        mAuth = FirebaseAuth.getInstance();
        listener = new AuthHelper().AuthListener2(AddActivity.this);
        recyclerView = findViewById(R.id.list_rw);
        empty = findViewById(R.id.empty_view_layout);
        searchView = findViewById(R.id.list_search);
        searchTxt = findViewById(R.id.list_search_txt);
        title = findViewById(R.id.list_title);
        appBar = findViewById(R.id.list_app_bar);
        back = findViewById(R.id.list_back);
        add = findViewById(R.id.list_add);
        searchRw = findViewById(R.id.list_search_rw);
        mRef = FirebaseDatabase.getInstance().getReference().child("users");
        list = new ArrayList<>();
        adapter = new FriendsAdapter(AddActivity.this,list);
        if(activity) new Helper().setEmptyView(empty,AddActivity.this,AddActivity.this
                ,getResources().getString(R.string.code_info),R.drawable.ic_no_friends); //infoView, addFriend
        else new Helper().setEmptyView(empty,AddActivity.this,AddActivity.this
                    ,getResources().getString(R.string.send_new_message),R.drawable.ic_send_new_msg); //infoView newMessage
        smallSettings();
    }

    private void smallSettings(){
        String sTxt;
        String tTxt;
        if(activity){//add friends
            sTxt = getResources().getString(R.string.write_code);
            tTxt = getResources().getString(R.string.add_friend);
        }else{//new message
            sTxt = getResources().getString(R.string.search_friends);
            tTxt = getResources().getString(R.string.send_new_message);
        }
        searchTxt.setText(sTxt);
        searchView.setQueryHint(sTxt);
        title.setText(tTxt);
        add.setVisibility(View.GONE);
        back.setVisibility(View.VISIBLE);
        searchRw.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        empty.setVisibility(View.VISIBLE);
        if(Build.VERSION.SDK_INT >= 21) appBar.setOutlineProvider(null);
        searchView.setOnClickListener(v1 -> {
            searchTxt.setVisibility(View.GONE);
            searchView.setIconified(false);
        });
        back.setOnClickListener(view -> onBackPressed());
        setupSearch();
    }

    private void setupSearch(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(searchView.getQuery().toString().isEmpty()){
                    list.clear();
                    setEmptyVisible(false);
                }else{
                    Search(newText);
                    recyclerView.setVisibility(View.VISIBLE);
                    empty.setVisibility(View.GONE);
                }
                return true;
            }
        });
        searchView.setOnCloseListener(() -> {
            searchTxt.setVisibility(View.VISIBLE);
            return false;
        });
        setupRw();
    }

    private void setupRw() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(AddActivity.this));
        recyclerView.setAdapter(adapter);
    }

    private void Search(final String searchText) {
        if(activity){ //add friend
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        if(mAuth.getCurrentUser() == null) return;
                        String myCode = String.valueOf(snapshot.child(mAuth.getCurrentUser().getUid()).child("code").getValue());
                        for(DataSnapshot d: snapshot.getChildren()){
                            if(String.valueOf(d.child("code").getValue()).equals(searchText)
                                    && !String.valueOf(d.child("code").getValue()).equals(myCode) ){
                                FriendsObject object = new FriendsObject(d.getKey()
                                        ,String.valueOf(d.child("nickname").getValue())
                                        ,String.valueOf(d.child("imageURL").getValue()),String.valueOf(d.child("code").getValue()),2);
                                list.add(object);
                                adapter.notifyItemInserted(list.size()-1);
                                break;
                            }
                        }
                        if(list.size() <= 0) setEmptyVisible(true);
                    }else{
                        setEmptyVisible(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AddActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{//new message
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        long number = snapshot.getChildrenCount();
                        int i = 1;
                        for(DataSnapshot d: snapshot.getChildren()){
                            if(String.valueOf(d.child("nickname").getValue()).contains(searchText)
                                    || String.valueOf(d.child("nickname").getValue()).equals(searchText)){
                                if(mAuth.getCurrentUser() == null) return;
                                int finalI = i;
                                mRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            for(DataSnapshot d1: snapshot.child("myList").getChildren()){
                                                if(String.valueOf(d1.getValue()).equals(String.valueOf(d.child("code").getValue()))){
                                                    FriendsObject object = new FriendsObject(d1.getKey()
                                                            ,String.valueOf(d.child("nickname").getValue())
                                                            ,String.valueOf(d.child("imageURL").getValue()),String.valueOf(d1.getValue()),21);
                                                    list.add(object);
                                                    adapter.notifyItemInserted(list.size()-1);
                                                    break;
                                                }
                                            }
                                            if((number == finalI) && list.size() <= 0) setEmptyVisible(true);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(AddActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            i++;
                        }
                    }else setEmptyVisible(true);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AddActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setEmptyVisible(boolean msgType){
        if(msgType){ //empty
            if(activity) new Helper().setEmptyView(empty,AddActivity.this,AddActivity.this
                    ,getResources().getString(R.string.code_wrong),R.drawable.ic_empty);
            else new Helper().setEmptyView(empty,AddActivity.this,AddActivity.this
                    ,getResources().getString(R.string.new_msg_no),R.drawable.ic_empty);
        }else{ //info
            if(activity) new Helper().setEmptyView(empty,AddActivity.this,AddActivity.this
                    ,getResources().getString(R.string.code_info),R.drawable.ic_no_friends);
            else new Helper().setEmptyView(empty,AddActivity.this,AddActivity.this
                    ,getResources().getString(R.string.send_new_message),R.drawable.ic_send_new_msg);
        }
        empty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
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