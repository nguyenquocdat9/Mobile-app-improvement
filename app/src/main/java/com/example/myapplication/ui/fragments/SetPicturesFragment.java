package com.example.myapplication.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.Model.Property.AmenityStatus;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Model.Property.Rooms;
import com.example.myapplication.interfaces.IStepValidator;
import com.example.myapplication.ui.adapters.SubPhotoAdapter;
import com.example.myapplication.ui.misc.PropertyViewModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SetPicturesFragment extends Fragment implements IStepValidator {

    private PropertyViewModel viewModel;

    private static final int REQUEST_PERMISSION_CODE = 101;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> pickMultipleImagesLauncher;

    private String mainPhoto;
    private List<String> subList = new ArrayList<String>();
    private ImageButton pickImageButton;
    private ImageButton pickMultipleImagesButton;

    private View mainPhotoInclude;
    private RecyclerView subPhotoRecycler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_pictures, container, false);

        mainPhotoInclude = view.findViewById(R.id.mainPhotoInclude);
        subPhotoRecycler = view.findViewById(R.id.subPhotoRecycler);

        ImageButton mainRemovedButton = mainPhotoInclude.findViewById(R.id.removeButton);
        mainRemovedButton.setOnClickListener(v -> {
            pickImageButton.setVisibility(View.VISIBLE);
            mainPhotoInclude.setVisibility(View.GONE);

            mainPhoto = null;
        });


        // Khởi tạo launcher để xử lý kết quả chọn ảnh (một ảnh)
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                // Xử lý ảnh được chọn (một ảnh)
                Log.d("Image URI", imageUri.toString());

                mainPhoto = imageUri.toString();
                updateDisplay(true);
            }
        });

        // Khởi tạo launcher để xử lý kết quả chọn nhiều ảnh
        pickMultipleImagesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                ClipData clipData = result.getData().getClipData();
                if (clipData != null) {
                    // Nếu chọn nhiều ảnh
                    subList.clear();
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri imageUri = clipData.getItemAt(i).getUri();
                        // Xử lý từng ảnh
                        Log.d("Multiple Image URI", imageUri.toString());

                        subList.add(imageUri.toString());
                        updateDisplay(false);
                    }
                }
            }
        });

        // Button chọn một ảnh
        pickImageButton = view.findViewById(R.id.pickMainPhoto);
        pickImageButton.setOnClickListener(v -> {
            // Kiểm tra và yêu cầu quyền trước khi mở gallery
            if (checkAndRequestPermissions()) {
                // Mở gallery chọn một ảnh
                Log.d("Test","Mo gallery");
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageLauncher.launch(intent);
            }
        });

        // Button chọn nhiều ảnh
        pickMultipleImagesButton = view.findViewById(R.id.pickSubPhoto);
        pickMultipleImagesButton.setOnClickListener(v -> {
            // Kiểm tra và yêu cầu quyền trước khi mở gallery
            if (checkAndRequestPermissions()) {
                // Mở gallery chọn nhiều ảnh
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                pickMultipleImagesLauncher.launch(intent);
            }
        });

        viewModel = new ViewModelProvider(requireActivity()).get(PropertyViewModel.class);

        applyData();

        return view;
    }

    public Bitmap getBitmapFromUri(Uri uri){
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Kiểm tra và yêu cầu quyền truy cập
    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION_CODE);
                return false;
            }

            return true;
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Nếu chưa có quyền, yêu cầu quyền
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
                return false;
            }

            return true;
        }
    }

    public void updateDisplay(boolean mainPT) {

        if (mainPT) {
            if (mainPhoto != null && !LinkValidator.validateLink(getContext(), mainPhoto)) mainPhoto = null;

            //Main photo
            ImageView mainImage = mainPhotoInclude.findViewById(R.id.photoView);
            if(mainPhoto != null) {
                LinkValidator.loadImage(getContext(), mainPhoto, mainImage);
                mainPhotoInclude.setVisibility(View.VISIBLE);
                pickImageButton.setVisibility(View.GONE);
            } else {
                mainPhotoInclude.setVisibility(View.GONE);
                pickImageButton.setVisibility(View.VISIBLE);
            }
        } else {
            subList = LinkValidator.filterInvalidLinks(getContext(), subList);

            //Sub photo
            SubPhotoAdapter adapter = new SubPhotoAdapter(getContext(), subList, new SubPhotoAdapter.OnItemRemovedListener() {
                @Override
                public void onItemRemoved(int position) {

                }
            });

            subPhotoRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            subPhotoRecycler.setAdapter(adapter);

            subPhotoRecycler.setVisibility((subList.isEmpty()) ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void applyData() {
        Property property = viewModel.getPropertyData().getValue();

        mainPhoto = property.getMainPhoto();
        subList = property.getSub_photos();

        if (subList == null) subList = new ArrayList<String>();

        updateDisplay(true);
        updateDisplay(false);
    }

    @Override
    public void save() {
        Property newValue = viewModel.getPropertyData().getValue();
        if (newValue == null) newValue = new Property();
        newValue.setMainPhoto(mainPhoto);
        newValue.setSub_photos(subList);

        viewModel.setPropertyData(newValue);
    }

    @Override
    public boolean validate(String warning) {
        return true;
    }

    @Override
    public int getStepIndex() {
        return 4;
    }
}


