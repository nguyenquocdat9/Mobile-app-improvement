package com.example.myapplication.ui.adapters;

import android.content.Context;
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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HostBookingAdapter extends RecyclerView.Adapter<HostBookingAdapter.ViewHolder> {

    private final List<Booking> bookings;
    private final OnBookingActionListener listener;
    private final Context context;
    private final PropertyRepository propertyRepository;

    public interface OnBookingActionListener {
        void onViewDetailsClick(Booking booking, Property property);
    }

    public HostBookingAdapter(List<Booking> bookings, OnBookingActionListener listener, Context context) {
        this.bookings = bookings;
        this.listener = listener;
        this.context = context;
        this.propertyRepository = new PropertyRepository(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_host_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView propertyNameText;
        private final TextView guestNameText;
        private final TextView bookingDatesText;
        private final TextView statusText;
        private final TextView bookingPriceText;
        private final ImageView propertyImageView;
        private final Button btnViewDetails;

        ViewHolder(View itemView) {
            super(itemView);
            propertyNameText = itemView.findViewById(R.id.property_name);
            guestNameText = itemView.findViewById(R.id.guest_name);
            bookingDatesText = itemView.findViewById(R.id.booking_dates);
            statusText = itemView.findViewById(R.id.status_text);
            bookingPriceText = itemView.findViewById(R.id.booking_price);
            propertyImageView = itemView.findViewById(R.id.property_image);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
        }

        void bind(final Booking booking) {
            if (booking.status != null) {
                setStatusText(booking.status);
            } else {
                statusText.setText("Pending");
            }

            // Format booking dates
            String dateRange = booking.check_in_day + " - " + booking.check_out_day;
            bookingDatesText.setText(dateRange);

            // Format price
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
            String formattedPrice = numberFormat.format(booking.total_price) + " VND";
            bookingPriceText.setText(formattedPrice);

            // Load property details and guest info
            propertyRepository.getPropertyById(booking.property_id, property -> {
                if (property != null) {
                    propertyNameText.setText(property.getName());

                    // Set guest name from user ID or other field
                    guestNameText.setText("Khách: " + (booking.guest_id != null ? booking.guest_id : "Unknown"));

                    // Load property image with Glide
                    String mainPhoto = property.getMainPhoto();
                    if (mainPhoto != null && !mainPhoto.isEmpty()) {
                        Glide.with(itemView.getContext())
                                .load(mainPhoto)
                                .into(propertyImageView);
                    } else {
                        propertyImageView.setImageResource(R.drawable.avatar_placeholder);
                    }

                    // Set up action button

                    // Set up view details button
                    btnViewDetails.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onViewDetailsClick(booking, property);
                        }
                    });
                }
            }, e -> {
                // Handle error
                Log.e("HostBookingAdapter", "Failed to load property: " + e.getMessage());
                propertyNameText.setText("Property unavailable");
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
    }
}