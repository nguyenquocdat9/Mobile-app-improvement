package com.example.myapplication.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.Model.Conversation.Message;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<Message> messageList;
    private final String currentUserId; // userId hiện tại
    private final String hostId;
    private final String guestId;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
    private final String partnerAvatar;

    public MessageAdapter(List<Message> messageList, String currentUserId, String hostId, String guestId, String partnerAvatar) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.hostId = hostId;
        this.guestId = guestId;
        this.partnerAvatar = partnerAvatar;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.sender_id != null && message.sender_id.equals(currentUserId)) {
            return 1; // Tin nhắn mình gửi
        } else {
            return 2; // Tin nhắn người khác gửi
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        // Xác định người gửi là Host hay Guest
        String senderRole;
        if (message.sender_id != null) {
            if (message.sender_id.equals(hostId)) {
                senderRole = "Chủ";
            } else if (message.sender_id.equals(guestId)) {
                senderRole = "Khách";
            } else {
                senderRole = "Unknown";
            }
        } else {
            senderRole = "Unknown";
        }

        holder.textSender.setText(senderRole);
        holder.textMessage.setText(message.message_content);

        if (message.time != null) {
            holder.textTime.setText(dateTimeFormat.format(message.time));
        } else {
            holder.textTime.setText("");
        }

        moreInfoSetup(holder, position);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textSender, textMessage, textTime;
        View senderLayout;
        CircleImageView parterAvatarView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textSender = itemView.findViewById(R.id.textSender);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTime = itemView.findViewById(R.id.textTime);
            senderLayout = itemView.findViewById(R.id.senderLayout);
            parterAvatarView = itemView.findViewById(R.id.senderAvatar);
        }
    }

    public void moreInfoSetup(@NonNull  MessageViewHolder holder, int position) {
        String senderID = messageList.get(position).sender_id;
        //show time
        if (position == messageList.size() - 1 || !messageList.get(position + 1).sender_id.equals(senderID)) {
            holder.textTime.setVisibility(View.VISIBLE);
        } else {
            holder.textTime.setVisibility(View.GONE);
        }

        if (getItemViewType(position) == 1) {
            holder.textSender.setVisibility(View.GONE);
            holder.parterAvatarView.setVisibility(View.GONE);
        } else {
            holder.senderLayout.setVisibility(View.GONE);

            if (holder.textTime.getVisibility() == View.VISIBLE) {
                for (int i = messageList.size() - 1; i >= position; i--) {
                    if(messageList.get(i).sender_id.equals(senderID)) {
                        if (i == position) {
                            Glide.with(holder.itemView)
                                    .load(partnerAvatar)
                                    .placeholder(R.drawable.avatar_placeholder)
                                    .error(R.drawable.avatar_placeholder) // Fallback image khi load lỗi
                                    .circleCrop() // Optional: Crop ảnh thành hình tròn
                                    .into(holder.parterAvatarView);

                            holder.senderLayout.setVisibility(View.VISIBLE);
                            holder.textSender.setVisibility(View.VISIBLE);
                            holder.parterAvatarView.setVisibility(View.VISIBLE);
                        }

                        break;
                    }
                }
            }
        }
    }
}
