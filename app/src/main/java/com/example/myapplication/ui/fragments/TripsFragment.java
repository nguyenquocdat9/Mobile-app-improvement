package com.example.myapplication.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.Enum.Booking_status;
import com.example.myapplication.data.Model.Booking.Booking;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Model.Review.Review;
import com.example.myapplication.data.Repository.Booking.BookingRepository;
import com.example.myapplication.data.Repository.Review.ReviewRepository;
import com.example.myapplication.ui.activities.HouseDetailActivity;
import com.example.myapplication.ui.adapters.BookingAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TripsFragment extends Fragment {
    private RecyclerView bookingRecyclerView;
    private BookingAdapter bookingAdapter;
    private BookingRepository bookingRepository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trips, container, false);
        bookingRecyclerView = view.findViewById(R.id.booking_recycler_view);
        bookingRepository = new BookingRepository(requireContext());
        loadBookings();
        return view;
    }

    private void loadBookings() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        bookingRepository.getAllBookingByGuestID(userId,
                bookings -> {
                    bookingAdapter = new BookingAdapter(bookings,
                            new BookingAdapter.OnBookingActionListener() {
                                @Override
                                public void onActionClick(Booking booking) {
                                    if (booking.status == Booking_status.ACCEPTED) {
                                        showCancelDialog(booking);
                                    } else if (booking.status == Booking_status.COMPLETED) {
                                        navigateToReview(booking);
                                    }
                                }

                                @Override
                                public void onViewDetailsClick(Booking booking, Property property) {
                                    // Handle property details click
                                    showBookingDetailDialog(booking, property);
                                }
                            },
                            requireContext());

                    // Set adapter to RecyclerView
                    bookingRecyclerView.setAdapter(bookingAdapter);
                },
                e -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void showCancelDialog(Booking booking) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hủy đặt phòng")
                .setMessage("Bạn có chắc muốn hủy đặt phòng này?")
                .setPositiveButton("Có", (dialog, which) -> {
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    bookingRepository.cancelBooking(userId, booking.getId(),
                            unused -> {
                                Toast.makeText(requireContext(), "Đã hủy đặt phòng", Toast.LENGTH_SHORT).show();
                                loadBookings(); // Reload the list
                            },
                            e -> Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void navigateToReview(Booking booking) {
        showReviewDialog(booking);
    }

    private void showReviewDialog(Booking booking) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_create_review, null);

        RatingBar ratingBar = view.findViewById(R.id.rating_bar);
        EditText reviewContent = view.findViewById(R.id.review_content);

        builder.setView(view)
                .setTitle("Đánh giá chỗ ở")
                .setPositiveButton("Gửi đánh giá", (dialog, which) -> {
                    int rating = Math.round(ratingBar.getRating());
                    String content = reviewContent.getText().toString();

                    if (rating == 0) {
                        Toast.makeText(requireContext(), "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    submitReview(booking, rating, content);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void submitReview(Booking booking, int rating, String content) {
        // Create a review using the available constructor
        // This constructor already generates a UUID for the id
        Review review = new Review(
                booking.id,
                booking.property_id,
                rating,
                content
        );

        ReviewRepository reviewRepository = new ReviewRepository(requireContext());

        // The guestReviewBooking method will handle converting Review to ReviewWithReviewerName
        reviewRepository.guestReviewBooking(review,
                unused -> {
                    Toast.makeText(requireContext(), "Đánh giá của bạn đã được gửi thành công", Toast.LENGTH_SHORT).show();
                    // Refresh the bookings list to update the UI
                    loadBookings();
                },
                e -> {
                    Toast.makeText(requireContext(), "Không thể gửi đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showBookingDetailDialog(Booking booking, Property property) {
        if (booking == null || property == null) {
            Toast.makeText(requireContext(), "Booking details unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create dialog with custom style
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_booking_detail);

        // Make dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Get screen width
            int screenWidth = getResources().getDisplayMetrics().widthPixels;

            // Set dialog width to 90% of screen width
            int dialogWidth = (int) (screenWidth * 0.9);

            dialog.getWindow().setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Find views
        ImageView propertyImage = dialog.findViewById(R.id.detail_property_image);
        TextView propertyName = dialog.findViewById(R.id.detail_property_name);
        TextView propertyLocation = dialog.findViewById(R.id.detail_property_location);
        TextView bookingDates = dialog.findViewById(R.id.detail_booking_dates);
        TextView status = dialog.findViewById(R.id.detail_status);
        TextView totalPrice = dialog.findViewById(R.id.detail_total_price);
        TextView bookingId = dialog.findViewById(R.id.detail_booking_id);
        TextView priceBreakdown = dialog.findViewById(R.id.detail_price_breakdown);
        MaterialButton closeButton = dialog.findViewById(R.id.btn_close_dialog);

        // Set property name with styling
        propertyName.setText(property.getName());

        // Format and set property location
        if (property.getAddress() != null) {
            StringBuilder addressBuilder = new StringBuilder();
            if (property.getAddress().detailed_address != null && !property.getAddress().detailed_address.isEmpty())
                addressBuilder.append(property.getAddress().detailed_address);
            if (property.getAddress().ward_name != null && !property.getAddress().ward_name.isEmpty()) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append(property.getAddress().ward_name);
            }
            if (property.getAddress().district_name != null && !property.getAddress().district_name.isEmpty()) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append(property.getAddress().district_name);
            }
            if (property.getAddress().city_name != null && !property.getAddress().city_name.isEmpty()) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append(property.getAddress().city_name);
            }
            propertyLocation.setText(addressBuilder.toString());
        } else {
            propertyLocation.setText("Địa chỉ không khả dụng");
        }

        // Set booking dates with icon
        bookingDates.setText(getString(R.string.booking_dates_format,
                booking.check_in_day, booking.check_out_day));

        // Set status with appropriate color based on booking status
        String statusText = "Trạng thái: ";
        int statusColor;

        switch(booking.status) {
            case IN_PROGRESS:
                statusText += "Đang tiến hành";
                statusColor = requireContext().getColor(R.color.black);
                break;
            case ACCEPTED:
                statusText += "Đã xác nhận";
                statusColor = requireContext().getColor(android.R.color.holo_green_dark);
                break;
            case COMPLETED:
                statusText += "Đã hoàn thành";
                statusColor = requireContext().getColor(android.R.color.holo_blue_dark);
                break;
            case CANCELLED:
                statusText += "Đã hủy";
                statusColor = requireContext().getColor(android.R.color.holo_red_dark);
                break;
            case REVIEWED:
                statusText += "Đã đánh giá";
                statusColor = requireContext().getColor(android.R.color.holo_purple);
                break;
            default:
                statusText += booking.status.toString();
                statusColor = requireContext().getColor(R.color.black);
        }

        status.setText(statusText);
        status.setTextColor(statusColor);

        // Format currency and set total price
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        totalPrice.setText(getString(R.string.total_price_format,
                currencyFormat.format(booking.total_price) + " VND"));

        // Calculate and set price breakdown
        // Replace the current date parsing logic with this more robust approach
        try {
            // Define the expected date format - adjust this to match your actual format
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

            // Parse the dates
            Date checkInDate = dateFormat.parse(booking.check_in_day);
            Date checkOutDate = dateFormat.parse(booking.check_out_day);

            if (checkInDate != null && checkOutDate != null) {
                long differenceMs = checkOutDate.getTime() - checkInDate.getTime();
                int numberOfDays = (int) (differenceMs / (1000 * 60 * 60 * 24));

                // Ensure at least 1 day
                if (numberOfDays < 1) numberOfDays = 1;

                // Calculate price per night
                double pricePerNight = booking.total_price / numberOfDays;

                priceBreakdown.setText(String.format("%s VND × %d đêm",
                        currencyFormat.format(pricePerNight),
                        numberOfDays));
            } else {
                // Fallback if parsing fails
                priceBreakdown.setText(String.format("%s VND (tổng cộng)",
                        currencyFormat.format(booking.total_price)));
            }
        } catch (ParseException e) {
            // Log the error for debugging
            Log.e("BookingDetailDialog", "Date parsing error: " + e.getMessage());

            // Fallback
            priceBreakdown.setText(String.format("%s VND (tổng cộng)",
                    currencyFormat.format(booking.total_price)));
        }

        // Set booking ID
        bookingId.setText(getString(R.string.booking_id_format, booking.id));

        // Load property image with rounded corners
        if (property.getMainPhoto() != null && !property.getMainPhoto().isEmpty()) {
            Glide.with(requireContext())
                    .load(property.getMainPhoto())
                    .centerCrop()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.avatar_placeholder)
                    .into(propertyImage);
        } else {
            propertyImage.setImageResource(R.drawable.avatar_placeholder);
        }

        // Set up close button
        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}