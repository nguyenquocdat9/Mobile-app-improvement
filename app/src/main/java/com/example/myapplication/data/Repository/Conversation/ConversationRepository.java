package com.example.myapplication.data.Repository.Conversation;

import android.content.Context;

import com.example.myapplication.data.Model.Booking.Booking;
import com.example.myapplication.data.Model.Conversation.Conversation;
import com.example.myapplication.data.Model.Conversation.Message;
import com.example.myapplication.data.Repository.FirebaseService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationRepository {
    private final FirebaseFirestore db;
    private final String COLLECTION_NAME = "conversations";
    public ConversationRepository(Context context) {
        this.db = FirebaseService.getInstance(context).getFireStore();
    }

    public void createConversation(Conversation conversation, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.db.collection(COLLECTION_NAME).document(conversation.id).set(conversation)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void getAllConversationByHostID(String userID, OnSuccessListener<List<Conversation>> onSuccess, OnFailureListener onFailure) {
        this.db.collection(COLLECTION_NAME).whereEqualTo("host_id", userID).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Conversation> conversationList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Conversation conversation = document.toObject(Conversation.class);
                        conversationList.add(conversation);
                    }
                    conversationList.sort((c1, c2) -> {
                        Date time1 = c1.messages.get(c1.messages.size() - 1).time;
                        Date time2 = c2.messages.get(c2.messages.size() - 1).time;
                        return time2.compareTo(time1);
                    });
                    onSuccess.onSuccess(conversationList);
                })
                .addOnFailureListener(onFailure);
    }

    public void getAllConversationByGuestID(String userID, OnSuccessListener<List<Conversation>> onSuccess, OnFailureListener onFailure) {
        this.db.collection(COLLECTION_NAME).whereEqualTo("guest_id", userID).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Conversation> conversationList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Conversation conversation = document.toObject(Conversation.class);
                        conversationList.add(conversation);
                    }
                    conversationList.sort((c1, c2) -> {
                        Date time1 = c1.messages.get(c1.messages.size() - 1).time;
                        Date time2 = c2.messages.get(c2.messages.size() - 1).time;
                        return time2.compareTo(time1);
                    });
                    onSuccess.onSuccess(conversationList);
                })
                .addOnFailureListener(onFailure);
    }

    public void getConversationById(String conversationID, OnSuccessListener<Conversation> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .document(conversationID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Conversation conversation = documentSnapshot.toObject(Conversation.class);
                        if (conversation != null) {
                            onSuccess.onSuccess(conversation);
                        } else {
                            onFailure.onFailure(new Exception("Property is null"));
                        }
                    } else {
                        onFailure.onFailure(new Exception("Property not found"));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void sendMessage(String conversationID, Message message, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.db.collection(COLLECTION_NAME)
                .document(conversationID)
                .update("messages", FieldValue.arrayUnion(message))
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void deleteConversation(String conversationID, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.db.collection(COLLECTION_NAME).document(conversationID).delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public ListenerRegistration listenForNewMessages(String conversationID, OnSuccessListener<Message> onNewMessage, OnFailureListener onFailure) {
        // Lắng nghe sự thay đổi trong tài liệu Conversation
        return db.collection(COLLECTION_NAME)
                .document(conversationID)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        onFailure.onFailure(error);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Lấy dữ liệu Conversation từ documentSnapshot
                        Conversation conversation = documentSnapshot.toObject(Conversation.class);
                        if (conversation != null && conversation.messages != null) {
                            // Kiểm tra xem có tin nhắn mới trong danh sách messages không
                            Message lastMessage = conversation.messages.get(conversation.messages.size() - 1);
                            // Gọi callback trả về tin nhắn mới
                            onNewMessage.onSuccess(lastMessage);
                        }
                    }
                });
    }
}
