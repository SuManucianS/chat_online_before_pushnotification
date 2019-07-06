package com.example.chatapplication.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapplication.Adapter.UserAdapter;
import com.example.chatapplication.R;
import com.example.chatapplication.model.userModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Fragment_User extends Fragment {
    private static final String key_fragment = "key_frag";
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<userModel> fusers;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment__user, container, false);
        recyclerView = view.findViewById(R.id.recyclerview_user);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fusers = new ArrayList<>();

        // Inflate the layout for this fragment;
        readuser();
        return view;
    }
    public static Fragment_User newInstances (int idfrag){
        Fragment_User fragment = new Fragment_User();
        Bundle args = new Bundle();
        args.putInt(key_fragment,idfrag);
        fragment.setArguments(args);
        return fragment;
    }
    private void readuser(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fusers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    userModel user = snapshot.getValue(userModel.class);
                    Log.w("infouser", "AAAAAAAAAAAAAAAAAA OUTPUT : " + user.getId() + "\n" + user.getUsername());
                    assert user != null;
                    assert firebaseUser != null;
                    if (!user.getId().equals(firebaseUser.getUid())){
                        fusers.add(user);
                    }
                }
                userAdapter = new UserAdapter(getContext(), fusers);
                recyclerView.setAdapter(userAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

}
