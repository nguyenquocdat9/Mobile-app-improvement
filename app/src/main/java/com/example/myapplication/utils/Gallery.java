package com.example.myapplication.utils;



import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import android.Manifest;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class Gallery {
    private static final int REQUEST_CODE_PICK_ONE_IMAGE = 100;
    private static final int REQUEST_CODE_PICK_MULTIPLE_IMAGES = 101;
    private static final int REQUEST_CODE_PERMISSIONS = 102;

    public static final int REQUEST_CODE = 1001;

    public static void checkAndRequestPermission(Context context) {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        // Kiểm tra quyền đã được cấp chưa
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa cấp quyền thì yêu cầu cấp quyền
            if (context instanceof Activity) {
                // Đối với Activity, dùng requestPermissions
                ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, REQUEST_CODE);
            }
        } else {
            // Nếu đã cấp quyền rồi, bạn có thể tiếp tục thực hiện hành động
            // Ví dụ: mở gallery
            openGallery(context);
        }
    }

    // Hàm mở gallery (ví dụ)
    private static void openGallery(Context context) {
        // Thực hiện hành động khi quyền đã được cấp
        // Bạn có thể mở một Intent để chọn ảnh từ gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, REQUEST_CODE);
        }
    }
}
