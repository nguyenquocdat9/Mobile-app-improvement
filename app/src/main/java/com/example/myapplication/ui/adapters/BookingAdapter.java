package com.example.myapplication.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.Enum.Booking_status;
import com.example.myapplication.data.Model.Booking.Booking;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Repository.Property.PropertyRepository;
import com.example.myapplication.ui.misc.Post;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private List<Booking> bookings;
    private OnBookingActionListener listener;
    private PropertyRepository propertyRepository;
    private Post post;

    public interface OnBookingActionListener {
        void onActionClick(Booking booking);
        void onViewDetailsClick(Booking booking, Property property);
    }

    public BookingAdapter(List<Booking> bookings, OnBookingActionListener listener, Context context) {
        this.bookings = bookings;
        this.listener = listener;
        this.propertyRepository = new PropertyRepository(context);
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private Button actionButton;
        private TextView statusText;
        private TextView propertyNameText;
        private TextView propertyLocationText;
        private TextView bookingDatesText;
        private TextView bookingPriceText;
        private ImageView propertyImageView;
        public Button vietDetailsButton;

        BookingViewHolder(View itemView) {
            super(itemView);
            actionButton = itemView.findViewById(R.id.action_button);
            statusText = itemView.findViewById(R.id.status_text);
            propertyNameText = itemView.findViewById(R.id.property_name);
            propertyLocationText = itemView.findViewById(R.id.property_location);
            bookingDatesText = itemView.findViewById(R.id.booking_dates);
            bookingPriceText = itemView.findViewById(R.id.booking_price);
            propertyImageView = itemView.findViewById(R.id.property_image);
            vietDetailsButton = itemView.findViewById(R.id.view_details_button);
        }

        void bind(Booking booking) {
            // Set booking dates
            String dateRange = booking.check_in_day + " - " + booking.check_out_day;
            bookingDatesText.setText(dateRange);

            // Set booking price
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
            String formattedPrice = numberFormat.format(booking.total_price) + " VND tổng cộng";
            bookingPriceText.setText(formattedPrice);

            // Set booking status with appropriate color
            setStatusText(booking.status);

            // Set action button based on booking status
            configureActionButton(booking);

            // Load property details
            loadPropertyDetails(booking);

            // Set view details button

        }

        private void loadPropertyDetails(Booking booking) {
            propertyRepository.getPropertyById(booking.property_id, property -> {
                // Set property name
                propertyNameText.setText(property.getName());

                // Set property location by formatting the Address object correctly
                if (property.getAddress() != null) {
                    // Create a formatted address string using available fields
                    StringBuilder addressBuilder = new StringBuilder();

                    if (property.getAddress().detailed_address != null && !property.getAddress().detailed_address.isEmpty()) {
                        addressBuilder.append(property.getAddress().detailed_address);
                    }

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

                    String addressText = addressBuilder.toString();
                    propertyLocationText.setText(addressText.isEmpty() ? "Address not available" : addressText);
                } else {
                    propertyLocationText.setText("Address unavailable");
                }

                // Load property image with Glide
                String mainPhoto = property.getMainPhoto();
                if (mainPhoto != null && !mainPhoto.isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(mainPhoto)
                            .into(propertyImageView);
                } else {
                    propertyImageView.setImageResource(R.drawable.avatar_placeholder);
                }
                vietDetailsButton.setOnClickListener(v -> {
                    listener.onViewDetailsClick(booking, property);
                });
            }, e -> {
                // Handle error
                Log.e("BookingAdapter", "Failed to load property: " + e.getMessage());
                vietDetailsButton.setOnClickListener(null);
                propertyNameText.setText("Property unavailable");
                propertyLocationText.setText("Location unknown");
                propertyImageView.setImageResource(R.drawable.avatar_placeholder);
            });
        }

        private void setStatusText(Booking_status status) {
            switch (status) {
                case IN_PROGRESS:
                    statusText.setText("Đang tiến hành");
                    statusText.setTextColor(itemView.getContext().getColor(R.color.black));
                    break;
                case ACCEPTED:
                    statusText.setText("Đã xác nhận");
                    statusText.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                    break;
                case COMPLETED:
                    statusText.setText("Đã hoàn thành");
                    statusText.setTextColor(itemView.getContext().getColor(android.R.color.holo_blue_dark));
                    break;
                case CANCELLED:
                    statusText.setText("Đã hủy");
                    statusText.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                    break;
                case REVIEWED:
                    statusText.setText("Đã đánh giá");
                    statusText.setTextColor(itemView.getContext().getColor(android.R.color.holo_purple));
                    break;
                default:
                    statusText.setText(status.toString());
                    statusText.setTextColor(itemView.getContext().getColor(R.color.black));
            }
        }

        private void configureActionButton(Booking booking) {
            if (booking.status == Booking_status.ACCEPTED) {
                actionButton.setText("Hủy đặt phòng");
                actionButton.setVisibility(View.VISIBLE);
            } else if (booking.status == Booking_status.COMPLETED) {
                actionButton.setText("Đánh giá");
                actionButton.setVisibility(View.VISIBLE);
            } else {
                actionButton.setVisibility(View.GONE);
            }

            actionButton.setOnClickListener(v -> listener.onActionClick(booking));
        }
    }
}