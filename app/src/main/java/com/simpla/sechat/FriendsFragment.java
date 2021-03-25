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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simpla.sechat.Adapters.FriendsAdapter;
import com.simpla.sechat.Extensions.Helper;
import com.simpla.sechat.Objects.FriendsObject;

import java.util.ArrayList;

public class FriendsFragment extends Fragment {

    private RecyclerView recyclerView,searchRw;
    private FriendsAdapter adapter,sAdapter;
    private ArrayList<FriendsObject> list,searchList;
    private View empty;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private SearchView searchView;
    private TextView searchTxt,title;
    private AppBarLayout appBar;
    private ImageView add;
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
        empty = v.findViewById(R.id.empty_view_layout);
        searchView = v.findViewById(R.id.list_search);
        searchTxt = v.findViewById(R.id.list_search_txt);
        title = v.findViewById(R.id.list_title);
        appBar = v.findViewById(R.id.list_app_bar);
        add = v.findViewById(R.id.list_add);
        searchRw = v.findViewById(R.id.list_search_rw);
        progressBar = v.findViewById(R.id.list_progress);
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference().child("users");
        list = new ArrayList<>();
        searchList = new ArrayList<>();
        sAdapter = new FriendsAdapter(getContext(),searchList);
        adapter = new FriendsAdapter(getContext(),list);
        smallSettings();
    }

    private void smallSettings(){
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.mainBlue),
                android.graphics.PorterDuff.Mode.MULTIPLY);
        searchTxt.setText(getResources().getString(R.string.search_friends));
        searchView.setQueryHint(getResources().getString(R.string.search_friends));
        title.setText(getResources().getString(R.string.friends));
        add.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_add_friends,requireActivity().getTheme()));
        if(Build.VERSION.SDK_INT >= 21) appBar.setOutlineProvider(null);
        searchView.setOnClickListener(v1 -> {
            searchTxt.setVisibility(View.GONE);
            searchView.setIconified(false);
        });
        add.setOnClickListener(view -> startActivity(new Intent(getContext(),AddActivity.class).putExtra("activity",true)));
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
                    searchList.clear();
                    if(list.size() <= 0) setEmptyVisible(false);
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
        loadFriends();
    }

    private void loadFriends() {
        progressBar.setVisibility(View.VISIBLE);
        if(mAuth.getCurrentUser() == null) return;
        mRef.child(mAuth.getCurrentUser().getUid()).child("myList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot d: snapshot.getChildren()){
                        if(d.getKey() == null) return;
                        mRef.child(d.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    FriendsObject object = new FriendsObject(d.getKey()
                                            ,String.valueOf(snapshot.child("nickname").getValue())
                                            ,String.valueOf(snapshot.child("imageURL").getValue())
                                            ,String.valueOf(d.getValue()),1);
                                    list.add(object);
                                    adapter.notifyItemInserted(list.size()-1);
                                }
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }else setEmptyVisible(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void Search(final String searchText) {
        if(list.size() <= 0) {
            setEmptyVisible(true);
            return;
        }
        searchList.clear();
        sAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.VISIBLE);
        for(FriendsObject o: list){
            if(o.getNickname().contains(searchText)
                    || o.getNickname().equals(searchText)){
                searchList.add(o);
                sAdapter.notifyItemInserted(searchList.size()-1);
                progressBar.setVisibility(View.GONE);
            }
        }
        if(searchList.size() <= 0) setEmptyVisible(true);
    }

    private void setEmptyVisible(boolean msgType){
        if(msgType){ //empty
            new Helper().setEmptyView(empty,requireContext(),requireActivity()
                    ,getResources().getString(R.string.new_msg_no),R.drawable.ic_empty);
        }else{ //info
            new Helper().setEmptyView(empty,requireContext(),requireActivity()
                    ,getResources().getString(R.string.no_friends),R.drawable.ic_no_friends);
        }
        empty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }
}
