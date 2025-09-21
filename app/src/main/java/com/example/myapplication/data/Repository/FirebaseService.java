package com.example.myapplication.data.Repository;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseService {
    private static FirebaseService instance;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseStorage firebaseStorage;
    private final FirebaseMessaging firebaseMessaging;
    private FirebaseService (Context context) {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseMessaging = FirebaseMessaging.getInstance();
    }

    public static synchronized FirebaseService getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseService(context);
        }
        return instance;
    }

    public FirebaseFirestore getFireStore() {
        return firestore;
    }

    public FirebaseAuth getAuth() {
        return firebaseAuth;
    }

    public FirebaseStorage getStorage() {
        return firebaseStorage;
    }

    public FirebaseMessaging getMessaging() { return firebaseMessaging; }

    public StorageReference getStorageReference() {
        return firebaseStorage.getReference();
    }
}
