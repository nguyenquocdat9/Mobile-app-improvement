package com.example.myapplication.ui.activities;

import android.location.Address;
import android.location.Geocoder;

import android.os.Bundle;

import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.example.myapplication.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LargeMapDetailActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private String locationName;
    private String locationTitle;

    private MapView miniMap;
    private ImageButton backButton;
    private TextView nameText;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locationName = getIntent().getStringExtra("location");
        locationTitle = getIntent().getStringExtra("name");

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        miniMap = findViewById(R.id.mapView);
        miniMap.onCreate(mapViewBundle);
        miniMap.getMapAsync(this);

        backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> finish());

        nameText = findViewById(R.id.nameText);
        nameText.setText(locationTitle);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        showDestination();
    }

    private void showDestination() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Toast.makeText(this, locationName, Toast.LENGTH_SHORT);
                Address address = addresses.get(0);
                LatLng location = new LatLng(address.getLatitude(), address.getLongitude());

                mMap.addMarker(new MarkerOptions().position(location).title(locationTitle != null ? locationTitle : locationName));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
            } else {
                Toast.makeText(this, "Không tìm thấy vị trí đích", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi tìm tọa độ đích", Toast.LENGTH_SHORT).show();
        }
    }

    //MapView setup
    @Override
    protected void onResume() {
        super.onResume();
        miniMap.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        miniMap.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        miniMap.onStop();
    }

    @Override
    protected void onPause() {
        miniMap.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        miniMap.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        miniMap.onLowMemory();
    }

}