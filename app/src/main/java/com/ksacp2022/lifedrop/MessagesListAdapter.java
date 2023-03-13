package com.ksacp2022.lifedrop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessagesListAdapter extends RecyclerView.Adapter<MessageCard> {
    List<Message> messageList;
    Context context;
    FirebaseAuth firebaseAuth;

    public MessagesListAdapter(List<Message> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
        firebaseAuth=FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MessageCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_card,parent,false);
        return new MessageCard(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageCard holder, int position) {
        Message message= messageList.get(position);
        String uid= firebaseAuth.getUid();
        //if the current user is the sender
        if(message.getFrom().equals(uid))
        {
            //hide receiver text
            holder.receiver_message.setVisibility(View.GONE);
            holder.sender_message.setText(message.getText());
        }
        else{
            //hide sender text
            holder.sender_message.setVisibility(View.GONE);
            holder.receiver_message.setText(message.getText());
        }


    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}

class MessageCard extends RecyclerView.ViewHolder{
    TextView sender_message,receiver_message;

    public MessageCard(@NonNull View itemView) {
        super(itemView);
        sender_message=itemView.findViewById(R.id.sender_message);
        receiver_message=itemView.findViewById(R.id.receiver_message);
    }
}
