package com.example.myapplication.ui.adapters;

import android.app.Notification;
import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class SubPhotoAdapter extends RecyclerView.Adapter<SubPhotoAdapter.ImageViewHolder> {

    private Context context;
    private List<String> imageUris;
    private OnItemRemovedListener removedListener;

    public SubPhotoAdapter (Context context, List<String> imageUris, OnItemRemovedListener removedListener) {
        this.context = context;
        this.imageUris = imageUris;
        this.removedListener = removedListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);

        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUris.get(position);
        Glide.with(context)
                .load(imageUrl)  // Load image from URL or URI
                .into(holder.imageView);

        holder.removeButon.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                imageUris.remove(pos);
                notifyItemRemoved(pos);
                if (removedListener!= null) {
                    removedListener.onItemRemoved(pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (imageUris == null) ? 0 : imageUris.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton removeButon;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photoView);
            removeButon = itemView.findViewById(R.id.removeButton);
        }
    }

    public interface OnItemRemovedListener {
        void onItemRemoved(int position);
    }
}
