package com.example.chatapplication.Fragment;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapplication.R;
import com.example.chatapplication.model.userModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class FragmentProfile extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final String key_fragment = "key_frag";
    private CircleImageView imageprofile;
    private TextView tv_nameprofile;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageuri;
    private StorageTask uploadtask;
    StorageReference storageReference;
    DatabaseReference reference;
    FirebaseUser firebaseUser;


    public static FragmentProfile newInstance(int idfrag) {
        FragmentProfile fragment = new FragmentProfile();
        Bundle args = new Bundle();
        args.putInt(key_fragment, idfrag);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_profile, container, false);
        imageprofile = view.findViewById(R.id.circleimage_profile);
        tv_nameprofile = view.findViewById(R.id.tv_profilename);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("user").child(firebaseUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference("Upload_Images");
        //Log.d("ABC1234", firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                userModel user = dataSnapshot.getValue(userModel.class);
                tv_nameprofile.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    imageprofile.setImageResource(R.drawable.ngok);
                }else
                {
                    Glide.with(getContext()).load(user.getImageURL()).into(imageprofile);
                }
                //Log.d("ABC", user.getUsername());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        imageprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImages();
            }
        });
        return view;
    }

    private void openImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData()!= null){
            imageuri = data.getData();
            if (uploadtask != null && uploadtask.isInProgress()){
                Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
            }else
                UploadImage();
        }
    }

    private String getFileExtension(Uri uri){ // lay duoi uri
        ContentResolver resolver = getContext().getContentResolver();
        MimeTypeMap typeMap = MimeTypeMap.getSingleton();
        return typeMap.getExtensionFromMimeType(resolver.getType(uri));
    }
    private void UploadImage(){
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Đang tải ảnh đẹp lên ahihi");
        progressDialog.show();
        if (imageuri !=null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageuri));
            // lay duoi timesystem + duoi uri  day len storage
            uploadtask = fileReference.putFile(imageuri); // put anh + dinh dang uri len
            uploadtask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String muri = downloadUri.toString();
                        reference = FirebaseDatabase.getInstance().getReference("user").child(firebaseUser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", muri);
                        reference.updateChildren(map);
                        progressDialog.dismiss();
                    }
                    else
                    {
                        Toast.makeText(getContext(), "Loi", Toast.LENGTH_SHORT).show();
                        Log.w("LOIABC : ", task.getException());
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }else {
            Toast.makeText(getContext(), "khong co 1 anh nao dc chon", Toast.LENGTH_SHORT).show();
        }
    }

}
