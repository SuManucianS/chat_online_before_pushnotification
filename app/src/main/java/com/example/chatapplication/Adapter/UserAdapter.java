package com.example.chatapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.R;
import com.example.chatapplication.function.Chats_Activity;
import com.example.chatapplication.model.userModel;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context;
    private List<userModel> users;
    private boolean chatstus;

    public UserAdapter(Context context, List<userModel> users) {
        this.context = context;
        this.users = users;
        //this.chatstus = chatstus;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_itemuser_offragmentuser, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder viewHolder, int position) {
        final userModel user = users.get(position);
        viewHolder.tv_usernameprofile.setText(user.getUsername());
        if (user.getImageURL().equals("default")){
            viewHolder.imgprofile.setImageResource(R.drawable.ngok);
        }else
        {
            Glide.with(context).load(user.getImageURL()).into(viewHolder.imgprofile);
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Chats_Activity.class); // gan context thay cho acti hien thoi
                intent.putExtra("iduser", user.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView tv_usernameprofile;

        public ImageView imgprofile;
        public ImageView status_on, status_off;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_usernameprofile = itemView.findViewById(R.id.tvusername);
            imgprofile = itemView.findViewById(R.id.circleimage_user);
            status_on = itemView.findViewById(R.id.status_user_on);
            status_off = imgprofile.findViewById(R.id.status_user_off);
        }
    }
}
