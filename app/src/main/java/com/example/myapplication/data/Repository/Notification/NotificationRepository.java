package com.example.myapplication.data.Repository.Notification;

import android.content.Context;
import android.util.Log;

import com.example.myapplication.data.Repository.FirebaseService;
import com.example.myapplication.data.Repository.User.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;

public class NotificationRepository {
    private final FirebaseMessaging firebaseMessaging;
    private final UserRepository userRepository;
    private static final String TAG = "NotificationService";

    public NotificationRepository(Context context) {
        this.firebaseMessaging = FirebaseService.getInstance(context).getMessaging();
        this.userRepository = new UserRepository(context);
    }

    public void fetchFCMToken(String userID) {
        firebaseMessaging.getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Lấy token mới FCM
                    String token = task.getResult();

                    // Log token
                    Log.d(TAG, "FCM Token for user " + userID + ": " + token);

                    // Lưu token vào Firestore thông qua UserRepository
                    userRepository.setFCMToken(userID, token,
                            aVoid -> Log.d(TAG, "FCM token saved successfully for user: " + userID),
                            e -> Log.w(TAG, "Failed to save FCM token for user: " + userID, e));
                });
    }

    public void deleteFCMToken(String userId) {
        userRepository.deleteFCMToken(userId,
                aVoid -> Log.d(TAG, "FCM token deleted (set to null) for user: " + userId),
                e -> Log.w(TAG, "Failed to delete FCM token for user: " + userId, e));
    }
}
