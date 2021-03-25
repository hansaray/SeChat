package com.simpla.sechat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.simpla.sechat.Adapters.ChatAdapter;
import com.simpla.sechat.Extensions.Helper;
import com.simpla.sechat.Extensions.PreferencesHelper;
import com.simpla.sechat.Objects.ChatObject;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView, searchRw;
    private FirebaseAuth mAuth;
   // private ConstraintLayout messageLayout,messageMainLayout;
    private ArrayList<ChatObject> allMessages,searchList;
    private ChatAdapter adapter,sAdapter;
    private DatabaseReference mRef;
    private Boolean listenerControl = false;
   // private String chatBack ="";
   // private String infoBack = "";
    private SearchView searchView;
    private long controlP = 0;
    //static Boolean activityControl = false;
    private View empty;
    private TextView searchTxt,title;
    private ImageView add;
    private AppBarLayout appBar;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_list,container,false);
        findIds(v);
        return v;
    }

    private void findIds(View v) {
        recyclerView = v.findViewById(R.id.list_rw);
        searchRw = v.findViewById(R.id.list_search_rw);
        empty = v.findViewById(R.id.empty_view_layout);
        searchView = v.findViewById(R.id.list_search);
        searchTxt = v.findViewById(R.id.list_search_txt);
        title = v.findViewById(R.id.list_title);
        appBar = v.findViewById(R.id.list_app_bar);
        add = v.findViewById(R.id.list_add);
        progressBar = v.findViewById(R.id.list_progress);
        mAuth = FirebaseAuth.getInstance();
        allMessages = new ArrayList<>();
        searchList = new ArrayList<>();
        sAdapter = new ChatAdapter(getContext(),searchList);
        adapter = new ChatAdapter(getContext(),allMessages);
        smallSettings();
    }

    private void smallSettings() {
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.mainBlue),
                android.graphics.PorterDuff.Mode.MULTIPLY);
        searchTxt.setText(getResources().getString(R.string.search_messages));
        title.setText(getResources().getString(R.string.chats));
        add.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_add_message,requireActivity().getTheme()));
        if(Build.VERSION.SDK_INT >= 21) appBar.setOutlineProvider(null);
        searchView.setOnClickListener(v1 -> {
            searchTxt.setVisibility(View.GONE);
            searchView.setIconified(false);
        });
        add.setOnClickListener(view -> startActivity(new Intent(getContext(),AddActivity.class).putExtra("activity",false)));
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
                    if(allMessages.size() <= 0) setEmptyVisible(false);
                    searchRw.setVisibility(View.GONE);
                }else{
                    Search(newText);
                    searchRw.setVisibility(View.VISIBLE);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        searchRw.setHasFixedSize(true);
        searchRw.setLayoutManager(new LinearLayoutManager(getContext()));
        searchRw.setAdapter(sAdapter);
        loadMessages();
    }

    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {
            if(snapshot.exists()){
                controlP = controlP + 1;
                ChatObject object = snapshot.getValue(ChatObject.class);
                if(object == null) return;
                object.setUser_id(snapshot.getKey());
                object.setType(String.valueOf(snapshot.child("type").getValue()));
                object.setName(String.valueOf(snapshot.child("name").getValue()));
                allMessages.add(0,object);
                adapter.notifyItemInserted(allMessages.size()-1);
                if(controlP == 0) setEmptyVisible(false);
                else setEmptyGone();
            }else setEmptyVisible(false);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String s) {
            if(snapshot.exists()){
                int control = findPosition(snapshot.getKey());
                if(control != -1){
                    ChatObject object = snapshot.getValue(ChatObject.class);
                    if(object == null) return;
                    object.setUser_id(snapshot.getKey());
                    object.setName(String.valueOf(snapshot.child("name").getValue()));
                    allMessages.remove(control);
                    adapter.notifyItemRemoved(control);
                    allMessages.add(0,object);
                    adapter.notifyItemInserted(0);
                }
                if(allMessages.size() == 0 || controlP == 0){
                    controlP = 0;
                    setEmptyVisible(false);
                }else setEmptyGone();
            }else setEmptyVisible(false);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private int findPosition(String uid){
        for(int i =0; i<=allMessages.size();i++){
            ChatObject o = allMessages.get(i);
            if(o.getUser_id().equals(uid)){
                return i;
            }
        }
        return -1;
    }

    private void loadMessages() {
        assert mAuth.getCurrentUser() != null;
        mRef = FirebaseDatabase.getInstance().getReference().child("messagesFastAccess").child(mAuth.getCurrentUser().getUid());
        if(!listenerControl){
            listenerControl=true;
            mRef.orderByChild("time").addChildEventListener(childEventListener);
        }
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setEmptyVisible(boolean msgType){
        if(msgType){ //empty
            new Helper().setEmptyView(empty,requireContext(),requireActivity()
                    ,getResources().getString(R.string.no_message),R.drawable.ic_empty);
        }else{ //info
            new Helper().setEmptyView(empty,requireContext(),requireActivity()
                    ,getResources().getString(R.string.no_messages),R.drawable.ic_no_messages);
        }
        empty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void setEmptyGone(){
        empty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void Search(final String searchText) {
        if(allMessages.size() <= 0){
            setEmptyVisible(true);
            return;
        }
        searchList.clear();
        sAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.VISIBLE);
        for(ChatObject o: allMessages){
            if(o.getName().contains(searchText)
                    || o.getName().equals(searchText)){
                searchList.add(o);
                sAdapter.notifyItemInserted(searchList.size()-1);
                progressBar.setVisibility(View.GONE);
            }
        }
        if(searchList.size() <= 0) setEmptyVisible(true);
    }

    /*private void setupAuthListener() {
        FirebaseAuth.AuthStateListener listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser myUser = FirebaseAuth.getInstance().getCurrentUser();
                if (myUser != null) {
                    Intent intent = new Intent(getApplicationContext(), PetZoneActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }*/

    @Override
    public void onPause() {
        super.onPause();
        //activityControl = false;
        allMessages.clear();
        if(listenerControl){
            listenerControl=false;
            mRef.removeEventListener(childEventListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //activityControl = true;
        if(!listenerControl){
            listenerControl=true;
            adapter.notifyDataSetChanged();
            mRef.orderByChild("time").addChildEventListener(childEventListener);
        }
    }

    /*@Override
    protected void onStop() {
        super.onStop();
        activityControl = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityControl = true;
    }*/
}
