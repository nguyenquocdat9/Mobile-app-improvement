package com.example.myapplication.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.example.myapplication.R;
import com.example.myapplication.data.Enum.Role;
import com.example.myapplication.data.Repository.User.UserRepository;
import com.example.myapplication.ui.auth.LoginActivity;
import com.example.myapplication.ui.fragments.ExploreFragment;
import com.example.myapplication.ui.fragments.MessagesFragment;
import com.example.myapplication.ui.fragments.NewExploreFragment;
import com.example.myapplication.ui.fragments.ProfileFragment;
import com.example.myapplication.ui.fragments.TripsFragment;
import com.example.myapplication.ui.fragments.WishlistFragment;
import com.example.myapplication.ui.misc.WishlistManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.core.splashscreen.SplashScreen;

public class MainActivity extends AppCompatActivity {
    private UserRepository userRepository;
    private WishlistManager wishlistManager;
    private boolean isFragmentTransactionInProgress = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install splash screen
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Check user and redirect before showing content
        userRepository = new UserRepository(this);
        checkUserAndRedirect();

        // Only set content view if we're not redirecting
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // Initialize UserRepository and WishlistManager
        userRepository = new UserRepository(this);
        wishlistManager = WishlistManager.getInstance();

        // Load user wishlist (get current user's UID)
        loadUserWishlist();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new NewExploreFragment())
                    .commit();
        }

        setupFooterNavigation();

        // Check for navigation intents
        String fragmentToLoad = getIntent().getStringExtra("FRAGMENT_TO_LOAD");
        if (fragmentToLoad != null) {
            switch (fragmentToLoad) {
                case "wishlists":
                    loadFragment(new WishlistFragment());
                    updateButtonStates(R.id.button_wishlists);
                    break;
                case "trips":
                    loadFragment(new TripsFragment());
                    updateButtonStates(R.id.button_trips);
                    break;
                case "profile":
                    loadFragment(new ProfileFragment());
                    updateButtonStates(R.id.button_profile);
                    break;
                case "messages":
                    // Handle in the specific check below
                    break;
                default:
                    loadFragment(new ExploreFragment());
                    updateButtonStates(R.id.button_explore);
                    break;
            }
        }

        // Neu intent contains "messages", load MessagesFragment
        if (fragmentToLoad != null && fragmentToLoad.equals("messages")) {
            MessagesFragment messagesFragment = new MessagesFragment();

            // Pass any extras to the fragment
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                messagesFragment.setArguments(extras);
            }

            loadFragment(messagesFragment);
            updateButtonStates(R.id.button_messages);
        }
    }

    private void updateButtonStates(int selectedButtonId) {
        int[] buttonIds = {
                R.id.button_explore,
                R.id.button_wishlists,
                R.id.button_trips,
                R.id.button_messages,
                R.id.button_profile
        };

        for (int id : buttonIds) {
            findViewById(id).setSelected(id == selectedButtonId);
        }
    }

    private void setupFooterNavigation() {
        findViewById(R.id.button_explore).setOnClickListener(v -> {
            loadFragment(new NewExploreFragment());
            updateButtonStates(R.id.button_explore);
        });
        findViewById(R.id.button_wishlists).setOnClickListener(v -> {
            loadFragment(new WishlistFragment());
            updateButtonStates(R.id.button_wishlists);
        });
        findViewById(R.id.button_trips).setOnClickListener(v -> {
            loadFragment(new TripsFragment());
            updateButtonStates(R.id.button_trips);
        });
        findViewById(R.id.button_messages).setOnClickListener(v -> {
            loadFragment(new MessagesFragment());
            updateButtonStates(R.id.button_messages);
        });
        findViewById(R.id.button_profile).setOnClickListener(v -> {
            loadFragment(new ProfileFragment());
            updateButtonStates(R.id.button_profile);
        });

        // Set initial state
        updateButtonStates(R.id.button_explore);
    }

    private void loadFragment(Fragment fragment) {
        // Prevent multiple concurrent transactions
        if (isFragmentTransactionInProgress) {
            return;
        }

        // Prevent loading the same fragment type that's already showing
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            return;
        }

        isFragmentTransactionInProgress = true;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss(); // Use commitAllowingStateLoss instead of commit

        // Reset the flag after a short delay to allow the transaction to complete
        new android.os.Handler().postDelayed(() -> isFragmentTransactionInProgress = false, 300);
    }

    private void loadUserWishlist() {
        String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("WishlistLoad", "Loading wishlist for user: " + userUID);

        wishlistManager.loadUserWishlist(
                userUID,
                userRepository,
                unused -> {
                    // Sau khi load xong, cập nhật giao diện hoặc adapter nếu cần
                    Log.d("WishlistLoad", "Wishlist loaded successfully.");
                    // Nếu cần cập nhật lại giao diện hoặc adapter, làm ở đây
                },
                e -> {
                    Log.e("WishlistLoad", "Lỗi khi load wishlist", e);
                }
        );
    }

    private void checkUserAndRedirect() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // User is not logged in, go to login screen
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userRepository.getUserByUid(currentUser.getUid(),
                user -> {
                    if (user != null && user.role == Role.HOST) {
                        // We're in MainActivity and need to go to HostMainActivity
                        startActivity(new Intent(this, HostMainActivity.class));
                        finish();
                    }
                },
                e -> {
                    // Error occurred, just continue in current activity
                }
        );
    }
}