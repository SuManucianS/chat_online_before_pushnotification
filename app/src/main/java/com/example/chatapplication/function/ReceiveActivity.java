package com.example.chatapplication.function;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.chatapplication.R;
import com.example.chatapplication.model.callModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class ReceiveActivity extends AppCompatActivity {
    private FrameLayout layout_caller, layout_receiver;
    private ImageView img_audio, img_cam, img_endcall;
    private String userid;
    private RtcEngine mRtcEngine;
    private static final int PERMISSON_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
    FirebaseUser firebaseUser;
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupRemotevideoStream(uid); ///////
                }
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft();
                }
            });
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RemoteUserVideoToggle(uid, muted);
                }
            });
        }
    };

    private void RemoteUserVideoToggle(int uid, boolean muted) {
        SurfaceView videoSurfaceView = (SurfaceView) layout_receiver.getChildAt(0);
        videoSurfaceView.setVisibility(muted ? View.GONE: View.VISIBLE); // neu muted gone ko thi visible
        if (muted){
            img_cam = new ImageView(this);
            img_cam.setImageResource(R.drawable.icon_cameraoff);
            layout_receiver.addView(img_cam);
        }else {
            img_cam = (ImageView) layout_receiver.getChildAt(1);
            if (img_cam != null){
                layout_receiver.removeView(img_cam);
            }
        }
    }

    private void onRemoteUserLeft() {
        removeVideo(R.id.videouser_receiver);
    }
    private void removeVideo(int containerID){
        FrameLayout videoContainer = findViewById(containerID);
        videoContainer.removeAllViews();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        checkcall();
        addControls();
        if (checkPermissionRequest(REQUESTED_PERMISSIONS[0], PERMISSON_REQ_ID) && checkPermissionRequest(REQUESTED_PERMISSIONS[1], PERMISSON_REQ_ID)){
            Agorainitid();
        }
        onjoinChanelClicked();
        addEvent();
    }

    private void addControls() {
        layout_caller = findViewById(R.id.videouser_call);
        layout_receiver = findViewById(R.id.videouser_receiver);
        img_audio = findViewById(R.id.audioBtn);
        img_cam = findViewById(R.id.videoBtn);
        img_endcall = findViewById(R.id.leaveBtn);

    }

    private void addEvent() {
        img_endcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //updatecallstatus("nocall", "nocall");
                mRtcEngine.leaveChannel();
                removeVideo(R.id.videouser_receiver);
                removeVideo(R.id.videouser_call);

                DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("call").child(firebaseUser.getUid());
                HashMap<String, Object> Map = new HashMap<>();
                Map.put("calling","nocall");
                Map.put("receiving","nocall");
                reference1.updateChildren(Map);
                finish();
            }
        });
        img_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (img_audio.isSelected()){
                    img_audio.setSelected(false);
                    img_audio.setImageResource(R.drawable.icon_microphone);
                }else {
                    img_audio.setSelected(true);
                    img_audio.setImageResource(R.drawable.icon_microoff);
                }
                mRtcEngine.muteLocalAudioStream(img_audio.isSelected());
            }
        });
        img_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (img_cam.isSelected()){
                    img_cam.setSelected(false);
                    img_cam.setImageResource(R.drawable.icon_camera);
                }else {
                    img_cam.setSelected(true);
                    img_cam.setImageResource(R.drawable.icon_cameraoff);
                }
                mRtcEngine.muteLocalVideoStream(img_cam.isSelected());
                layout_caller.setVisibility(img_cam.isSelected() ? View.GONE : View.VISIBLE);
                SurfaceView surfaceView = (SurfaceView) layout_caller.getChildAt(0);
                surfaceView.setZOrderMediaOverlay(!img_cam.isSelected());
                surfaceView.setVisibility(img_cam.isSelected() ? View.GONE : View.VISIBLE);
            }
        });
    }
    private void onjoinChanelClicked(){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("call").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callModel call = dataSnapshot.getValue(callModel.class);
                if (!call.getReceiving().equals("nocall")){
                    mRtcEngine.joinChannel(null, call.getReceiving(),"123", 0);
                    setupLocalVideouser();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public boolean checkPermissionRequest(String permission, int requestCode){
        Log.i("Checkkkk", "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }else
            return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("Check result Permisson", "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);
        switch (requestCode){
            case PERMISSON_REQ_ID: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED){
                    Log.d("CHEckk per", "need per" + Manifest.permission.CAMERA + "\n" + Manifest.permission.RECORD_AUDIO);
                    break;
                }
                Agorainitid();

            }
        }
    }

    private void Agorainitid() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_id), mRtcEventHandler);

        }catch (Exception ex){
            Log.e("Log ex", Log.getStackTraceString(ex));
            throw new RuntimeException("Check rtc sdk " + Log.getStackTraceString(ex));
        }
        setupSession();
    }

    private void setupSession() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION); // create chanel chat.
        mRtcEngine.enableVideo();
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_1920x1080, VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT)); // fixvideo,
    }
    private void setupLocalVideouser(){
        SurfaceView surfacevideo = RtcEngine.CreateRendererView(getBaseContext());
        surfacevideo.setZOrderMediaOverlay(true); // not under
        layout_caller.addView(surfacevideo);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfacevideo, VideoCanvas.RENDER_MODE_FIT, 0));
    }
    private void setupRemotevideoStream(int uid){
        SurfaceView videoSurface = RtcEngine.CreateRendererView(getBaseContext());
        layout_receiver.addView(videoSurface);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT, uid));
        mRtcEngine.setRemoteSubscribeFallbackOption(Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY);  // not under
    }
    private void checkcall(){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("call").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callModel model = dataSnapshot.getValue(callModel.class);
                Log.w("staterece", "" + model.getReceiving());
                if (model.getReceiving().equals("nocall")){
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("call").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("receiving","nocall");
        reference.updateChildren(hashMap);
    }
}
