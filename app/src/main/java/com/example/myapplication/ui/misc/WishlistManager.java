package com.example.myapplication.ui.misc;

import android.util.Log;

import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Repository.User.UserRepository;
import com.example.myapplication.utils.PostConverter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class WishlistManager {
    private static WishlistManager instance;
    private List<WishlistFolder> folders;
    private WishlistFolder recentViewedFolder;

    private WishlistFolder interestedFolder;


    private WishlistManager() {
        folders = new ArrayList<>();
        recentViewedFolder = new WishlistFolder("Đã xem gần đây");
        interestedFolder = new WishlistFolder("Yêu thích");
        folders.add(recentViewedFolder);
        folders.add(interestedFolder);
    }

    public static WishlistManager getInstance() {
        if (instance == null) {
            instance = new WishlistManager();
        }
        return instance;
    }

    public void loadUserWishlist(
            String userId,
            UserRepository userRepository,
            OnSuccessListener<Void> onSuccess,
            OnFailureListener onFailure
    ) {
        // Load wish_list
        userRepository.getPropertyInUserWishList(userId,
                wishList -> {
                    interestedFolder.getPosts().clear();
                    interestedFolder.getPosts().addAll(PostConverter.convertPropertiesToPosts(wishList));


                    // Sau khi load wish list xong, tiếp tục load recent list
                    userRepository.getPropertyInUserRecentList(userId,
                            recentList -> {
                                recentViewedFolder.getPosts().clear();
                                recentViewedFolder.getPosts().addAll(PostConverter.convertPropertiesToPosts(recentList));
                                onSuccess.onSuccess(null); // Tất cả đã load xong
                            },
                            onFailure
                    );
                },
                onFailure
        );
    }


    // Thêm vào "Recently viewed"
//    public void addToRecentlyViewed(Post post) {
//        if (recentViewedFolder.getPosts().contains(post)) {
//            recentViewedFolder.removePost(post);
//        }
//        recentViewedFolder.getPosts().add(0, post); // luôn thêm lên đầu
//    }

    public void addToRecentlyViewed(Post post, String userId, UserRepository userRepository) {
        // Kiểm tra xem bài viết đã có trong danh sách chưa, nếu có thì bỏ đi rồi thêm vào đầu
        if (recentViewedFolder.getPosts().contains(post)) {
            recentViewedFolder.removePost(post);
        }
        recentViewedFolder.getPosts().add(0, post); // thêm bài viết vào đầu danh sách

        // Lưu bài viết vào Firebase (danh sách "recentlyViewed" của người dùng)
        userRepository.addRecentList(userId, post.getId(), new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Thành công khi cập nhật vào Firebase
                Log.d("WishlistManager", "Post added to Recently Viewed in Firebase");
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // Lỗi khi cập nhật vào Firebase
                Log.e("WishlistManager", "Error adding post to Recently Viewed in Firebase", e);
            }
        });
    }


    // Thêm vào 'Mục yêu thích"
//    public void addToInterestedView(Post post) {
//        interestedFolder.getPosts().add(0, post);
//    }

    public void addToInterestedView(Post post, String userId, UserRepository userRepository) {
        if (!interestedFolder.getPosts().contains(post)) {
            interestedFolder.getPosts().add(0, post);

            // Đồng bộ lên Firebase
            userRepository.addToWishList(
                    userId,
                    post.getId(),
                    unused -> {
                        // Thành công - bạn có thể log nếu muốn
                    },
                    e -> {
                        // Thất bại - xử lý nếu cần
                    }
            );
        }
    }


    // Xóa khỏi folder "Yêu thích"
//    public void removeFromInterestedView(Post post) {
//        if (interestedFolder.getPosts().contains(post)) {
//            interestedFolder.removePost(post);
//        }
//    }

    public void removeFromInterestedView(Post post, String userId, UserRepository userRepository) {
        if (interestedFolder.getPosts().contains(post)) {
            interestedFolder.removePost(post);

            // Xóa trên Firebase
            userRepository.removeFromWishList(
                    userId,
                    post.getId(),
                    unused -> {
                        // Thành công - có thể log
                    },
                    e -> {
                        // Thất bại - xử lý lỗi nếu cần
                    }
            );
        }
    }



    // Kiểm tra post có trong folder wishlist nào không (trừ "Recently viewed")
    public boolean isPostInInterestedWishlist(Post post) {
        return interestedFolder.getPosts().contains(post);
    }

    public List<WishlistFolder> getFolders() {
        return folders;
    }
}
