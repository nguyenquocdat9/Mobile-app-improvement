package com.example.myapplication.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.Model.Conversation.Conversation;
import com.example.myapplication.data.Model.Conversation.Message;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.ConversationViewHolder> {

    private final List<Conversation> conversationList;
    private final String currentUserId;
    private final OnConversationClickListener listener;
    private final SimpleDateFormat timeFormat;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationListAdapter(List<Conversation> conversationList, String currentUserId, OnConversationClickListener listener) {
        this.conversationList = conversationList;
        this.currentUserId = currentUserId;
        this.listener = listener;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);
        holder.bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatarImageView;
        private final TextView nameTextView;
        private final TextView messageTextView;
        private final TextView timeTextView;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.conversationAvatar);
            nameTextView = itemView.findViewById(R.id.conversationName);
            messageTextView = itemView.findViewById(R.id.lastMessage);
            timeTextView = itemView.findViewById(R.id.messageTime);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onConversationClick(conversationList.get(position));
                }
            });
        }

        void bind(Conversation conversation) {
            // Set the conversation name
            nameTextView.setText(conversation.name);

            // Set avatar using Glide
            if (conversation.avatar_url != null && !conversation.avatar_url.isEmpty()) {
                Glide.with(avatarImageView.getContext())
                        .load(conversation.avatar_url)
                        .placeholder(R.drawable.user)
                        .error(R.drawable.user) // Fallback image khi load lỗi
                        .circleCrop() // Optional: Crop ảnh thành hình tròn
                        .into(avatarImageView);
            } else {
                avatarImageView.setImageResource(R.drawable.user);
            }

            // Get the last message if available
            if (conversation.messages != null && !conversation.messages.isEmpty()) {
                Message lastMessage = conversation.messages.get(conversation.messages.size() - 1);

                // Format the message prefix based on sender
                String prefix;
                if (lastMessage.sender_id.equals(conversation.host_id)) {
                    prefix = "Chủ: ";
                } else {
                    prefix = "Khách: ";
                }

                // Set the last message text
                messageTextView.setText(prefix + lastMessage.message_content);

                // Set the time if available
                if (lastMessage.time != null) {
                    timeTextView.setText(timeFormat.format(lastMessage.time));
                } else {
                    timeTextView.setText("");
                }
            } else {
                // No messages yet
                messageTextView.setText("No messages yet");
                timeTextView.setText("");
            }
        }
    }
}
