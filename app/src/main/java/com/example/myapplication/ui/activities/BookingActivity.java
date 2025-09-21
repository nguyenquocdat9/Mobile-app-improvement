package com.example.myapplication.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.fragments.RoomBookingFragment;


public class BookingActivity extends AppCompatActivity {
    private ImageButton backButton;
    private ImageButton exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Get data from intent
        String propertyId = getIntent().getStringExtra("propertyId");
        String hostId = getIntent().getStringExtra("hostId");

        Log.d("BookingActivity", "Received propertyId: " + propertyId + ", hostId: " + hostId);


        String propertyTitle = getIntent().getStringExtra("propertyTitle");
        String propertyLocation = getIntent().getStringExtra("propertyLocation");
        String price = getIntent().getStringExtra("price");
        double propertyRating = getIntent().getDoubleExtra("propertyRating", 0.0);
        int totalReviews = getIntent().getIntExtra("totalReviews", 0);

        // Create and setup fragment - Fix the duplicate creation
        RoomBookingFragment fragment = new RoomBookingFragment();
        Bundle args = new Bundle();
        args.putString("propertyId", propertyId);
        args.putString("hostId", hostId);
        args.putString("propertyTitle", propertyTitle);
        args.putString("propertyLocation", propertyLocation);
        args.putString("price", price);
        args.putDouble("propertyRating", propertyRating);
        args.putInt("totalReviews", totalReviews);
        fragment.setArguments(args);

        // Use the fragment instance that has the arguments
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        // Initialize buttons
        backButton = findViewById(R.id.btnBack);
        exitButton = findViewById(R.id.btnClose);

        backButton.setOnClickListener(v -> onBackPressed());

        exitButton.setOnClickListener(v -> {
            Intent intent = new Intent(BookingActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}