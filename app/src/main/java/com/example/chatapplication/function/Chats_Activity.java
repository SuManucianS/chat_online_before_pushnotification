package com.example.chatapplication.function;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Adapter.MessageAdapter;
import com.example.chatapplication.MainActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.interfaces.reveicecall;
import com.example.chatapplication.model.callModel;
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
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chats_Activity extends AppCompatActivity {
    private CircleImageView circleImageView;
    private TextView tv_username;
    private Toolbar toolbars;
    private ImageButton imgbtnsend;
    private EditText edtmessage;
    private RecyclerView recycler_message;
    private String useridd;
    private MessageAdapter messageAdapter;
    private List<chatModel> chatlist;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_);
        receiveCall();
        addControls();
        addEvents();
    }

    private void addControls() {
        circleImageView = findViewById(R.id.circle_message_profile);
        tv_username = findViewById(R.id.tvusername_message);
        imgbtnsend = findViewById(R.id.img_btnsend);
        edtmessage = findViewById(R.id.edt_entermessage);
        toolbars = findViewById(R.id.toolbarmessage);
        setSupportActionBar(toolbars);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recycler_message = findViewById(R.id.recycler_message);
        recycler_message.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        manager.setStackFromEnd(true);
        recycler_message.setLayoutManager(manager);
        intent = getIntent();
        useridd = intent.getStringExtra("iduser");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.w("Valueuseroff: ", " " + firebaseUser.getUid());
        Log.w("IDIntent","" + useridd);
    }
    private void addEvents(){
        toolbars.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        reference = FirebaseDatabase.getInstance().getReference("user").child(useridd);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userModel user = dataSnapshot.getValue(userModel.class);
                tv_username.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    circleImageView.setImageResource(R.drawable.ngok);
                }else
                    Glide.with(Chats_Activity.this).load(user.getImageURL()).into(circleImageView);
                readMessage(firebaseUser.getUid(), useridd, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        imgbtnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = edtmessage.getText().toString();
                if (!message.equals("")){
                    sendmessage(firebaseUser.getUid(), useridd, message);
                    edtmessage.setText("");
                }
            }
        });
    }

    private void sendmessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        reference.child("chat").push().setValue(hashMap);
    }


    public void receiveCall() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("call").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    callModel call= dataSnapshot.getValue(callModel.class);
                    Log.w("state: ", "" + call.getReceiving());
                    if (!call.getReceiving().equals("nocall")){
                        Intent intent = new Intent(Chats_Activity.this, ReceiveActivity.class);
                        startActivity(intent);
                    }else {

                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void readMessage(final  String myID, final  String userID,final String imageurl){
        chatlist = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("chat");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatlist.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    chatModel chat = snapshot.getValue(chatModel.class);

                    Log.w("Chat123",": hello " + chat.getMessage());
                    if (chat.getReceiver().equals(userID) && chat.getSender().equals(myID) ||
                            chat.getReceiver().equals(myID) && chat.getSender().equals(userID)){
                        chatlist.add(chat);
                    }

                    messageAdapter = new MessageAdapter(Chats_Activity.this, chatlist, imageurl);
                    recycler_message.setAdapter(messageAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.callchat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.callvideo:
                statecall(useridd, "nocall", firebaseUser.getUid());
                Intent intent = new Intent(Chats_Activity.this, CallActivity.class);
                intent.putExtra("receiveruser", useridd);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void statecall(String uid, String urevceivercall, String idreceive){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("calling", uid);
        hashMap.put("receiving", urevceivercall);
        ref.child("call").child(firebaseUser.getUid()).setValue(hashMap);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("call").child(uid);
        HashMap<String, Object> map = new HashMap<>();
        map.put("receiving", idreceive);
        reference.updateChildren(map);
    }

}
