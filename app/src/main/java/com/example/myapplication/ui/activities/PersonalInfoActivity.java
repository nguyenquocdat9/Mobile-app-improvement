package com.example.myapplication.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.Repository.User.UserRepository;
import com.example.myapplication.data.Model.User.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalInfoActivity extends AppCompatActivity {
    private TextView nameValue;
    private TextView phoneValue;
    private TextView emailValue;
    private CircleImageView profileImage;
    private UserRepository userRepository;
    private User currentUserData;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        Glide.with(this)
                                .load(selectedImageUri)
                                .centerCrop()
                                .into(profileImage);

                        uploadAvatar();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        initViews();
        setupClickListeners();
        loadUserInfo();
    }

    private void initViews() {
        nameValue = findViewById(R.id.name_value);
        phoneValue = findViewById(R.id.phone_value);
        emailValue = findViewById(R.id.email_value);
        profileImage = findViewById(R.id.profile_image);
        userRepository = new UserRepository(this);
    }

    private void setupClickListeners() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.btn_edit_name).setOnClickListener(v -> {
            // Navigate to edit name screen or show dialog
            showEditDialog("name", nameValue.getText().toString());
        });

        findViewById(R.id.btn_edit_phone).setOnClickListener(v -> {
            // Navigate to edit phone screen or show dialog
            showEditDialog("phone", phoneValue.getText().toString());
        });

        findViewById(R.id.btn_edit_email).setOnClickListener(v -> {
            android.view.LayoutInflater inflater = getLayoutInflater();
            android.view.View dialogView = inflater.inflate(R.layout.dialog_warning, null);

            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            android.widget.Button btnOk = dialogView.findViewById(R.id.btn_ok);
            btnOk.setOnClickListener(view -> dialog.dismiss());

            dialog.show();
        });

        FloatingActionButton btnEditAvatar = findViewById(R.id.btn_edit_avatar);
        btnEditAvatar.setOnClickListener(v -> selectImage());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void uploadAvatar() {
        if (selectedImageUri == null || currentUserData == null) return;

        Toast.makeText(this, "Uploading avatar...", Toast.LENGTH_SHORT).show();

        userRepository.updateAvatar(currentUserData.uid, selectedImageUri,
                avatarUrl -> {
                    Toast.makeText(this, "Avatar updated successfully", Toast.LENGTH_SHORT).show();
                    currentUserData.avatar_link = avatarUrl.toString();
                },
                error -> {
                    String errorMessage = "Unknown error";
                    if (error != null) {
                        errorMessage = error.toString();
                    }
                    Toast.makeText(this,
                            "Failed to update avatar: " + errorMessage,
                            Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void showEditDialog(String field, String currentValue) {
        // Create a custom dialog layout
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

        // Inflate a custom layout
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_edit_field, null);
        builder.setView(dialogView);

        // Get references to views in the dialog
        android.widget.TextView titleText = dialogView.findViewById(R.id.dialog_title);
        android.widget.EditText input = dialogView.findViewById(R.id.dialog_input);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        android.widget.Button btnSave = dialogView.findViewById(R.id.btn_save);

        // Set the dialog title
        String title = "Edit " + field.substring(0, 1).toUpperCase() + field.substring(1);
        titleText.setText(title);

        // Set current value
        input.setText(currentValue);

        // Create and show the dialog
        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Setup button click listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newValue = input.getText().toString().trim();
            if (!newValue.isEmpty()) {
                updateUserField(field, newValue);
                dialog.dismiss();
            } else {
                input.setError("This field cannot be empty");
            }
        });
    }

    private void updateUserField(String field, String value) {
        if (currentUserData == null) return;

        // Update the specific field
        switch (field) {
            case "name":
                // Call updateUser with the correct parameters
                userRepository.updateUser(currentUserData.uid, "full_name", value,
                        aVoid -> {
                            nameValue.setText(value);
                            currentUserData.full_name = value;
                            Toast.makeText(PersonalInfoActivity.this,
                                    "Name updated successfully", Toast.LENGTH_SHORT).show();
                        },
                        error -> {
                            Toast.makeText(PersonalInfoActivity.this,
                                    "Failed to update name: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                break;
            case "phone":
                userRepository.updateUser(currentUserData.uid, "phone_number", value,
                        aVoid -> {
                            phoneValue.setText(value);
                            currentUserData.phone_number = value;
                            Toast.makeText(PersonalInfoActivity.this,
                                    "Phone updated successfully", Toast.LENGTH_SHORT).show();
                        },
                        error -> {
                            Toast.makeText(PersonalInfoActivity.this,
                                    "Failed to update phone: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                break;
//            case "email":
//                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//                if (firebaseUser != null) {
//                    firebaseUser.updateEmail(value)
//                            .addOnSuccessListener(unused -> {
//                                emailValue.setText(value);
//                                Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show();
//                            })
//                            .addOnFailureListener(e ->
//                                    Toast.makeText(this, "Failed to update email: " + e.getMessage(),
//                                            Toast.LENGTH_SHORT).show());
//                }
//                break;
        }
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRepository.getUserByUid(currentUser.getUid(),
                user -> {
                    if (user != null) {
                        currentUserData = user;
                        nameValue.setText(user.full_name);
                        phoneValue.setText(user.phone_number);
                        emailValue.setText(currentUser.getEmail());

                        // Load avatar
                        if (user.avatar_link != null && !user.avatar_link.isEmpty()) {
                            Glide.with(this)
                                    .load(user.avatar_link)
                                    .placeholder(R.drawable.default_avatar)
                                    .centerCrop()
                                    .into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.default_avatar);
                        }
                    }
                },
                e -> Toast.makeText(this,
                        "Failed to load user info: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show()
        );
    }
}