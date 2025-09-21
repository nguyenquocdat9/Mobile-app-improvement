package com.example.myapplication.ui.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Patterns;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LinkValidator {

    // Kiểm tra cú pháp của URL
    public static boolean isValidUrl(String urlString) {
        return Patterns.WEB_URL.matcher(urlString).matches();  // Kiểm tra cú pháp URL
    }

    // Kiểm tra URI có tồn tại trên thiết bị
    public static boolean isValidUri(Context context, String uriString) {
        Uri uri = Uri.parse(uriString);

        // Kiểm tra URI kiểu file://
        if ("file".equals(uri.getScheme())) {
            File file = new File(uri.getPath());
            return file.exists(); // Kiểm tra sự tồn tại của tệp
        }

        // Kiểm tra URI kiểu content://
        else if ("content".equals(uri.getScheme())) {
            try {
                // Truy vấn qua ContentResolver
                String[] proj = { MediaStore.Images.Media._ID };
                String selection = MediaStore.Images.Media.DATA + "=?";
                String[] selectionArgs = new String[]{uri.getPath()};

                try (Cursor cursor = context.getContentResolver().query(uri, proj, selection, selectionArgs, null)) {
                    return cursor != null && cursor.moveToFirst(); // Kiểm tra sự tồn tại của URI trong hệ thống
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;  // Nếu URI không hợp lệ hoặc không tìm thấy
    }

    // Kiểm tra xem link có hợp lệ hay không, hỗ trợ cả URL và URI
    public static boolean validateLink(Context context, String link) {
        // Nếu là URL hợp lệ (chỉ cần cú pháp hợp lệ)
        if (isValidUrl(link)) {
            return true;
        }
        // Nếu là URI hợp lệ và tồn tại trên máy
        else if (isValidUri(context, link)) {
            return true;
        }
        return false;  // Nếu không hợp lệ
    }

    public static List<String> filterInvalidLinks(Context context, List<String> links) {
        List<String> validLinks = new ArrayList<>();
        for (String link : links) {
            if (validateLink(context, link)) {
                validLinks.add(link);  // Thêm link hợp lệ vào danh sách
            }
        }
        return validLinks;  // Trả về danh sách chỉ chứa các link hợp lệ
    }

    public static void loadImage(Context context, String imagePath, ImageView imageView) {
        // Kiểm tra nếu imagePath là một URL hợp lệ
        if (imagePath != null && !imagePath.isEmpty()) {
            Uri imageUri = Uri.parse(imagePath);

            // Nếu là URL hợp lệ, sử dụng Glide để tải ảnh vào ImageView
            Glide.with(context)
                    .load(imageUri) // Tải hình ảnh từ URL hoặc URI
                    .into(imageView); // Đưa ảnh vào ImageView
        }
    }
}
