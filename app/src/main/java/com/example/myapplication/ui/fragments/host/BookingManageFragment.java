package com.example.myapplication.ui.fragments.host;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.Enum.Booking_status;
import com.example.myapplication.data.Model.Booking.Booking;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Repository.Booking.BookingRepository;
import com.example.myapplication.data.Repository.User.UserRepository;
import com.example.myapplication.ui.adapters.HostBookingAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingManageFragment extends Fragment {
    private Button upComingTab;
    private Button onGoingTab;
    private Button completedTab;
    private RecyclerView recycler;

    private ImageView emptyImage;

    private HostBookingAdapter adapterCompleted, adapterInProgress, adapterUpcoming, adapterCancelled;
    private List<Booking> listCompleted = new ArrayList<>();
    private List<Booking> listInProgress = new ArrayList<>();
    private List<Booking> listUpcoming = new ArrayList<>();
    private List<Booking> listCancelled = new ArrayList<>();

    private BookingRepository bookingRepository;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bookingRepository = new BookingRepository(requireContext());
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_host_booking_management, container, false);

        recycler = view.findViewById(R.id.recyclerView);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        upComingTab = view.findViewById(R.id.upConming);
        onGoingTab = view.findViewById(R.id.onGoing);
        completedTab = view.findViewById(R.id.completed);

        emptyImage = view.findViewById(R.id.imageEmpty);

        adapterCompleted = new HostBookingAdapter(listCompleted, bookingActionListener, getContext());
        adapterInProgress = new HostBookingAdapter(listInProgress, bookingActionListener, getContext());
        adapterUpcoming = new HostBookingAdapter(listUpcoming, bookingActionListener, getContext());
        adapterCancelled = new HostBookingAdapter(listCancelled, bookingActionListener, getContext());

        highlightButton(upComingTab);
        unhighlightButton(onGoingTab);
        unhighlightButton(completedTab);

        recycler.setAdapter(adapterUpcoming);

        upComingTab.setOnClickListener(v -> {
            highlightButton(upComingTab);
            unhighlightButton(onGoingTab);
            unhighlightButton(completedTab);
            UpdateBooking(0);
        });

        onGoingTab.setOnClickListener(v -> {
            highlightButton(onGoingTab);
            unhighlightButton(upComingTab);
            unhighlightButton(completedTab);
            UpdateBooking(1);
        });

        completedTab.setOnClickListener(v -> {
            highlightButton(completedTab);
            unhighlightButton(upComingTab);
            unhighlightButton(onGoingTab);
            UpdateBooking(2);
        });

        fetchHostBookings();

        return view;
    }

    private final HostBookingAdapter.OnBookingActionListener bookingActionListener = new HostBookingAdapter.OnBookingActionListener() {
        @Override
        public void onViewDetailsClick(Booking booking, Property property) {
            showBookingDetailDialog(booking, property);
        }
    };

    private void showBookingDetailDialog(Booking booking, Property property) {
        if (booking == null || property == null) {
            Toast.makeText(getContext(), "Booking details unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create dialog with custom style
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_host_booking_detail);

        // Make dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Get screen width
            int screenWidth = getResources().getDisplayMetrics().widthPixels;

            // Set dialog width to 90% of screen width
            int dialogWidth = (int) (screenWidth * 0.9);

            dialog.getWindow().setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Initialize dialog views
        TextView propertyName = dialog.findViewById(R.id.host_detail_property_name);
        TextView bookingStatus = dialog.findViewById(R.id.host_detail_status);
        TextView bookingDates = dialog.findViewById(R.id.host_detail_booking_dates);
        TextView guestName = dialog.findViewById(R.id.host_detail_guest_name);
        TextView totalPrice = dialog.findViewById(R.id.host_detail_total_price);
        TextView bookingId = dialog.findViewById(R.id.host_detail_booking_id);
        LinearLayout guestNoteContainer = dialog.findViewById(R.id.guest_note_container);
        TextView guestNote = dialog.findViewById(R.id.host_detail_guest_note);
        com.google.android.material.button.MaterialButton closeButton = dialog.findViewById(R.id.btn_close_host_dialog);

        // Set data to views
        propertyName.setText(property.getName());
        bookingStatus.setText(getStatusText(booking.status));

        String dateRange = booking.check_in_day + " - " + booking.check_out_day;
        bookingDates.setText(dateRange);

        // Show guest name
        if (booking.guest_id != null) {
            // Fetch the guest name from the UserRepository
            fetchGuestName(booking.guest_id, guestName);
        } else {
            guestName.setText("Khách: Unknown");
        }

        // Format price properly without $ sign and avoid scientific notation
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        String formattedPrice = numberFormat.format(booking.total_price) + " VND";
        totalPrice.setText(formattedPrice);

        bookingId.setText("Mã đặt phòng: " + booking.id);

        // Display guest note if available
        if (booking.guest_note != null && !booking.guest_note.isEmpty()) {
            guestNoteContainer.setVisibility(View.VISIBLE);
            guestNote.setText(booking.guest_note);
        } else {
            guestNoteContainer.setVisibility(View.GONE);
        }

        // Set up close button
        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void fetchGuestName(String userId, TextView textView) {
        UserRepository userRepository = new UserRepository(requireContext());
        userRepository.getUserNameByUid(userId,
                name -> {
                    if (name != null && isAdded()) {
                        textView.setText("Khách: " + name);
                    }
                },
                error -> {
                    if (isAdded()) {
                        Log.e("BookingManageFragment", "Error fetching user: " + error.getMessage());
                        textView.setText("Khách: " + userId);
                    }
                });
    }

    private String getStatusText(Booking_status status) {
        if (status == null) return "Pending";

        switch (status) {
            case IN_PROGRESS:
                return "Đang tiến hành";
            case ACCEPTED:
                return "Đã xác nhận";
            case COMPLETED:
                return "Đã hoàn thành";
            case CANCELLED:
                return "Đã hủy";
            case REVIEWED:
                return "Đã đánh giá";
            default:
                return status.toString();
        }
    }

    private void fetchHostBookings() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        String hostId = firebaseAuth.getCurrentUser().getUid();
        Log.d("hostId", hostId);
        bookingRepository.getAllBookingsByHostID(hostId,
                bookings -> {
                    if (isAdded()) {
                        listCompleted.clear();
                        listInProgress.clear();
                        listUpcoming.clear();
                        listCancelled.clear();
                        Log.d("booking", bookings.size() + "");
                        if (bookings != null) {
                            for (Booking booking : bookings) {
                                if (booking.status == Booking_status.COMPLETED || booking.status == Booking_status.REVIEWED) {
                                    listCompleted.add(booking);
                                } else if (booking.status == Booking_status.IN_PROGRESS) {
                                    listInProgress.add(booking);
                                } else if (booking.status == Booking_status.ACCEPTED) {
                                    listUpcoming.add(booking);
                                } else if (booking.status == Booking_status.CANCELLED) {
                                    listCancelled.add(booking);
                                }
                            }
                        }
                        adapterCompleted.notifyDataSetChanged();
                        adapterInProgress.notifyDataSetChanged();
                        adapterUpcoming.notifyDataSetChanged();
                    }
                },
                e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void UpdateBooking(int index) {
        emptyImage.setVisibility(View.INVISIBLE);
        if (index == 0) {
            recycler.setAdapter(adapterUpcoming);
            if (adapterUpcoming.getItemCount() == 0) emptyImage.setVisibility(View.VISIBLE);
        } else if (index == 1) {
            recycler.setAdapter(adapterInProgress);
            if (adapterInProgress.getItemCount() == 0) emptyImage.setVisibility(View.VISIBLE);
        } else {
            recycler.setAdapter(adapterCompleted);
            if (adapterCompleted.getItemCount() == 0) emptyImage.setVisibility(View.VISIBLE);
        }
    }

    private void highlightButton(Button button) {
        button.setBackgroundColor(Color.parseColor("black")); // Hồng
        button.setTextColor(Color.WHITE);
    }

    private void unhighlightButton(Button button) {
        button.setBackgroundColor(Color.parseColor("#F3F3F3")); // Xám nhạt
        button.setTextColor(Color.BLACK);
    }

    private void handleBookingAction(Booking booking) {
        if (booking.status == null) return;
        switch (booking.status) {
            case ACCEPTED:
                Toast.makeText(getContext(), "Contacting guest...", Toast.LENGTH_SHORT).show();
                break;
            case IN_PROGRESS:
                Toast.makeText(getContext(), "Marking as completed...", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(getContext(), "No action available", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchHostBookings();
    }
}