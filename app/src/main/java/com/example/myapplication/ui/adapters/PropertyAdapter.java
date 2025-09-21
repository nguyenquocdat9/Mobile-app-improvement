package com.example.myapplication.ui.adapters;

// PropertyAdapter.java
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.example.myapplication.data.Enum.PropertyType;
import com.example.myapplication.data.Model.Property.Property;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {
    private List<Property> propertyList;
    private OnPropertyActionListener listener;
    private Context context;

    public interface OnPropertyActionListener {
        void onUpdateProperty(Property property);
    }

    public PropertyAdapter(List<Property> propertyList, OnPropertyActionListener listener) {
        this.propertyList = propertyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_property_management, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        Property property = propertyList.get(position);
        holder.bind(property);
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    class PropertyViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageProperty;
        private TextView textPropertyName;
        private TextView textPropertyType;
        private TextView textPropertyAddress;
        private TextView textPropertyPrice;
        private TextView textStatusLabel;
        private View statusIndicator;
        private Button textUpdateButton;
        private CardView cardProperty;

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProperty = itemView.findViewById(R.id.imageProperty);
            textPropertyName = itemView.findViewById(R.id.textPropertyName);
            textPropertyType = itemView.findViewById(R.id.textPropertyType);
            textPropertyAddress = itemView.findViewById(R.id.textPropertyAddress);
            textPropertyPrice = itemView.findViewById(R.id.textPropertyPrice);
            textStatusLabel = itemView.findViewById(R.id.textStatusLabel);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            textUpdateButton = itemView.findViewById(R.id.textUpdateButton);
            cardProperty = itemView.findViewById(R.id.cardProperty);
        }

        public void bind(Property property) {
            // Load main photo using Glide
            if (property.main_photo != null && !property.main_photo.isEmpty()) {
                Glide.with(context)
                        .load(property.main_photo)
                        .placeholder(R.drawable.placeholder_property)
                        .error(R.drawable.placeholder_property)
                        .into(imageProperty);
            } else {
                imageProperty.setImageResource(R.drawable.placeholder_property);
            }

            // Set property details
            textPropertyName.setText(property.name);
            textPropertyType.setText(getPropertyTypeText(property.property_type));

            if (property.address != null) {
                textPropertyAddress.setText(property.address.toString());
            }

            // Format price
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            textPropertyPrice.setText(formatter.format(property.normal_price) + "/đêm");

            // Set fake status (since you asked to fake it)
            PropertyStatus fakeStatus = generateFakeStatus();
            setPropertyStatus(fakeStatus);

            // Set update button click listener
            textUpdateButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUpdateProperty(property);
                }
            });

            // Set card click listener for more details
            cardProperty.setOnClickListener(v -> {
                // You can implement navigation to property details here
            });
        }

        private PropertyStatus generateFakeStatus() {
            Random random = new Random();
            int statusIndex = random.nextInt(4);
            switch (statusIndex) {
                case 0: return PropertyStatus.AVAILABLE;
                case 1: return PropertyStatus.BOOKED;
                case 2: return PropertyStatus.RENTED;
                default: return PropertyStatus.MAINTENANCE;
            }
        }

        private void setPropertyStatus(PropertyStatus status) {
            switch (status) {
                case AVAILABLE:
                    textStatusLabel.setText("Còn trống");
                    textStatusLabel.setTextColor(ContextCompat.getColor(context, R.color.status_available_text));
                    statusIndicator.setBackgroundResource(R.drawable.status_available_bg);
                    break;
                case BOOKED:
                    textStatusLabel.setText("Đã được đặt");
                    textStatusLabel.setTextColor(ContextCompat.getColor(context, R.color.status_booked_text));
                    statusIndicator.setBackgroundResource(R.drawable.status_booked_bg);
                    break;
                case RENTED:
                    textStatusLabel.setText("Đang cho thuê");
                    textStatusLabel.setTextColor(ContextCompat.getColor(context, R.color.status_rented_text));
                    statusIndicator.setBackgroundResource(R.drawable.status_rented_bg);
                    break;
                case MAINTENANCE:
                    textStatusLabel.setText("Bảo trì");
                    textStatusLabel.setTextColor(ContextCompat.getColor(context, R.color.status_maintenance_text));
                    statusIndicator.setBackgroundResource(R.drawable.status_maintenance_bg);
                    break;
            }
        }

        private String getPropertyTypeText(PropertyType type) {
            if (type == null) return "Căn hộ"; // hoặc "Không xác định"
            switch (type) {
                case Apartment:
                    return "Căn hộ";
                case House:
                    return "Nhà nguyên căn";
                case Villa:
                    return "Biệt thự";
                case Homestay:
                    return "Homestay";
                case Hotel:
                    return "Khách sạn";
                default:
                    return "Không xác định";
            }
        }
    }

    // Add these enums if you don't have them
    public enum PropertyStatus {
        AVAILABLE, BOOKED, RENTED, MAINTENANCE
    }
}
