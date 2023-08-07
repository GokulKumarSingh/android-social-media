package com.example.firechat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firechat.R;
import com.example.firechat.model.Message;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Message> messageArrayList;
    String selfUuid;

    int ITEM_SEND = 0;
    int ITEM_RECEIVE = 1;

    public MessageAdapter(Context context, ArrayList<Message> messageArrayList, String selfUuid) {
        this.context = context;
        this.messageArrayList = messageArrayList;
        this.selfUuid = selfUuid;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.send_layout, parent, false);
            return new SenderViewHolder(view);
        } else if (viewType == ITEM_RECEIVE) {
            return new ReceiverViewHolder(LayoutInflater.from(context).inflate(R.layout.recived_layout, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageArrayList.get(position);
        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder senderViewHolder = (SenderViewHolder) holder;
            senderViewHolder.senderText.setText(message.getData());
            senderViewHolder.senderTime.setText(message.getTime());
        } else{
            ReceiverViewHolder receiverViewHolder = (ReceiverViewHolder) holder;
            receiverViewHolder.receiverText.setText(message.getData());
            receiverViewHolder.receiverTime.setText(message.getTime());
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageArrayList.get(position);
        if(message.getSenderId().equals(selfUuid)){
            return ITEM_SEND;
        }else{
            return ITEM_RECEIVE;
        }
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView senderText, senderTime, deliveryStatus;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderText = itemView.findViewById(R.id.senderMessageText);
            senderTime = itemView.findViewById(R.id.senderTimeStamp);
            deliveryStatus = itemView.findViewById(R.id.txt_deliveryStatus);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView receiverText, receiverTime;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverText = itemView.findViewById(R.id.receiverText);
            receiverTime = itemView.findViewById(R.id.receiverTimeStamp);
        }
    }
}
