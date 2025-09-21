package com.example.myapplication.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.List;

public class AIResultActivity extends AppCompatActivity {

    MaterialTextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_result);

        textView = findViewById(R.id.resTextView);

        // Lấy JSON string từ intent extras
        Intent intent = getIntent();
        String jsonString = intent.getStringExtra("property_json");
        Log.d("Nhận request", jsonString);

        if (jsonString != null && !jsonString.isEmpty()) {
            try {
                Gson gson = new Gson();
                PropertyResponse response = gson.fromJson(jsonString, PropertyResponse.class);

                String formattedText = formatPropertyData(response);
                textView.setText(formattedText);

            } catch (Exception e) {
                String errorMsg = "⚠️ Error parsing JSON: " + e.getMessage();
                Log.e("JSON_ERROR", errorMsg, e);
                textView.setText(errorMsg + "\n\nJSON Content:\n" + jsonString);
            }
        } else {
            textView.setText("❌ No JSON data received");
        }
    }

    public String formatPropertyData(PropertyResponse response) {
        // 1. Kiểm tra dữ liệu đầu vào
        if (response == null || response.extracted_info == null ||
                response.extracted_info.properties == null) {
            return "❌ Không tìm thấy dữ liệu hợp lệ";
        }

        List<PropertyData> properties = response.extracted_info.properties;
        if (properties.isEmpty()) {
            return "ℹ️ Không có property nào";
        }

        StringBuilder result = new StringBuilder();

        // 2. Duyệt qua các property
        for (PropertyData property : properties) {
            if (property == null) continue;

            // 2.1. Thông tin cơ bản
            result.append("\n════════════════════\n")
                    .append("🏢 Property ID: ").append(property.id != null ? property.id : "N/A").append("\n")
                    .append("⭐ Điểm số: ").append(property.score).append("\n\n");

            // 2.2. Danh sách matches
            if (property.matches != null && !property.matches.isEmpty()) {
                result.append("✅ Các kết quả khớp:\n");
                for (Match match : property.matches) {
                    if (match == null) continue;
                    result.append("Tìm thấy từ " + match.from + ": ").append(match.descSent != null ? match.descSent : "N/A").append("\n")
                            .append("Yêu cầu: ").append(match.requestSent != null ? match.requestSent : "N/A").append(")\n")
                            .append("  Độ chính xác: ").append((int)(match.confidence * 100)).append("%\n\n");
                }
            } else {
                result.append("⚠️ Không có kết quả khớp\n\n");
            }

            // 2.3. Danh sách contradictions
            if (property.contradictions != null && !property.contradictions.isEmpty()) {
                result.append("❌ Mâu thuẫn:\n");
                for (Contradiction contra : property.contradictions) {
                    if (contra == null) continue;
                    result.append("Tìm thấy từ " + contra.from + ": ").append(contra.descSent != null ? contra.descSent : "N/A").append("\n")
                            .append("  (Yêu cầu: ").append(contra.requestSent != null ? contra.requestSent : "N/A").append(")\n")
                            .append("  Độ chính xác: ").append((int)(contra.confidence * 100)).append("%\n\n");
                }
            }
        }

        return result.toString();
    }

    private static String getSafeString(String value) {
        return value != null ? value : "N/A";
    }

    // Model classes
    public class PropertyResponse {
        @SerializedName("extracted_info")
        public ExtractedInfo extracted_info;

        public class ExtractedInfo {
            public List<PropertyData> properties;
        }
    }

    public static class PropertyData {
        public String id;
        public double score;
        public List<Match> matches;
        public List<Contradiction> contradictions;
    }

    public static class Match {
        @SerializedName("request_sent")
        public String requestSent;

        @SerializedName("desc_sent")
        public String descSent;

        public String from;
        public double confidence;
    }

    public static class Contradiction {
        @SerializedName("request_sent")
        public String requestSent;

        @SerializedName("desc_sent")
        public String descSent;

        public String from;
        public double confidence;
    }
}
