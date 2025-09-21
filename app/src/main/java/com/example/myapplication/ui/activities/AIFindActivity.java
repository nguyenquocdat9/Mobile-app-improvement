package com.example.myapplication.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.NestedScrollingChild;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Repository.Property.PropertyRepository;
import com.example.myapplication.ui.adapters.PostAdapter;
import com.example.myapplication.ui.misc.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import org.json.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class AIFindActivity extends AppCompatActivity {

    EditText editTextRequest;
    Button btnSubmit;
    ImageButton backButton;
    RecyclerView resultContainer;

    ImageView aiIcon;
    Button detailButton;

    private boolean isTitleVisible = false;
    private int showThresholdPx;

    // Store property data from backend
    private PropertyRepository propertyRepository;
    // List to hold UI post items
    private List<Post> postList;
    // Adapter to display posts in RecyclerView
    private PostAdapter postAdapter;

    private List<Post> fullPostList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_find);

        editTextRequest = findViewById(R.id.editTextRequest);
        btnSubmit = findViewById(R.id.btnSubmit);
        resultContainer = findViewById(R.id.recyclerAI);
        detailButton = findViewById(R.id.detailButton);
        resultContainer.setLayoutManager(new LinearLayoutManager(this));
        aiIcon = findViewById(R.id.aiIcon);

        TextView folderHeaderText = findViewById(R.id.headerTitle);
        View headerLine = findViewById(R.id.headerLine);


        NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);

        // Ngưỡng tính bằng pixels (100dp)
        float dpThreshold = 50f;
        showThresholdPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dpThreshold, getResources().getDisplayMetrics()
        );

        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (scrollY > 0) {
                        headerLine.setVisibility(View.VISIBLE);
                    } else {
                        headerLine.setVisibility(View.GONE);
                    }

                    if (scrollY > showThresholdPx && !isTitleVisible) {
                        isTitleVisible = true;
                        folderHeaderText.animate().alpha(1f).setDuration(150).start();
                    } else if (scrollY <= showThresholdPx && isTitleVisible) {
                        isTitleVisible = false;
                        folderHeaderText.animate().alpha(0f).setDuration(50).start();
                    }
                }
        );

        btnSubmit.setOnClickListener(view -> {
            String userInput = editTextRequest.getText().toString().trim();
            if (!userInput.isEmpty()) {
                sendRequestToModel(userInput);
                SearchStart();
            } else {
                Toast.makeText(this, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show();
            }
        });

        backButton = findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> finish());

        // Initialize empty list and adapter
        postList = new ArrayList<>();
        fullPostList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList, false);
        resultContainer.setAdapter(postAdapter);

        // Create repository instance to interact with Firebase
        propertyRepository = new PropertyRepository(this);
        fetchBackendData();

        resultContainer.setVisibility(View.GONE);
        detailButton.setVisibility(View.GONE);
    }

    private void sendRequestToModel(String userInput) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("https://search-ai-363255354392.asia-southeast1.run.app/extract_booking");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(60000); // 10s timeout kết nối
                conn.setReadTimeout(60000);    // 10s timeout đọc dữ liệu

                // Gửi dữ liệu JSON
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("text", userInput);

                OutputStream os = conn.getOutputStream();
                os.write(jsonRequest.toString().getBytes());
                os.flush();
                os.close();

                // Nhận phản hồi
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    is.close();

                    // Chuyển về UI Thread để hiển thị Toast
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Kết nối thành công", Toast.LENGTH_SHORT).show();
                        parseAndDisplayResponse(response.toString());
                        SearchEnd();
                    });
                } else {
                    runOnUiThread(() -> {
                        SearchEnd();
                        Toast.makeText(this, "Lỗi server: " + responseCode, Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (SocketTimeoutException e) {
                runOnUiThread(() -> Toast.makeText(this, "Mất kết nối: Timeout", Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Lỗi mạng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (JSONException e) {
                runOnUiThread(() -> Toast.makeText(this, "Lỗi dữ liệu JSON", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Lỗi không xác định", Toast.LENGTH_SHORT).show());
            } finally {
                if (conn != null) {
                    conn.disconnect(); // Đảm bảo đóng kết nối
                }
            }
        }).start();
    }

    private void parseAndDisplayResponse(String json) {
//        editTextRequest.setEnabled(true);
//        btnSubmit.setEnabled(true);

        detailButton.setOnClickListener(v -> {
            NavResult(json);
        });

//        detailButton.setVisibility(View.VISIBLE);
//        resultContainer.setVisibility(View.VISIBLE);


    }

    private void NavResult(String result) {
        Intent intent = new Intent(this, AIResultActivity.class);
        intent.putExtra("property_json", result);
        startActivity(intent);
    }

    private void addResultItem(String text) {
        TextView tv = new TextView(this);
        tv.setText("• " + text);
        tv.setTextSize(22);
        tv.setTextColor(getResources().getColor(android.R.color.black));
        tv.setPadding(40, 30, 0, 8);
        resultContainer.addView(tv);
    }

//    private void FetchData(String responseAI) {
//
//        String jsonString = responseAI;
//        if (jsonString != null && !jsonString.isEmpty()) {
//            try {
//                Gson gson = new Gson();
//                AIResultActivity.PropertyResponse response = gson.fromJson(jsonString, AIResultActivity.PropertyResponse.class);
//
//                PropertyRepository repo = new PropertyRepository(this);
//                for (int i = 0; i < response.extracted_info.properties.size(); i++) {
//                    repo.getPropertyById(response.extracted_info.properties.get(i).id, property -> {
//
//                    }
//                });
//                }
//
//            } catch (Exception e) {
//                String errorMsg = "⚠️ Error parsing JSON: " + e.getMessage();
//                Log.e("JSON_ERROR", errorMsg, e);
//                textView.setText(errorMsg + "\n\nJSON Content:\n" + jsonString);
//            }
//    }

    private void SearchStart() {
        aiIcon.setVisibility(View.VISIBLE);
        AlphaAnimation blinkAnimation = new AlphaAnimation(1.0f, 0.3f); // từ sáng -> mờ
        blinkAnimation.setDuration(500); // thời gian mỗi lần mờ/sáng (ms)

        blinkAnimation.setRepeatCount(Animation.INFINITE);
        blinkAnimation.setRepeatMode(Animation.REVERSE); // lặp lại từ mờ -> sáng

        aiIcon.startAnimation(blinkAnimation);

        detailButton.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        resultContainer.setVisibility(View.GONE);
        editTextRequest.setEnabled(false);
    }

    private void SearchEnd() {
        aiIcon.clearAnimation();
        aiIcon.setVisibility(View.GONE);
        detailButton.setVisibility(View.VISIBLE);
        btnSubmit.setVisibility(View.VISIBLE);
        resultContainer.setVisibility(View.VISIBLE);
        editTextRequest.setEnabled(true);
    }
    private void fetchBackendData() {
        // Call repository method to get all properties from Firestore
        propertyRepository.getAllProperties(
                // Success callback - receives List<Property> from Firebase
                new OnSuccessListener<List<Property>>() {
                    @Override
                    public void onSuccess(List<Property> properties) {
                        // Clear existing posts
                        postList.clear();
                        fullPostList.clear();

                        // Convert each Property to Post
                        for (Property property : properties) {
                            // Format price to display with $ symbol
                            String formattedPrice = "₫" + String.format("%,.0f", property.getNormal_price()) + " cho 1 đêm";

                            // Handle null address case
                            String title = property.address.getDetailAddress() != null ?
                                    property.address.getDetailAddress() : "No location";

                            String propertyType = property.property_type.toString();
                            int maxGuest = property.max_guess;
                            int bedRooms = property.rooms.bedRooms;
                           /*
                            String livingRoomStatus = property.rooms.livingRooms.toString();
                            String kitchenStatus = property.rooms.kitchen.toString();

                            // Nếu có phòng khách, chỉ ghi "· living room"
                            String livingRoomText = "";
                            if ("available".equalsIgnoreCase(livingRoomStatus)) {
                                livingRoomText = " · phòng khách";
                            }

                            // Nếu có phòng khách, chỉ ghi "· living room"
                            String kitchenText = "";
                            if ("available".equalsIgnoreCase(kitchenStatus)) {
                                livingRoomText = " · phòng bếp";
                            }
                            */

                            // Ghép chuỗi mô tả chi tiết
                            //String detail = propertyType + " · " + maxGuest + " khách" + " · " + bedRooms + " phòng ngủ" + livingRoomText + kitchenText;

                            String detail = "Để tạm ở đây cho đỡ lỗi thôi bro, nhớ sửa lại nhé, living room và kitchen sẽ là int nhé ông bạn";
                            // Create new Post object with property data
                            Post post = new Post(
                                    property.id,
                                    property.getHost_id(),
                                    title,                    // title
                                    property.getName(),
                                    property.getMainPhoto(),               // placeholder image
                                    property.address.district_name + ", " + property.address.city_name,                        // address string
                                    property.address.getFullAddress(),
                                    detail,// property type as detail
                                    "1.200 km",                          // no distance available
                                    "Available now",                 // placeholder date range
                                    formattedPrice,
                                    property.total_reviews,
                                    property.avg_ratings,
                                    property.amenities,
                                    property.sub_photos
                            );

                            postList.add(post);
                            fullPostList.add(post);
                        }
                        // Notify adapter to refresh RecyclerView
                        postAdapter.notifyDataSetChanged();
                    }
                },
                // Failure callback - shows error toast
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                    }
                }
        );
    }
}
