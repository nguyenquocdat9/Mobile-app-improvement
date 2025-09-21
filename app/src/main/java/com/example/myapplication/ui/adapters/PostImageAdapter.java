package com.example.myapplication.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.Repository.User.UserRepository;
import com.example.myapplication.ui.activities.HouseDetailActivity;
import com.example.myapplication.ui.misc.Post;
import com.example.myapplication.ui.misc.WishlistManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class PostImageAdapter extends RecyclerView.Adapter<PostImageAdapter.ImageViewHolder> {
    private final List<String> imageUrls;
    private final Context context;
    private final Post post;  // Thêm đối tượng Post để truyền thông tin vào HouseDetailActivity

    private final boolean isClickable;

    public PostImageAdapter(Context context, List<String> imageUrls, Post post, boolean isClickable) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.post = post;
        this.isClickable = isClickable;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new ImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.photo1)
                .error(R.drawable.photo1)
                .into((ImageView) holder.itemView);

//        if (position + 1 < imageUrls.size()) {
//            Glide.with(context).load(imageUrls.get(position + 1)).preload();
//        }

        if (isClickable) {
            holder.itemView.setOnClickListener(v -> {
                Log.d("PostImageAdapter", "Image clicked at position: " + position);
                WishlistManager.getInstance().addToRecentlyViewed(post, FirebaseAuth.getInstance().getCurrentUser().getUid(), new UserRepository(context));
                Intent intent = new Intent(context, HouseDetailActivity.class);
                intent.putExtra("post", post);
                context.startActivity(intent);
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }


    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

