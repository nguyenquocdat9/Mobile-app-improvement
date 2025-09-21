package com.example.myapplication.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.Model.Property.Property;


import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SearchedListAdapter extends RecyclerView.Adapter<SearchedListAdapter.PropertyViewHolder> {

    private List<Property> propertyList;
    private Context context;
    private OnPropertyClickListener listener;

    // Interface for click events
    public interface OnPropertyClickListener {
        void onPropertyClick(Property property);
        void onFavoriteClick(Property property, int position);
    }

    public SearchedListAdapter(Context context, List<Property> propertyList, OnPropertyClickListener listener) {
        this.context = context;
        this.propertyList = propertyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_searched_property, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        Property property = propertyList.get(position);

        // Set property name
        holder.propertyName.setText(property.name);

        // Set fake distance (1,200 km)
        holder.propertyDistance.setText("1,200 km");

        // Set price
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedPrice = formatter.format(property.normal_price) + " cho 1 đêm";
        holder.propertyPrice.setText(formattedPrice);

        // Load image using Glide
        if (property.main_photo != null && !property.main_photo.isEmpty()) {
            Glide.with(context)
                    .load(property.main_photo)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image)  // You'll need to create this placeholder
                    .error(R.drawable.error_image)  // You'll need to create this error image
                    .into(holder.propertyImage);
        } else {
            holder.propertyImage.setImageResource(R.drawable.placeholder_image);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPropertyClick(property);
            }
        });

        holder.favoriteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(property, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return propertyList != null ? propertyList.size() : 0;
    }

    public void updateData(List<Property> newPropertyList) {
        this.propertyList = newPropertyList;
        notifyDataSetChanged();
    }

    public static class PropertyViewHolder extends RecyclerView.ViewHolder {
        ImageView propertyImage;
        ImageView favoriteButton;
        TextView propertyName;
        TextView propertyDistance;
        TextView propertyAvailability;
        TextView propertyPrice;

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            propertyImage = itemView.findViewById(R.id.property_image);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
            propertyName = itemView.findViewById(R.id.property_name);
            propertyDistance = itemView.findViewById(R.id.property_distance);
            propertyAvailability = itemView.findViewById(R.id.property_availability);
            propertyPrice = itemView.findViewById(R.id.property_price);
        }
    }
}