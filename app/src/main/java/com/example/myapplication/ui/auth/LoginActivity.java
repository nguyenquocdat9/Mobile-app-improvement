package com.example.myapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.data.Model.Auth.AuthLogin;
import com.example.myapplication.data.Repository.Auth.AuthRepository;
import com.example.myapplication.data.Repository.Notification.NotificationRepository;
import com.example.myapplication.ui.activities.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.myapplication.R;
import com.example.myapplication.data.Repository.User.UserRepository;


public class LoginActivity extends AppCompatActivity {
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private TextView tvForgotPassword;
    private UserRepository userRepository;
    private AuthRepository authRepository;
    private NotificationRepository notificationRepository;
    private static final int RC_GOOGLE_SIGN_IN = 1001;
    private GoogleSignInClient googleSignInClient;
    private SignInButton btnGoogleSignIn;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private TextView tvErrorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository(this);
        userRepository = new UserRepository(this);
        notificationRepository = new NotificationRepository(this);


        if (authRepository.checkLogin()) {
            notificationRepository.fetchFCMToken(authRepository.getUserUid());
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Không hiển thị màn login nữa
            return;
        }

        //Google Client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                authRepository.loginWithGoogleAccount(
                                        account,
                                        this::handlePostGoogleSignIn,
                                        error -> Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show()
                                );
                            }
                        } catch (ApiException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Lỗi đăng nhập: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);

        // Set click listeners
        btnLogin.setOnClickListener(v -> loginUser());
        // Gắn sự kiện đăng nhập Google
        btnGoogleSignIn.setOnClickListener(view -> startGoogleSignIn());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
    }

    private void startGoogleSignIn() {
        googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    authRepository.loginWithGoogleAccount(
                            account,
                            this::handlePostGoogleSignIn,
                            error -> Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            } catch (ApiException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi đăng nhập: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handlePostGoogleSignIn(AuthResult authResult) {
        boolean isNew = authResult.getAdditionalUserInfo().isNewUser();
        FirebaseUser fbUser = authResult.getUser();
        if (isNew && fbUser != null) {
            goToCompleteProfile(fbUser);
            Toast.makeText(this, "Tài khoản mới đăng kí thành công", Toast.LENGTH_SHORT).show();
        } else {
            assert fbUser != null;
            notificationRepository.fetchFCMToken(fbUser.getUid());
            goToMain();
        }
    }


    private void goToCompleteProfile(FirebaseUser fbUser) {
        Intent i = new Intent(LoginActivity.this, CompleteProfile.class);
        startActivity(i);
        finish();
    }


    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        tvErrorMessage.setVisibility(View.GONE);

        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            tvErrorMessage.setText("Please fill all fields");
            tvErrorMessage.setVisibility(View.VISIBLE);
            return;
        }

        // Show loading indicator
        btnLogin.setEnabled(false);

        AuthLogin authLogin = new AuthLogin(email, password);

        // Authenticate with Firebase
        authRepository.login(authLogin,
                unused -> {
                    String uid = authRepository.getUserUid();
                    if (uid != null) {
                        userRepository.getUserByUid(uid,
                                userData -> {
                                    // Login thành công, chuyển sang MainActivity
                                    notificationRepository.fetchFCMToken(uid);
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                },
                                e -> {
                                    btnLogin.setEnabled(true);
                                    Toast.makeText(LoginActivity.this,
                                            "Lấy thông tin người dùng thất bại: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Không tìm thấy UID người dùng", Toast.LENGTH_SHORT).show();
                    }
                },
                e -> {
                    btnLogin.setEnabled(true);
                    String errorMsg = "Sai email hoặc mật khẩu";
                    tvErrorMessage.setText(errorMsg);
                    tvErrorMessage.setVisibility(View.VISIBLE);
                });
    }

    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Hãy nhập email của bạn", Toast.LENGTH_SHORT).show();
            return;
        }

        authRepository.resetPassword(email,
                unused -> Toast.makeText(LoginActivity.this,
                        "Password reset email sent", Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(LoginActivity.this,
                        "Failed to send reset email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
