package com.example.myapplication.data.Repository.Storage;

import android.content.Context;
import android.net.Uri;

import com.example.myapplication.data.Repository.FirebaseService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StorageRepository {
    private final FirebaseStorage storage;
    private final StorageReference storageReference;
    public StorageRepository(Context context) {
        this.storage = FirebaseService.getInstance(context).getStorage();
        storageReference = storage.getReference();
    }

    public void uploadMainImage(String house_id, Uri imageUri, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = storageReference.child("houses/" + house_id + "/main_photo/" + fileName);

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    String url = downloadUri.toString();
                                    onSuccess.onSuccess(url); // Trả URL về
                                })
                                .addOnFailureListener(onFailure)
                )
                .addOnFailureListener(onFailure);
    }

    public void uploadHouseSubImages(String house_id, List<Uri> imageUris, OnSuccessListener<List<String>> onComplete, OnFailureListener onFailure) {
        List<String> downloadUrls = new ArrayList<>();
        int total = imageUris.size();

        if (total == 0) {
            onComplete.onSuccess(downloadUrls);
            return;
        }

        for (Uri uri : imageUris) {
            String fileName = UUID.randomUUID().toString() + ".jpg";
            StorageReference fileRef = storageReference.child("houses/" + house_id + "/sub_photos/" + fileName);

            fileRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot ->
                            fileRef.getDownloadUrl()
                                    .addOnSuccessListener(downloadUri -> {
                                        downloadUrls.add(downloadUri.toString());
                                        if (downloadUrls.size() == total) {
                                            onComplete.onSuccess(downloadUrls);
                                        }
                                    })
                                    .addOnFailureListener(onFailure)
                    )
                    .addOnFailureListener(onFailure);
        }
    }

    public void uploadUserAvatar(String uid, Uri avatar_uri, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference avatarRef = storageReference.child("avatars/" + "user_" + uid + "/" + fileName);

        avatarRef.putFile(avatar_uri)
                .addOnSuccessListener(taskSnapshot -> avatarRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            String url = downloadUri.toString();
                            onSuccess.onSuccess(url);
                        })
                        .addOnFailureListener(onFailure)
                )
                .addOnFailureListener(onFailure);
    }
}
