package com.example.myapplication.data.Repository.Auth;

import android.content.Context;

import com.example.myapplication.data.Model.Auth.AuthLogin;
import com.example.myapplication.data.Model.Auth.AuthRegister;
import com.example.myapplication.data.Model.User.User;
import com.example.myapplication.data.Repository.FirebaseService;
import com.example.myapplication.data.Repository.Notification.NotificationRepository;
import com.example.myapplication.data.Repository.User.UserRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public AuthRepository(Context context) {
        this.firebaseAuth = FirebaseService.getInstance(context).getAuth();
        this.userRepository = new UserRepository(context);
        this.notificationRepository = new NotificationRepository(context);
    }

    // Đăng ký
    public void register(AuthRegister authInformation, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        firebaseAuth.createUserWithEmailAndPassword(authInformation.email, authInformation.password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            User newUser = new User(uid, authInformation.full_name, authInformation.phone_number);

                            userRepository.createUser(
                                    newUser,
                                    unused -> onSuccess.onSuccess(null),
                                    e -> onFailure.onFailure(new Exception("Đăng ký thành công nhưng lưu user thất bại: " + e.getMessage()))
                            );
                        } else {
                            onFailure.onFailure(new Exception("Đăng ký thành công nhưng không lấy được FirebaseUser"));
                        }
                    } else {
                        onFailure.onFailure(task.getException());
                    }
                });
    }

    //Đăng nhập
    public void login(AuthLogin authInformation, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        firebaseAuth.signInWithEmailAndPassword(authInformation.email, authInformation.password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        onSuccess.onSuccess(null); // Đăng nhập không có dữ liệu trả về
                    } else {
                        onFailure.onFailure(task.getException());
                    }
                });
    }

    public void resetPassword(String email, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public String getUserUid() {
        return this.firebaseAuth.getUid();
    }

    public FirebaseUser getCurrentUser() {
        return this.firebaseAuth.getCurrentUser();
    }
    public boolean checkLogin() {
        return this.firebaseAuth.getCurrentUser() != null;
    }

    // Dang nhap bang Google

    public void loginWithGoogleAccount(GoogleSignInAccount account, OnSuccessListener<AuthResult> onSuccess, OnFailureListener onFailure) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void logout() {
        firebaseAuth.signOut();
        this.notificationRepository.deleteFCMToken(this.getUserUid());
    }

}
