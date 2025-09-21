package com.example.myapplication.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.Model.Review.Review;
import com.example.myapplication.data.Model.Review.ReviewWithReviewerName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<ReviewWithReviewerName> reviewList;

    public ReviewAdapter(List<ReviewWithReviewerName> reviewList) {
        this.reviewList = reviewList;
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView reviewerName, reviewDate, reviewContent, reviewerActivity;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            reviewerName = itemView.findViewById(R.id.txt_username);
            //reviewerActivity = itemView.findViewById(R.id.txt_active_years);
            reviewDate = itemView.findViewById(R.id.txt_review_date);
            reviewContent = itemView.findViewById(R.id.txt_review_content);
            ratingBar = itemView.findViewById(R.id.review_rating_bar);
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_2, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewWithReviewerName review = reviewList.get(position);
        Log.d("ReviewAdapter", "Binding review: " + review.booking_id + ", " + review.content);
        holder.reviewerName.setText(review.reviewer_name);
        Date createdDate = review.created_at; // Giả sử là java.util.Date
        long currentTime = System.currentTimeMillis();
        long createdTime = createdDate.getTime();
        long diffMillis = currentTime - createdTime;

        long diffSeconds = diffMillis / 1000;
        long diffMinutes = diffSeconds / 60;
        long diffHours = diffMinutes / 60;
        long diffDays = diffHours / 24;

        String displayText;

        if (diffSeconds < 10) {
            displayText = "vừa xong";
        } else if (diffSeconds < 60) {
            displayText = diffSeconds + " giây trước";
        } else if (diffMinutes < 60) {
            displayText = diffMinutes + " phút trước";
        } else if (diffHours < 24) {
            displayText = diffHours + " giờ trước";
        } else if (diffDays <= 7) {
            displayText = diffDays + " ngày trước";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            displayText = sdf.format(createdDate);
        }

        holder.reviewDate.setText(displayText);
        holder.reviewContent.setText(review.content);
        holder.ratingBar.setRating(review.point);
        //holder.reviewerActivity.setText(review.property_id);
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }
}

