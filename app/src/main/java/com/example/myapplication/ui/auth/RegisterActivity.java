package com.example.myapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.data.Enum.Role;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.myapplication.R;
import com.example.myapplication.data.Model.Auth.AuthRegister;
import com.example.myapplication.data.Model.User.User;
import com.example.myapplication.data.Repository.User.UserRepository;
import com.example.myapplication.ui.activities.MainActivity;

import java.util.ArrayList;
import java.util.Date;


public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail;
    private EditText etPassword;
    private EditText etFullName;
    private EditText etPhoneNumber;
    private Button btnRegister;
    private TextView tvLogin;
    private FirebaseAuth firebaseAuth;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository(this);

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etFullName = findViewById(R.id.et_full_name);
        etPhoneNumber = findViewById(R.id.et_phone);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);

        // Set click listeners
        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        btnRegister.setEnabled(false);

        // Create auth register object
        AuthRegister authRegister = new AuthRegister(email, password, fullName, phoneNumber);

        // Create user with Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(authRegister.email, authRegister.password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        // Create User object (without email field)
                        User user = new User();
                        user.uid = firebaseUser.getUid();
                        user.full_name = authRegister.full_name;
                        user.phone_number = authRegister.phone_number;
                        user.created_at = new Date(); // Add creation timestamp
                        user.role = Role.valueOf("USER");
                        user.rentingHistory = new ArrayList<>(); // Initialize empty list
                        user.wish_list = new ArrayList<>(); // Initialize empty list

                        // Save user to Firestore
                        userRepository.createUser(user,
                                aVoid -> {
                                    // Registration successful
                                    Toast.makeText(RegisterActivity.this,
                                            "Registration successful",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                },
                                e -> {
                                    btnRegister.setEnabled(true);
                                    Toast.makeText(RegisterActivity.this,
                                            "Failed to create user: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this,
                            "Registration failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}