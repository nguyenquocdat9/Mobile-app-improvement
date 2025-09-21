package com.example.myapplication.ui;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.data.Enum.PropertyStatus;
import com.example.myapplication.data.Model.Booking.Booking;
import com.example.myapplication.data.Model.Location.District;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Model.Property.SearchProperty;
import com.example.myapplication.data.Model.Review.Review;
import com.example.myapplication.data.Model.Search.BookedDateRequest;
import com.example.myapplication.data.Model.Search.SearchResponse;
import com.example.myapplication.data.Model.Statistic.PropertyStatistic;
import com.example.myapplication.data.Model.Statistic.PropertyStatisticDetails;
import com.example.myapplication.data.Model.Statistic.ReviewStatisticDetails;
import com.example.myapplication.data.Repository.Auth.AuthRepository;
import com.example.myapplication.data.Repository.Booking.BookingRepository;
import com.example.myapplication.data.Repository.Conversation.ConversationRepository;
import com.example.myapplication.data.Repository.Location.LocationAPIClient;
import com.example.myapplication.data.Repository.Property.PropertyRepository;
import com.example.myapplication.data.Repository.Review.ReviewRepository;
import com.example.myapplication.data.Repository.Search.PropertyAPIClient;
import com.example.myapplication.data.Repository.Statistic.StatisticRepository;

public class TestActivity extends AppCompatActivity {
    private static final String TAG = "TestActivity";
    private StatisticRepository statisticRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String hostID = "v4528ioquLTQbtmKYieS3quQUsp2";
        statisticRepository = new StatisticRepository(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            statisticRepository.getPropertyPowerForChart(hostID, LocalDate.of(2025, 5, 30), result -> {
                for (Map.Entry<Integer, Double> entry : result.entrySet()) {
                    Integer key = entry.getKey();
                    Double value = entry.getValue();
                    Log.d(TAG, "Month: " + key + ", Average Power: " + value);
                }
            }, e -> {

            });
        }
    }


}