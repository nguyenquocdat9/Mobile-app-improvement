package com.example.myapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.Model.User.User;
import com.example.myapplication.data.Repository.Auth.AuthRepository;
import com.example.myapplication.data.Repository.Notification.NotificationRepository;
import com.example.myapplication.data.Repository.User.UserRepository;
import com.example.myapplication.ui.activities.MainActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class CompleteProfile extends AppCompatActivity {

    private TextInputLayout fullNameLayout, phoneNumberLayout;
    private EditText fullNameEditText, phoneNumberEditText;
    private Button submitButton;

    private AuthRepository authRepository;
    private UserRepository userRepository;
    private NotificationRepository notificationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complete_profile);

        notificationRepository = new NotificationRepository(this);
        authRepository = new AuthRepository(this);
        final String uID = authRepository.getUserUid();
        userRepository = new UserRepository(this);

        // Initialize views
        fullNameLayout = findViewById(R.id.fullNameLayout);
        phoneNumberLayout = findViewById(R.id.phoneNumberLayout);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        submitButton = findViewById(R.id.submitButton);

        // Set click listener for submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    // Process the form data
                    String fullName = fullNameEditText.getText().toString().trim();
                    String phoneNumber = phoneNumberEditText.getText().toString().trim();
                    User user = new User(uID, fullName, phoneNumber);
                    // Here you would typically save the data to your backend/database
                    userRepository.createUser(user,
                            unused -> {
                                notificationRepository.fetchFCMToken(uID);
                                Toast.makeText(CompleteProfile.this, "Profile update successfully!", Toast.LENGTH_SHORT).show();
                                goToMain();
                            },
                            e -> {
                                Toast.makeText(CompleteProfile.this, "Profile update failed! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                goToMain();
                            });
                }
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(CompleteProfile.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String fullName = fullNameEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();

        if (fullName.isEmpty()) {
            fullNameLayout.setError("Vui lòng nhập họ tên đầy đủ");
            isValid = false;
        } else {
            fullNameLayout.setError(null);
        }

        if (phoneNumber.isEmpty()) {
            phoneNumberLayout.setError("Vui lòng nhập số điện thoại");
            isValid = false;
        } else if (!isValidPhoneNumber(phoneNumber)) {
            phoneNumberLayout.setError("Số điện thoại không hợp lệ");
            isValid = false;
        } else {
            phoneNumberLayout.setError(null);
        }

        return isValid;
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Basic phone number validation (modify according to your requirements)
        return phoneNumber.length() >= 10;
    }
}