package com.example.myapplication.ui.adapters;

import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.Model.Property.AmenityStatus;
import com.example.myapplication.ui.misc.Amenity;

import java.util.List;

public class AmenitySetupAdapter extends RecyclerView.Adapter<AmenitySetupAdapter.AmenityViewHolder> {

    private List<Amenity> amenitiesList;

    public AmenitySetupAdapter (List<Amenity> amenitiesList) {
        this.amenitiesList = amenitiesList;
    }

    @NonNull
    @Override
    public AmenityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_set_amenity, parent, false);
        return new AmenityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AmenityViewHolder holder, int position) {
        Amenity amenity = amenitiesList.get(position);
        holder.bind(amenity, position);
    }

    @Override
    public int getItemCount() {
        return amenitiesList.size();
    }

    public List<Amenity> GetAmenities() {
        return amenitiesList;
    }

    static class AmenityViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        ImageView iconView;
        SelectorButton noneButton;
        SelectorButton hideButton;
        SelectorButton existButton;

        int position;

        Amenity amenity;
        public AmenityViewHolder(@NonNull View itemView) {
            super(itemView);

            text = itemView.findViewById(R.id.text);
            iconView = itemView.findViewById(R.id.iconView);
            noneButton = new SelectorButton(itemView.findViewById(R.id.xButton), itemView.findViewById(R.id.xicon), Color.parseColor("#D43737"));
            hideButton = new SelectorButton(itemView.findViewById(R.id.neutralButton), itemView.findViewById(R.id.neutralIcon), Color.parseColor("#ACACAC"));
            existButton = new SelectorButton(itemView.findViewById(R.id.checkButton), itemView.findViewById(R.id.checkIcon), Color.parseColor("#4BCF56"));

            noneButton.button.setOnClickListener(v -> {
                SetState(AmenityStatus.Unavailable);
            });

            hideButton.button.setOnClickListener(v -> {
                SetState(AmenityStatus.Hidden);
            });

            existButton.button.setOnClickListener(v -> {
                SetState(AmenityStatus.Available);
            });
        }

        public void ResetState() {
            noneButton.SetStats(false);
            hideButton.SetStats(false);
            existButton.SetStats(false);
        }

        public void SetState(AmenityStatus state) {
            if (state == null) state = AmenityStatus.Hidden;
            amenity.status = state;

            ResetState();
            switch (state) {
                case Unavailable:
                    noneButton.SetStats(true);
                    break;
                case Hidden:
                    hideButton.SetStats(true);
                    break;
                case Available:
                    existButton.SetStats(true);
                    break;
            }
        }

        void bind(Amenity amenity, int position) {
            this.amenity = amenity;
            text.setText(amenity.name);
            iconView.setImageResource(amenity.iconResId);
            this.position = position;

            SetState(amenity.status);
        }
    }
}



class SelectorButton {
    public CardView button;
    private ImageView icon;

    private int offColor = Color.TRANSPARENT;
    private int onColor;

    private int offIconColor = Color.parseColor("#666666");

    private boolean stats = false;
    public SelectorButton(CardView button, ImageView icon, int onColor) {
        this.button = button;
        this.icon = icon;
        this.onColor = onColor;
    }

    public void SetStats(boolean stats) {
        if (stats) {
            icon.setColorFilter(Color.WHITE);
            button.setCardBackgroundColor(onColor);
        } else {
            icon.setColorFilter(offIconColor);
            button.setCardBackgroundColor(offColor);
        }
    }
}
