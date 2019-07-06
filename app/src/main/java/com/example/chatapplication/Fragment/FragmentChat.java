package com.example.chatapplication.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.solver.widgets.Snapshot;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapplication.Adapter.UserAdapter;
import com.example.chatapplication.R;
import com.example.chatapplication.model.chatModel;
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


public class FragmentChat extends Fragment {
    private static final String key_fragment = "key_frag";
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<String> userlist;
    private ArrayList<userModel> users;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_chat, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_fragmentchat);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("chat");
        userlist = new ArrayList<>();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userlist.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    chatModel chats =  snapshot.getValue(chatModel.class);
                    Log.w("sender: ", "" + chats.getSender());
                    if (chats.getSender().equals(firebaseUser.getUid())){
                        userlist.add(chats.getReceiver());
                    }
                    if (chats.getReceiver().equals(firebaseUser.getUid())){
                        userlist.add(chats.getSender());
                    }
                    Log.w("ID123: ", "" + userlist); // list ra ng da nhan tin
                    readchat();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }
    private void readchat() {
        users = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("user");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    userModel user = snapshot.getValue(userModel.class);
                    // hien thi  1 ban chat da chat trong danh sach
                    for (String id : userlist){ // duyet id trong userlist cac ten ng nhan
                        if (user.getId().equals(id)){ // neu co id nao trong do trung voi user trong node user
//                            if (users.size()!=0){
//                                for (userModel userModel : users){
//                                    if (!user.getId().equals(userModel.getId())){
//                                        users.add(user);
//                                    }
//                                }
//                            }else {
//                                users.add(user);
//                            }
                                users.add(user);
//
                        }
                    }
                }
                userAdapter = new UserAdapter(getContext(), users);
                recyclerView.setAdapter(userAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public static FragmentChat newInstances(int idfrag){
        FragmentChat fragment = new FragmentChat();
        Bundle args = new Bundle();
        args.putInt(key_fragment,idfrag);
        fragment.setArguments(args);
        return fragment;
    }

}
