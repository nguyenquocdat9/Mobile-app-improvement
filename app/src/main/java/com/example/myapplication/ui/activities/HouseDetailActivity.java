package com.example.myapplication.ui.activities;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.data.Model.Property.Amenities;
import com.example.myapplication.data.Model.Property.AmenityStatus;
import com.example.myapplication.data.Model.Review.Review;
import com.example.myapplication.data.Repository.Review.ReviewRepository;
import com.example.myapplication.data.Repository.User.UserRepository;
import com.example.myapplication.ui.adapters.PostImageAdapter;
import com.example.myapplication.ui.adapters.ReviewAdapter;
import com.example.myapplication.ui.misc.Amenity;
import com.example.myapplication.ui.misc.Post;
import com.example.myapplication.ui.misc.WishlistManager;
import com.google.firebase.auth.FirebaseAuth;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import org.w3c.dom.Text;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HouseDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ImageButton heartButton;
    private Post post;

    ViewPager2 reviewRecyclerView;

    UserRepository userRepository;

    //doi mau
    private boolean isTopBarWhite = false;

    private MapView miniMap;
    private GoogleMap mMap;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ko lay dc propertyId
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_detail);
        getWindow().setBackgroundDrawableResource(android.R.color.white);

        ImageButton backButton = findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> finish());

        ImageButton shareButton = findViewById(R.id.btnShare);
        shareButton.setOnClickListener(v -> sharePost());

        ImageButton btnMessageHost = findViewById(R.id.btnMessageHost);
        btnMessageHost.setOnClickListener(view -> {
            // Get host ID from the property
            String hostId = post.getHostId();
            String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            // Create intent to MainActivity with messages fragment
            Intent intent = new Intent(HouseDetailActivity.this, MainActivity.class);
            intent.putExtra("FRAGMENT_TO_LOAD", "messages");
            intent.putExtra("HOST_ID", hostId);
            intent.putExtra("PROPERTY_ID", post.getId());
            intent.putExtra("CREATE_CONVERSATION", true);
            startActivity(intent);
        });

        ScrollView scrollView = findViewById(R.id.scrollView);
        ViewPager2 postImage = findViewById(R.id.viewPagerImages);
        ConstraintLayout topBar = findViewById(R.id.top_button_bar);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollView.getScrollY();
            int imageHeight = postImage.getHeight();

            if (scrollY > imageHeight - 100 && !isTopBarWhite) {
                isTopBarWhite = true;
                animateBackgroundColor(topBar, 0x00FFFFFF, 0xFFFFFFFF); // transparent → white
            } else if (scrollY <= imageHeight - 100 && isTopBarWhite) {
                isTopBarWhite = false;
                animateBackgroundColor(topBar, 0xFFFFFFFF, 0x00FFFFFF); // white → transparent
            }
        });

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        miniMap = findViewById(R.id.miniMapView);
        miniMap.onCreate(mapViewBundle);
        miniMap.getMapAsync(this);

        //Set clickable cho mapView mở Large Map
        View mapClick = findViewById(R.id.mapClick);
        mapClick.setClickable(true);
        mapClick.setOnClickListener(v -> {
            Intent intent = new Intent(HouseDetailActivity.this, LargeMapDetailActivity.class);
            intent.putExtra("location", post.getTitle());
            intent.putExtra("name", post.getTitle());
            startActivity(intent);
        });

        post = getIntent().getParcelableExtra("post");
        if (post != null) {
            TextView title = findViewById(R.id.title);
            //ImageView postImageView = findViewById(R.id.post_image);
            ViewPager2 viewPager = findViewById(R.id.viewPagerImages);
            TextView hostName = findViewById(R.id.host_name);
            TextView hostYear = findViewById(R.id.hostYear);
            TextView location = findViewById(R.id.location);
            TextView detail = findViewById(R.id.details);
            TextView dateRange = findViewById(R.id.date_range);
            TextView price = findViewById(R.id.price);
            TextView avg_ratings = findViewById(R.id.ratings);
            TextView total_reviews = findViewById(R.id.total_reviews);
            TextView house_rule = findViewById(R.id.house_rule);
            TextView special_feature = findViewById(R.id.special_feature);
            TextView titleRiview = findViewById(R.id.reviewTitle);
            showAmenities(post);
            heartButton = findViewById(R.id.heart_button);

            title.setText(post.getTitle());
            List<String> allImages = post.getSub_photos();
            if (allImages == null) allImages = new ArrayList<>();
            if (!allImages.contains(post.getImageResId())) {
                allImages.add(0, post.getImageResId()); // chèn ảnh chính vào đầu
            }

            PostImageAdapter adapter = new PostImageAdapter(this, allImages, post, false);
            viewPager.setAdapter(adapter);

            location.setText(post.getLocation());
            detail.setText(post.getDetail());
            dateRange.setText(post.getDateRange());
            price.setText(post.getNormal_price());
            avg_ratings.setText(post.getAvgRatings() + "");
            total_reviews.setText(post.getTotalReview() + "\nĐánh giá");
            titleRiview.setText("Đánh giá (" + post.getTotalReview() + ")");
            if (post.getAmenities() != null && post.getAmenities().houseRules != null) {
                house_rule.setText(post.getAmenities().houseRules);
            } else {
                house_rule.setText("Không có nội quy"); // hoặc để trống
            }

            if (post.getAmenities() != null && post.getAmenities().more != null) {
                special_feature.setText(post.getAmenities().more);
            } else {
                special_feature.setText("Không có"); // hoặc để trống
            }

            updateHeartIcon();

            heartButton.setOnClickListener(v -> handleHeartClick());

            UserRepository userRepository = new UserRepository(this);
            userRepository.getHostNameByPropertyID(post.getId(),
                    hostNameStr -> {
                        hostName.setText("Host: " + hostNameStr);
                    },
                    e -> {
                        hostName.setText("Không rõ chủ nhà");
                    }
            );

            // them ui phan host
//            userRepository.getDateCreateByPropertyID(post.getId(),
//                    hostYearStr -> {
//                        hostYear.setText("" + hostYearStr);
//                    },
//                    e -> {
//                        hostYear.setText("");
//                    }
//            );

            reviewRecyclerView = findViewById(R.id.reviewRecyclerView);

            ReviewRepository reviewRepository = new ReviewRepository(this); // hoặc getContext() nếu trong Fragment

            reviewRepository.getAllReviewByPropertyID(post.getId(),
                    reviews -> {
                        Log.d("HouseDetail", "Loaded reviews: " + reviews.size());
                        // Gán adapter để hiển thị trong RecyclerView
                        ReviewAdapter review_adapter = new ReviewAdapter(reviews);
                        reviewRecyclerView.setAdapter(review_adapter);
                        reviewRecyclerView.setOffscreenPageLimit(3);
                    },
                    e -> {
                        Log.e("HouseDetail", "Failed to load reviews: " + e.getMessage());
                    }
            );
        }



        boolean showReview = getIntent().getBooleanExtra("show_review", false);
        String bookingId = getIntent().getStringExtra("booking_id");

        if (showReview && bookingId != null) {
            showReviewDialog(bookingId);
        }

        Button btnBooking = findViewById(R.id.btnBooking);
        btnBooking.setOnClickListener(v -> navigateToBooking());
    }

    private void navigateToBooking() {
        if (post != null) {
            Intent intent = new Intent(this, BookingActivity.class);
            // Pass data
            intent.putExtra("propertyId", post.getId());
            intent.putExtra("hostId", post.getHostId());
            intent.putExtra("propertyTitle", post.getTitle());
            intent.putExtra("propertyLocation", post.getLocation());
            intent.putExtra("price", post.getNormal_price());
            intent.putExtra("propertyRating", post.getAvgRatings());
            intent.putExtra("totalReviews", post.getTotalReview());
            startActivity(intent);

            // Log all data
            Log.d("House DetailActivity", "Navigating to BookingActivity with data:");
            Log.d("House DetailActivity", "Property ID: " + post.getId());
            Log.d("House DetailActivity", "Host ID: " + post.getHostId());
            Log.d("House DetailActivity", "Property Title: " + post.getTitle());
            Log.d("House DetailActivity", "Property Location: " + post.getLocation());
            Log.d("House DetailActivity", "Price: " + post.getNormal_price());
            Log.d("House DetailActivity", "Property Rating: " + post.getAvgRatings());
            Log.d("House DetailActivity", "Total Reviews: " + post.getTotalReview());

        } else {
            Toast.makeText(this, "Không thể đặt phòng lúc này", Toast.LENGTH_SHORT).show();
        }
    }

    private void animateBackgroundColor(View view, int startColor, int endColor) {
        ObjectAnimator colorAnim = ObjectAnimator.ofArgb(view, "backgroundColor", startColor, endColor);
        colorAnim.setDuration(300); // thời gian đổi màu (ms)
        colorAnim.start();
    }

    private void handleHeartClick() {
        boolean isInWishlist = WishlistManager.getInstance().isPostInInterestedWishlist(post);

        if (!isInWishlist) {
            WishlistManager.getInstance().addToInterestedView(post, FirebaseAuth.getInstance().getCurrentUser().getUid(), new UserRepository(this));
            updateHeartIcon();
        } else {
            WishlistManager.getInstance().removeFromInterestedView(post, FirebaseAuth.getInstance().getCurrentUser().getUid(), new UserRepository(this));
            updateHeartIcon();
        }
    }

    private void updateHeartIcon() {
        boolean isInWishlist = WishlistManager.getInstance().isPostInInterestedWishlist(post);
        heartButton.setImageResource(isInWishlist ?
                R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
    }

    private void sharePost() {
        if (post == null) return;

        String shareText = "Check out this house!\n"
                + post.getTitle() + "\n"
                + post.getLocation() + "\n"
                + "Price: " + post.getNormal_price();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "House Listing");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void showAmenities(Post post) {
        LinearLayout amenityContainer = findViewById(R.id.amenity_list); // LinearLayout trong layout chính

        Amenities a = post.getAmenities();

        List<Amenity> amenityList = Arrays.asList(
                new Amenity("TV", R.drawable.ic_tv, a.tv),
                new Amenity("Wi-Fi", R.drawable.ic_wifi, a.wifi),
                new Amenity("Thú cưng", R.drawable.ic_pets, a.petAllowance),
                new Amenity("Hồ bơi", R.drawable.ic_pool, a.pool),
                new Amenity("Máy giặt", R.drawable.ic_bed, a.washingMachine),
                new Amenity("Bữa sáng", R.drawable.ic_free_breakfast, a.breakfast),
                new Amenity("Máy lạnh", R.drawable.ic_airconditioner, a.airConditioner),
                new Amenity("BBQ", R.drawable.ic_outdoor_grill, a.bbq)
        );

        for (Amenity amenity : amenityList) {
            if (amenity.status == AmenityStatus.Hidden) continue;

            View view = LayoutInflater.from(this).inflate(R.layout.amenity_item, amenityContainer, false);
            ImageView icon = view.findViewById(R.id.amenity_icon);
            TextView name = view.findViewById(R.id.amenity_name);

            icon.setImageResource(amenity.iconResId);
            name.setText(amenity.name);

            if (amenity.status == AmenityStatus.Unavailable) {
                name.setPaintFlags(name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                name.setTextColor(Color.GRAY);
                icon.setColorFilter(Color.GRAY);
            }

            amenityContainer.addView(view);
        }
    }


    private void showReviewDialog(String bookingId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_review, null);

        EditText reviewText = dialogView.findViewById(R.id.review_text);
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);

        builder.setView(dialogView)
                .setTitle("Đánh giá")
                .setPositiveButton("Gửi", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    String comment = reviewText.getText().toString();
                    submitReview(bookingId, rating, comment);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void submitReview(String bookingId, float rating, String comment) {
        // Implement review submission using ReviewRepository
        // You can add this functionality when needed
        Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        miniMap.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        showDestination();

        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        // Tắt tất cả tương tác người dùng
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
    }

    private void showDestination() {
        String locationName = post.getTitle();
        Geocoder geocoder = new Geocoder(this, new Locale("vi", "VN"));
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng location = new LatLng(address.getLatitude(), address.getLongitude());

                mMap.addMarker(new MarkerOptions().position(location).title(post.getName()));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            } else {
                if (addresses == null) Toast.makeText(this, "Không phan hoi", Toast.LENGTH_SHORT).show();
                else Toast.makeText(this, "Không tìm thấy vị trí đích" + locationName, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi tìm tọa độ đích", Toast.LENGTH_SHORT).show();
        }
    }
}
