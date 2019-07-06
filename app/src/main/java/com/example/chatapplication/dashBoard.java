package com.example.chatapplication;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Fragment.FragmentChat;
import com.example.chatapplication.Fragment.FragmentProfile;
import com.example.chatapplication.Fragment.Fragment_User;
import com.example.chatapplication.function.Chats_Activity;
import com.example.chatapplication.function.ReceiveActivity;
import com.example.chatapplication.model.callModel;
import com.example.chatapplication.model.userModel;
import com.example.chatapplication.services.userService;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class dashBoard extends AppCompatActivity {
    private CircleImageView circleimageuser;
    private TextView tvuser;
    private Toolbar toolbar;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    userService us;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.requestFeature(Window.FEATURE_NO_TITLE | Window.FEATURE_ACTIVITY_TRANSITIONS);
            w.setEnterTransition(new Slide());
            w.setExitTransition(new Slide());
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        setContentView(R.layout.activity_dash_board);
        receiveCall();
        getView();
        bindEvent();
    }

    private void getView() {
        us = new userService(this, this);
        circleimageuser = findViewById(R.id.circleimage_user);
        tvuser = findViewById(R.id.tvusername);
        ViewPager viewPager = findViewById(R.id.viewpaper);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        ViewPaperAdapter viewPaperAdapter = new ViewPaperAdapter(getSupportFragmentManager());
//        viewPaperAdapter.addFragment(new ChatsFragment(), "Chats");
//        viewPaperAdapter.addFragment(new UsersFragment(), "Users");
//        viewPaperAdapter.addFragment(new ProfileFragment(), "Profile");
//        viewPager.setAdapter(viewPaperAdapter);

        FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return 3; // tao so luong fragment
            }

            @Override
            public Fragment getItem(int position) {
                Fragment fragment = null;
                switch (position){
                    case 0:
                        fragment = new Fragment_User();
                        break;
                    case 1:
                        fragment = new FragmentChat();
                        break;
                    case 2:
                        fragment = new FragmentProfile();
                        break;
                }
                return fragment;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                switch (position){
                    case 0:
                        return "User";
                    case 1:
                        return "Chat";
                    case 2:
                        return  "Profile";
                }
                return null;
            }
        };
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout =  findViewById(R.id.tablayyout);
        tabLayout.setupWithViewPager(viewPager);
        }



    private void bindEvent() {
        Intent intent = getIntent();
        String mId = intent.getStringExtra("id");
        us.validateUser(mId);
        Log.w("IDDDD", " " + mId);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("user").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userModel user = dataSnapshot.getValue(userModel.class);
                tvuser.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    circleimageuser.setImageResource(R.drawable.ngok);
                }else{
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(circleimageuser);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
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
                    Intent intent = new Intent(dashBoard.this, ReceiveActivity.class);
                    startActivity(intent);
                }else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
