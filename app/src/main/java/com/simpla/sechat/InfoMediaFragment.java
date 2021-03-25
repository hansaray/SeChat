package com.simpla.sechat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simpla.sechat.Adapters.MediaAdapter;
import com.simpla.sechat.Extensions.EventBusDataEvents;
import com.simpla.sechat.Objects.MediaObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public class InfoMediaFragment extends Fragment {

    private String uid;
    private ArrayList<MediaObject> list;
    private MediaAdapter adapter;
    private ImageView back;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info_media, container, false);
        findIds(v);
        return v;
    }

    private void findIds(View v){
        back = v.findViewById(R.id.im_back);
        recyclerView = v.findViewById(R.id.im_rw);
        list = new ArrayList<>();
        adapter = new MediaAdapter(getContext(),list);
        setListeners();
        setupRw();
    }

    private void setListeners() {
        back.setOnClickListener(v1 -> {
            requireActivity().findViewById(R.id.info_layout).setVisibility(View.VISIBLE);
            requireActivity().findViewById(R.id.info_frame).setVisibility(View.GONE);
        });
    }

    private void setupRw() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),4));
        recyclerView.setAdapter(adapter);
        loadData();
    }

    private void loadData() {
        assert FirebaseAuth.getInstance().getCurrentUser() != null;
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("messagesMedia")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(uid);
        dRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    list.clear();
                    for(DataSnapshot d : snapshot.getChildren()){
                        for(DataSnapshot d1 : d.getChildren()){
                            MediaObject chatInfoMediaObject = new MediaObject(String.valueOf(d1.getValue()),d1.getKey());
                            list.add(chatInfoMediaObject);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Subscribe(sticky = true)
    public void EventBusTakeUid(EventBusDataEvents.sendMediaPosition data){
        uid = data.getPosition();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }
}
