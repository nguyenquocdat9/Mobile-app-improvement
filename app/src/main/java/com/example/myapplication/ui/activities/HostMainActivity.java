package com.example.myapplication.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;
import com.example.myapplication.ui.fragments.MessagesFragment;
import com.example.myapplication.ui.fragments.host.BookingManageFragment;
import com.example.myapplication.ui.fragments.host.PropertyManageFragment;
import com.example.myapplication.ui.fragments.host.HostMessageFragment;
import com.example.myapplication.ui.fragments.host.PropertyManagementFragment;
import com.example.myapplication.ui.fragments.host.StatisticFragment;
import com.example.myapplication.ui.fragments.ProfileFragment;

public class HostMainActivity extends AppCompatActivity {
    private boolean isFragmentTransactionInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_main);

        if (savedInstanceState == null) {
            loadFragment(new PropertyManagementFragment());
            updateButtonStates(R.id.button_property_manage);
        }

        setupFooterNavigation();
    }

    private void setupFooterNavigation() {
        findViewById(R.id.button_property_manage).setOnClickListener(v -> {
            loadFragment(new PropertyManagementFragment());
            updateButtonStates(R.id.button_property_manage);
        });
        findViewById(R.id.button_booking_manage).setOnClickListener(v -> {
            loadFragment(new BookingManageFragment());
            updateButtonStates(R.id.button_booking_manage);
        });
        findViewById(R.id.button_message).setOnClickListener(v -> {
            loadFragment(new MessagesFragment());
            updateButtonStates(R.id.button_message);
        });
        findViewById(R.id.button_statistic).setOnClickListener(v -> {
            loadFragment(new StatisticFragment());
            updateButtonStates(R.id.button_statistic);
        });
        findViewById(R.id.button_profile).setOnClickListener(v -> {
            loadFragment(new ProfileFragment());
            updateButtonStates(R.id.button_profile);
        });
    }

    private void updateButtonStates(int selectedButtonId) {
        int[] buttonIds = {
                R.id.button_property_manage,
                R.id.button_booking_manage,
                R.id.button_message,
                R.id.button_statistic,
                R.id.button_profile
        };
        for (int id : buttonIds) {
            findViewById(id).setSelected(id == selectedButtonId);
        }
    }

    private void loadFragment(Fragment fragment) {
        // Prevent multiple concurrent transactions
        if (isFragmentTransactionInProgress) {
            return;
        }

        // Prevent loading the same fragment type that's already showing
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.host_fragment_container);
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            return;
        }

        isFragmentTransactionInProgress = true;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.host_fragment_container, fragment)
                .commitAllowingStateLoss(); // Use commitAllowingStateLoss instead of commit

        // Reset the flag after a short delay to allow the transaction to complete
        new android.os.Handler().postDelayed(() -> isFragmentTransactionInProgress = false, 300);
    }
}