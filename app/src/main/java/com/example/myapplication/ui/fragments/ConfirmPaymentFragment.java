package com.example.myapplication.ui.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;
import com.example.myapplication.data.Enum.Booking_status;
import com.example.myapplication.data.Model.Booking.Booking;
import com.example.myapplication.data.Repository.Booking.BookingRepository;
import com.example.myapplication.ui.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import com.google.firebase.firestore.FieldValue;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.util.Log;

public class ConfirmPaymentFragment extends Fragment {
    private TextView propertyTitleText;
    private TextView locationText;
    private TextView dateRangeText;
    private TextView totalPriceText;
    private ImageView propertyImageView;
    private TextView selectedPaymentMethodText;
    private Button payButton;
    private BookingRepository bookingRepository;
    private String propertyId;
    private String hostId;
    private String propertyTitle;
    private String propertyImage;
    private String propertyLocation;
    private String checkInDay;
    private String checkOutDay;
    private double totalPrice;
    private String paymentMethod;
    private TextView paymentScheduleText;
    private String paymentTiming;
    private String guestNote;

    public ConfirmPaymentFragment() {
        super(R.layout.fragment_confirm_payment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bookingRepository = new BookingRepository(requireContext());
        initializeViews(view);
        getArgumentData();
        setupViews();
        setupListeners();
    }

    private void initializeViews(View view) {
        propertyTitleText = view.findViewById(R.id.property_title);      // Changed from property_name
        locationText = view.findViewById(R.id.property_location);
        dateRangeText = view.findViewById(R.id.booking_dates);           // Changed from dates_guests
        totalPriceText = view.findViewById(R.id.total_price);
        propertyImageView = view.findViewById(R.id.property_image);
        selectedPaymentMethodText = view.findViewById(R.id.selected_payment_method);
        payButton = view.findViewById(R.id.next_button);
        bookingRepository = new BookingRepository(requireContext());
        paymentScheduleText = view.findViewById(R.id.payment_schedule);
        Button priceButton = view.findViewById(R.id.price_details_button);
    }

    private void getArgumentData() {
        Bundle args = getArguments();
        if (args != null) {
            propertyId = args.getString("propertyId", "");
            hostId = args.getString("hostId", "");
            propertyTitle = args.getString("propertyTitle", "");
            propertyLocation = args.getString("propertyLocation", "");
            propertyImage = args.getString("propertyImage", "");
            checkInDay = args.getString("checkInDay", "");
            checkOutDay = args.getString("checkOutDay", "");
            totalPrice = args.getDouble("totalPrice", 0.0);
            paymentMethod = args.getString("paymentMethod", "");
            paymentTiming = args.getString("paymentTiming", "");
            guestNote = args.getString("guestNote", "");
        }
    }

    private void setupListeners() {
        payButton.setOnClickListener(v -> handlePayment());


        Button priceDetailsButton = getView().findViewById(R.id.price_details_button);
        priceDetailsButton.setOnClickListener(v -> {
            // Create dialog to show price details
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_price_details, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setView(dialogView);

            // Calculate nights - assume simple date format
            long nights = calculateNights(checkInDay, checkOutDay);
            double roomRate = totalPrice / nights; // Per night rate

            // Set values in dialog
            TextView nightsDetail = dialogView.findViewById(R.id.nights_detail);
            TextView roomRateDetail = dialogView.findViewById(R.id.room_rate_detail);
            TextView totalDetail = dialogView.findViewById(R.id.total_detail);

            nightsDetail.setText(nights + " đêm");
            roomRateDetail.setText("₫" + String.format("%,.0f", roomRate) + " × " + nights + " đêm");
            totalDetail.setText("₫" + String.format("%,.0f", totalPrice));

            AlertDialog dialog = builder.create();
            dialog.show();

            // Handle close button
            dialogView.findViewById(R.id.close_button).setOnClickListener(view -> dialog.dismiss());
        });
    }

    private void setupViews() {
        propertyTitleText.setText(propertyTitle);
        locationText.setText(propertyLocation);
        dateRangeText.setText(String.format("%s - %s", checkInDay, checkOutDay));
        totalPriceText.setText(String.format("%,.0fđ", totalPrice));
        selectedPaymentMethodText.setText(paymentMethod);
        paymentScheduleText.setText(paymentTiming);

        if (!propertyImage.isEmpty()) {
            Glide.with(requireContext())
                    .load(propertyImage)
                    .into(propertyImageView);
        }
    }

    private void handlePayment() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Booking booking = new Booking(
                propertyId,      // property_id
                currentUserId,   // guest_id
                hostId,         // host_id
                checkInDay,      // check_in_day
                checkOutDay,     // check_out_day
                totalPrice,      // total_price
                guestNote        // guest_note
        );

        // Use BookingRepository to create the booking
        bookingRepository.createBooking(booking,
                unused -> {
                    updatePropertyBookedDates(checkInDay, checkOutDay);

                    Toast.makeText(requireContext(),
                            "Đặt phòng thành công!",
                            Toast.LENGTH_SHORT).show();
                    new android.os.Handler().postDelayed(
                            () -> {
                                // Close current activity and go to MainActivity with TripsFragment
                                Intent intent = new Intent(requireActivity(), MainActivity.class);
                                // Add flag to clear previous activities
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                // Use FRAGMENT_TO_LOAD instead of openFragment
                                intent.putExtra("FRAGMENT_TO_LOAD", "trips");
                                startActivity(intent);
                                requireActivity().finish();
                            },
                            1000 // 1 second delay
                    );
                },
                e -> Toast.makeText(requireContext(),
                        "Đặt phòng thất bại: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show()
        );
    }

    // Add this method to update the property's booked dates
    private void updatePropertyBookedDates(String checkInDay, String checkOutDay) {
        try {
            // Parse dates - they should be in dd-MM-yyyy format already
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date checkInDate = sdf.parse(checkInDay);
            Date checkOutDate = sdf.parse(checkOutDay);

            // Get all dates between check-in and check-out
            List<String> datesToBook = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(checkInDate);

            while (!calendar.getTime().after(checkOutDate)) {
                datesToBook.add(sdf.format(calendar.getTime()));
                calendar.add(Calendar.DATE, 1);
            }

            // Update property with new booked dates
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("properties").document(propertyId)
                    .update("booked_date", FieldValue.arrayUnion(datesToBook.toArray()))
                    .addOnSuccessListener(aVoid ->
                            Log.d("ConfirmPayment", "Booked dates updated successfully"))
                    .addOnFailureListener(e ->
                            Log.e("ConfirmPayment", "Error updating booked dates", e));

        } catch (ParseException e) {
            Log.e("ConfirmPayment", "Date parsing error", e);
        }
    }

    private long calculateNights(String checkInDay, String checkOutDay) {
        try {
            // Parse dates in format "dd/MM/yyyy"
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            java.util.Date checkIn = format.parse(checkInDay);
            java.util.Date checkOut = format.parse(checkOutDay);

            // Calculate difference in days
            long diffInMillis = checkOut.getTime() - checkIn.getTime();
            return java.util.concurrent.TimeUnit.DAYS.convert(diffInMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return 1; // Default to 1 night if date parsing fails
        }
    }
}