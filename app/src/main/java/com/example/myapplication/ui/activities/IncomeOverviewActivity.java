package com.example.myapplication.ui.activities;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.Model.Booking.Booking;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Repository.Property.PropertyRepository;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.myapplication.data.Enum.Booking_status;
import com.google.android.material.button.MaterialButton;


public class IncomeOverviewActivity extends AppCompatActivity {
    private boolean isExpanded = false;
    private boolean isCompletedExpanded = false;

    List<Booking> completedBookings = new ArrayList<>();
    List<Booking> upcomingBookings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_overview); // layout bạn đã thiết kế

        TextView total_income = findViewById(R.id.total_income);
        TextView income_forecast = findViewById(R.id.income_forecast);
        Button upcomingButton = findViewById(R.id.btn_upcoming_summary);
        LinearLayout upcomingBookingList = findViewById(R.id.upcoming_summary);
        Button completedButton = findViewById(R.id.btn_completed_summary);
        LinearLayout completedBookingList = findViewById(R.id.completed_summary);

        double totalIncome = getIntent().getDoubleExtra("total_income", 0.0);
        double currentMonthForecast = getIntent().getDoubleExtra("current_month_forecast", 0.0);
        List<Booking> bookings = (List<Booking>) getIntent().getSerializableExtra("bookings");

        // ✅ Sắp xếp theo ngày check-in (định dạng dd-MM-yyyy)
        if (bookings != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Collections.sort(bookings, (b1, b2) -> {
                try {
                    Date d1 = sdf.parse(b1.check_out_day);
                    Date d2 = sdf.parse(b2.check_out_day);
                    return d1.compareTo(d2); // tăng dần. Đổi thành d2.compareTo(d1) nếu muốn giảm dần.
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            });

            Log.d("bookings_size", bookings.size() + "");
        } else {
            Log.e("transactions", "transactions is null");
        }


        for (Booking booking : bookings) {
            if (booking.status == Booking_status.COMPLETED || booking.status == Booking_status.REVIEWED) {
                completedBookings.add(booking);
            } else if (booking.status == Booking_status.ACCEPTED) {
                upcomingBookings.add(booking);
            }
        }

        DecimalFormat df = new DecimalFormat("#,###");

        // Ví dụ hiển thị tổng thu nhập
        String formattedTotalIncome = df.format(totalIncome);
        total_income.setText("₫" + formattedTotalIncome);

        // Nếu muốn hiển thị riêng từng khoản, bạn cần TextView riêng, ví dụ:
        // actualIncomeTextView.setText("Thực tế: ₫" + df.format(currentMonthActual));
        income_forecast.setText("Số tiền ₫" + df.format(currentMonthForecast) + " sắp tới");

        HashMap<String, Double> monthlyTotal =
                (HashMap<String, Double>) getIntent().getSerializableExtra("monthly_income_map");

        Map<String, Double> sortedMonthlyTotal = new LinkedHashMap<>();
        monthlyTotal.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // hoặc .comparingByKey(Comparator.reverseOrder()) nếu muốn ngược
                .forEachOrdered(entry -> sortedMonthlyTotal.put(entry.getKey(), entry.getValue()));

        int size = sortedMonthlyTotal.size();
        int fromIndex = Math.max(0, size - 5);

        List<Map.Entry<String, Double>> lastFiveEntries = new ArrayList<>(
                new ArrayList<>(sortedMonthlyTotal.entrySet()).subList(fromIndex, size)
        );


        BarChart barChart = findViewById(R.id.barChart);
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Double> entry : lastFiveEntries) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));

            String key = entry.getKey(); // ví dụ "2023-05"
            String[] parts = key.split("-");
            String label = parts[1] + "-" + parts[0]; // "05-2023"
            labels.add(label);
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu theo tháng");
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Cấu hình trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Chỉ hiện trục Y bên trái
        barChart.getAxisRight().setEnabled(false);  // Ẩn trục Y bên phải
        barChart.getAxisLeft().setEnabled(false);

        // Cải thiện giao diện
        dataSet.setColor(Color.parseColor("#E6005C"));
        dataSet.setValueTextSize(12f);
        barChart.getDescription().setEnabled(false); // Ẩn mô tả mặc định
        barChart.animateY(1000); // Thêm hiệu ứng

        barChart.invalidate();  // Refresh

        ImageButton backButton = findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> finish());

        ImageButton closeButton = findViewById(R.id.btnClose);
        closeButton.setOnClickListener(v -> finish());

        // hien cac giao dich
        loadupcomingBookings(upcomingBookingList, upcomingBookings, 3);

        upcomingButton.setOnClickListener(v -> {
            upcomingBookingList.removeAllViews();
            if (isExpanded) {
                loadupcomingBookings(upcomingBookingList, upcomingBookings, 3);
                upcomingButton.setText("Xem tất cả giao dịch sắp tới");
            } else {
                loadupcomingBookings(upcomingBookingList, upcomingBookings, upcomingBookings.size());
                upcomingButton.setText("Ẩn bớt các giao dịch");
            }
            isExpanded = !isExpanded;
        });

        Collections.reverse(completedBookings);

        loadCompletedBookings(completedBookingList, completedBookings, 3);

        completedButton.setOnClickListener(v -> {
            completedBookingList.removeAllViews();
            if (isCompletedExpanded) {
                loadCompletedBookings(completedBookingList, completedBookings, 3);
                completedButton.setText("Xem tất cả các khoản đã thanh toán");
            } else {
                loadCompletedBookings(completedBookingList, completedBookings, completedBookings.size());
                completedButton.setText("Ẩn bớt các khoản đã thanh toán");
            }
            isCompletedExpanded = !isCompletedExpanded;
        });
    }

    //Ham hien thi cac giao dich trong container
    private void loadupcomingBookings(LinearLayout container, List<Booking> bookings, int count) {
        LayoutInflater inflater = LayoutInflater.from(this);
        container.removeAllViews();

        for (int i = 0; i < Math.min(count, bookings.size()); i++) {
            Booking t = bookings.get(i);

            View itemView = inflater.inflate(R.layout.item_upcoming_income, container, false);

            TextView date = itemView.findViewById(R.id.income_date);
            TextView income = itemView.findViewById(R.id.income_amount);
            ImageView icon = itemView.findViewById(R.id.button_explore);

            date.setText(t.check_out_day);
            DecimalFormat df = new DecimalFormat("#,###");
            String totalIncome = df.format(t.total_price);
            income.setText(totalIncome);

            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBookingDetailDialog(t, t.property_id);
                }
            });

            container.addView(itemView);

            // Divider (trừ item cuối cùng)
            if (i < count - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
                container.addView(divider);
            }
        }
    }

    private void loadCompletedBookings(LinearLayout container, List<Booking> bookings, int count) {
        LayoutInflater inflater = LayoutInflater.from(this);
        container.removeAllViews();

        for (int i = 0; i < Math.min(count, bookings.size()); i++) {
            Booking t = bookings.get(i);

            View itemView = inflater.inflate(R.layout.item_completed_income, container, false);

            TextView date = itemView.findViewById(R.id.completed_date);
            TextView income = itemView.findViewById(R.id.completed_amount);
            ImageView icon = itemView.findViewById(R.id.button_explore);

            date.setText(t.check_out_day);
            DecimalFormat df = new DecimalFormat("#,###");
            String totalIncome = df.format(t.total_price);
            income.setText(totalIncome);

            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBookingDetailDialog(t, t.property_id);
                }
            });

            container.addView(itemView);

            // Divider (trừ item cuối cùng)
            if (i < count - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
                container.addView(divider);
            }
        }
    }

    private void showBookingDetailDialog(Booking booking, String propertyID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_booking_detail, null);

        // Find views
        ImageView propertyImage = view.findViewById(R.id.detail_property_image);
        TextView propertyName = view.findViewById(R.id.detail_property_name);
        TextView propertyLocation = view.findViewById(R.id.detail_property_location);
        TextView bookingDates = view.findViewById(R.id.detail_booking_dates);
        TextView status = view.findViewById(R.id.detail_status);
        TextView totalPrice = view.findViewById(R.id.detail_total_price);
        TextView bookingId = view.findViewById(R.id.detail_booking_id);
        TextView priceBreakdown = view.findViewById(R.id.detail_price_breakdown);

        MaterialButton closeButton = view.findViewById(R.id.btn_close_dialog);

        AlertDialog dialog = builder.setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Show dialog immediately (with placeholders), fill in property data later
        dialog.show();

        // Set booking info first
        bookingDates.setText(getString(R.string.booking_dates_format,
                booking.check_in_day, booking.check_out_day));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        totalPrice.setText(getString(R.string.total_price_format,
                currencyFormat.format(booking.total_price)));

        bookingId.setText(getString(R.string.booking_id_format, booking.id));

        // Status logic
        String statusText = "Trạng thái: ";
        int statusColor;
        switch (booking.status) {
            case IN_PROGRESS:
                statusText += "Đang tiến hành";
                statusColor = ContextCompat.getColor(this, android.R.color.black);
                break;
            case ACCEPTED:
                statusText += "Đã xác nhận";
                statusColor = ContextCompat.getColor(this, android.R.color.holo_green_dark);
                break;
            case COMPLETED:
                statusText += "Đã hoàn thành";
                statusColor = ContextCompat.getColor(this, android.R.color.holo_blue_dark);
                break;
            case CANCELLED:
                statusText += "Đã hủy";
                statusColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
                break;
            case REVIEWED:
                statusText += "Đã đánh giá";
                statusColor = ContextCompat.getColor(this, android.R.color.holo_purple);
                break;
            default:
                statusText += booking.status.toString();
                statusColor = ContextCompat.getColor(this, android.R.color.black);
        }

        status.setText(statusText);
        status.setTextColor(statusColor);

        // Price breakdown
        try {
            String[] dateFormats = {"dd/MM/yyyy", "yyyy-MM-dd", "MM/dd/yyyy"};
            Date checkInDate = null, checkOutDate = null;
            for (String format : dateFormats) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                    checkInDate = sdf.parse(booking.check_in_day);
                    checkOutDate = sdf.parse(booking.check_out_day);
                    if (checkInDate != null && checkOutDate != null) break;
                } catch (ParseException ignored) {}
            }
            if (checkInDate != null && checkOutDate != null) {
                long diffMs = checkOutDate.getTime() - checkInDate.getTime();
                int nights = Math.max(1, (int)(diffMs / (1000 * 60 * 60 * 24)));
                double pricePerNight = booking.total_price / nights;
                priceBreakdown.setText(String.format("%s × %d đêm",
                        currencyFormat.format(pricePerNight), nights));
            } else {
                priceBreakdown.setText(String.format("%s (tổng cộng)",
                        currencyFormat.format(booking.total_price)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            priceBreakdown.setText(String.format("%s (tổng cộng)",
                    currencyFormat.format(booking.total_price)));
        }

        // Load property info
        PropertyRepository propertyRepository = new PropertyRepository(this);
        propertyRepository.getPropertyById(propertyID, property -> {
            propertyName.setText(property.getName());

            // Format address
            if (property.getAddress() != null) {
                StringBuilder addressBuilder = new StringBuilder();
                if (property.getAddress().detailed_address != null && !property.getAddress().detailed_address.isEmpty())
                    addressBuilder.append(property.getAddress().detailed_address);
                if (property.getAddress().ward_name != null && !property.getAddress().ward_name.isEmpty())
                    addressBuilder.append(", ").append(property.getAddress().ward_name);
                if (property.getAddress().district_name != null && !property.getAddress().district_name.isEmpty())
                    addressBuilder.append(", ").append(property.getAddress().district_name);
                if (property.getAddress().city_name != null && !property.getAddress().city_name.isEmpty())
                    addressBuilder.append(", ").append(property.getAddress().city_name);

                propertyLocation.setText(addressBuilder.toString());
            } else {
                propertyLocation.setText("Địa chỉ không khả dụng");
            }

            // Load main photo
            if (property.getMainPhoto() != null && !property.getMainPhoto().isEmpty()) {
                Glide.with(this)
                        .load(property.getMainPhoto())
                        .centerCrop()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.avatar_placeholder)
                        .into(propertyImage);
            } else {
                propertyImage.setImageResource(R.drawable.avatar_placeholder);
            }

        }, error -> {
            // Nếu load property thất bại, vẫn hiển thị dialog
            propertyName.setText("Không tìm thấy tên nhà");
            propertyLocation.setText("Địa chỉ không khả dụng");
            propertyImage.setImageResource(R.drawable.avatar_placeholder);
        });
    }

}
