package com.example.chatapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.R;
import com.example.chatapplication.model.chatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private Context context;
    private List<chatModel> chat;
    public static final int MSG_BACKGROUND_LEFT = 0;
    public static final int MSG_BACKGROUND_RIGHT = 1;
    private String img_url;
    FirebaseUser firebaseUser;
    public MessageAdapter(Context context, List<chatModel> chat, String img_url) {
        this.context = context;
        this.chat = chat;
        this.img_url = img_url;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_BACKGROUND_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        chatModel chats = chat.get(position);
        holder.tv_showmessage.setText(chats.getMessage());
        if (img_url.equals("default")){
            holder.profile_imgprofile.setImageResource(R.drawable.ngok);
        }else
            Glide.with(context).load(img_url).into(holder.profile_imgprofile);
    }

    @Override
    public int getItemCount() {
        return chat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView tv_showmessage;
        public ImageView profile_imgprofile;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_showmessage = itemView.findViewById(R.id.showmessage);
            profile_imgprofile = itemView.findViewById(R.id.profile_imguser);

        }
    }
    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chat.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_BACKGROUND_RIGHT;
        }else {
            return MSG_BACKGROUND_LEFT;
        }
    }
}
