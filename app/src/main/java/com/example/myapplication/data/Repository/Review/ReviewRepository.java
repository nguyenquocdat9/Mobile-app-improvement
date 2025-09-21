package com.example.myapplication.data.Repository.Review;

import android.content.Context;

import com.example.myapplication.data.Enum.Booking_status;
import com.example.myapplication.data.Model.Review.Review;
import com.example.myapplication.data.Model.Review.ReviewWithReviewerName;
import com.example.myapplication.data.Repository.Booking.BookingRepository;
import com.example.myapplication.data.Repository.FirebaseService;
import com.example.myapplication.data.Repository.Property.PropertyRepository;
import com.example.myapplication.data.Repository.User.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReviewRepository {
    private final FirebaseFirestore db;
    private final String COLLECTION_NAME = "reviews";
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public ReviewRepository(Context context) {
        this.db = FirebaseService.getInstance(context).getFireStore();
        this.propertyRepository = new PropertyRepository(context);
        this.bookingRepository = new BookingRepository(context);
        this.userRepository = new UserRepository(context);
    }


    public void createReview(ReviewWithReviewerName review, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.db.collection(COLLECTION_NAME).document(review.id).set(review)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // user danh gia 1 phong sau khi da hoan thanh 1 booking = tao ra danh gia cho 1 booking -> cap nhat total review cua 1 nha va cap nhat avg rating
    // ben fe goi ham nay nhe
    public void guestReviewBooking(Review review, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.bookingRepository.getBookingById(review.booking_id,
                booking -> {
                    this.userRepository.getUserByUid(booking.guest_id,
                            guest -> {
                                String name = guest.full_name;
                                ReviewWithReviewerName reviewWithReviewerName = new ReviewWithReviewerName(review, name);
                                if(booking.status == Booking_status.COMPLETED) {
                                    this.createReview(reviewWithReviewerName,
                                            unused -> {
                                                // + 1 review cho property va cap nhat avg_rating
                                                this.propertyRepository.updatePropertyAvgRatings(review.property_id, review.point,
                                                        unused1 -> {
                                                            this.propertyRepository.updatePropertyTotalReviews(review.property_id,
                                                                    unused2 -> {
                                                                        this.bookingRepository.setReviewedBooking(review.booking_id, onSuccess, e -> {
                                                                            onFailure.onFailure(new Exception("Can not set booking status to REVIEWED: " + e.getMessage()));
                                                                        });
                                                                    },
                                                                    e -> {
                                                                        onFailure.onFailure(new Exception("Average rating updated but can not add up total review: " + e.getMessage()));
                                                                    });
                                                        },
                                                        e -> {
                                                            onFailure.onFailure(new Exception("Created Review but can not update average rating: " + e.getMessage()));
                                                        });
                                            },
                                            e -> {
                                                onFailure.onFailure(new Exception("Can not create Review: " + e.getMessage()));
                                            });
                                } else {
                                    onFailure.onFailure(new Exception("Booking status must be COMPLETED"));
                                }
                            }, e -> {
                                onFailure.onFailure(new Exception("Can not get Guest by ID"));
                            });
                },
                e -> {
                    onFailure.onFailure(new Exception("Can not get Booking by ID: " + e.getMessage()));
                });
    }

    // sua so diem hoac noi dung cua Review - tao 1 class Review moi voi thong tin da duoc sua la duoc
    public void updateReview(Review oldReview, Review new_review, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.propertyRepository.updatePropertyAvgRatingWhenReviewModified(oldReview.property_id, oldReview.point, new_review.point,
                unused -> {
                    new_review.id = oldReview.id;
                    this.db.collection(COLLECTION_NAME).document(oldReview.id).set(new_review)
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(e -> {
                                onFailure.onFailure(new Exception("Không thể thêm vào DB Review mới"));
                            });
                },
                e -> {
                    onFailure.onFailure(new Exception("Không tìm thấy Review với id để cập nhật " + e.getMessage()));
                });
    }

    // xoa Review
    public void deleteReview(String id, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.db.collection(COLLECTION_NAME).document(id).delete().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    public void getReviewByID(String id, OnSuccessListener<ReviewWithReviewerName> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ReviewWithReviewerName review = documentSnapshot.toObject(ReviewWithReviewerName.class);
                        if (review != null) {
                            onSuccess.onSuccess(review);
                        } else {
                            onFailure.onFailure(new Exception("Property is null"));
                        }
                    } else {
                        onFailure.onFailure(new Exception("Property not found"));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void getAllReviewByPropertyID(String propertyID, OnSuccessListener<List<ReviewWithReviewerName>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("property_id", propertyID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots  -> {
                    List<ReviewWithReviewerName> reviewList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ReviewWithReviewerName review = document.toObject(ReviewWithReviewerName.class);
                        reviewList.add(review);
                    }
                    onSuccess.onSuccess(reviewList);
                })
                .addOnFailureListener(onFailure);
    }

}
