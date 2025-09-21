package com.example.myapplication.ui.fragments.host;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.Enum.Booking_status;
import com.example.myapplication.data.Model.Booking.Booking;
import com.example.myapplication.data.Model.Statistic.PropertyStatisticDetails;
import com.example.myapplication.data.Model.Statistic.ReviewStatisticDetails;
import com.example.myapplication.data.Repository.Booking.BookingRepository;
import com.example.myapplication.data.Repository.Property.PropertyRepository;
import com.example.myapplication.data.Repository.Statistic.StatisticRepository;
import com.example.myapplication.ui.activities.IncomeOverviewActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Locale;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class StatisticFragment extends Fragment {
    private boolean isExpanded = false;
    private boolean isCompletedExpanded = false;

    private List<Booking> bookingList = new ArrayList<>();
    private BookingRepository bookingRepository;

    private View layoutIncome;
    private View layoutResult;
    private Button buttonIncome, buttonResult;

    List<Booking> completedBookings = new ArrayList<>();
    List<Booking> upcomingBookings = new ArrayList<>();

    private StatisticRepository statisticRepository;

    // Biến lưu ngày được chọn (đầu tháng)
    private LocalDate selectedDate;
    

    @SuppressLint("NewApi")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_host_statistic, container, false);

        // Button
        buttonIncome = view.findViewById(R.id.button_income);
        buttonResult = view.findViewById(R.id.button_result);

        // Các layout được include
        layoutIncome = view.findViewById(R.id.include_income);
        layoutResult = view.findViewById(R.id.include_result);

        String hostID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        bookingRepository = new BookingRepository(requireContext());
        bookingRepository.getAllBookingsByHostID(hostID,
                bookings -> {
                    bookingList = bookings;
                    TextView totalIncome = view.findViewById(R.id.total_income);
                    TextView incomeForecast = view.findViewById(R.id.income_forecast);
                    double totalMonthIncome = incomeTotal(bookingList).getKey();
                    double currentMonthForecast = incomeTotal(bookingList).getValue();
                    DecimalFormat df = new DecimalFormat("#,###");
                    String totalMonthIncomeFormatted = df.format(totalMonthIncome);
                    String currentMonthForecastFormatted = df.format(currentMonthForecast);
                    totalIncome.setText(totalMonthIncomeFormatted + "");
                    incomeForecast.setText("Số tiền " + currentMonthForecastFormatted + " sắp tới");

                    BarChart barChart = view.findViewById(R.id.barChart);
                    List<BarEntry> entries = new ArrayList<>();
                    List<String> labels = new ArrayList<>();

                    Map<String, Double> lastFiveEntries = getIncomePerMonth(bookingList);

                    int index = 0;
                    for (Map.Entry<String, Double> entry : lastFiveEntries.entrySet()) {
                        entries.add(new BarEntry(index, entry.getValue().floatValue()));

                        String[] parts = entry.getKey().split("-");
                        String label = parts[1] + "-" + parts[0]; // "MM-YYYY"
                        labels.add(label);
                        index++;
                    }

                    BarDataSet dataSet = new BarDataSet(entries, "Doanh thu theo tháng");
                    dataSet.setColor(Color.parseColor("#E6005C"));
                    dataSet.setValueTextSize(12f);
                    BarData barData = new BarData(dataSet);
                    barChart.setData(barData);
                    barChart.setFitBars(true);

                    // Cấu hình trục X
                    XAxis xAxis = barChart.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                    xAxis.setGranularity(1f);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setTextSize(12f);
                    xAxis.setTextColor(Color.DKGRAY);
                    xAxis.setDrawGridLines(false);

                    // Chỉ hiện trục Y bên trái
                    barChart.getAxisRight().setEnabled(false);  // Ẩn trục Y bên phải
                    barChart.getAxisLeft().setEnabled(false);

                    // Cải thiện giao diện

                    barChart.getDescription().setEnabled(false); // Ẩn mô tả mặc định
                    barChart.animateY(1000); // Thêm hiệu ứng

                    barChart.invalidate();  // Refresh

                    Button upcomingButton = view.findViewById(R.id.btn_upcoming_summary);
                    LinearLayout upcomingBookingList = view.findViewById(R.id.upcoming_summary);
                    Button completedButton = view.findViewById(R.id.btn_completed_summary);
                    LinearLayout completedBookingList = view.findViewById(R.id.completed_summary);

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
                },
                e -> {
                    Toast.makeText(requireContext(), "Lỗi lấy dữ liệu", Toast.LENGTH_SHORT).show();
                });

        ImageButton buttonPickMonth = view.findViewById(R.id.buttonPickMonth);
        TextView textViewMonthYear = view.findViewById(R.id.textViewMonthYear);

        statisticRepository = new StatisticRepository(requireContext());

        // Trong onViewCreated hoặc nơi bạn khởi tạo view:
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        selectedDate = LocalDate.now();
        Log.d("selectedDate", selectedDate.toString());
        // Hiển thị tháng năm hiện tại
        textViewMonthYear.setText("Tháng " + month + ", " + year);

        // Gọi dữ liệu thống kê lần đầu với selectedDate
        loadStatistics(selectedDate, hostID, view);

        // Nút chọn tháng năm
        buttonPickMonth.setOnClickListener(v -> showMonthYearPicker(textViewMonthYear, hostID, view));

        showIncomeLayout();

        buttonIncome.setOnClickListener(v -> {
            showIncomeLayout();
            highlightButton(buttonIncome);
            unhighlightButton(buttonResult);
        });
        buttonResult.setOnClickListener(v -> {
            showResultLayout();
            highlightButton(buttonResult);
            unhighlightButton(buttonIncome);
        });

        return view;
    }

    @SuppressLint("NewApi")
    private void loadStatistics(LocalDate date, String hostId, View view) {
        boolean isCurrentMonth = date.getMonthValue() == LocalDate.now().getMonthValue() && date.getYear() == LocalDate.now().getYear();
        LocalDate finalDate;
        if (isCurrentMonth) {
            finalDate = LocalDate.now();
        } else {
            finalDate = date.withDayOfMonth(date.lengthOfMonth()); // cuối tháng
        }

        statisticRepository.getAllPropertyStatistic(hostId, finalDate,
                propertyStatistic -> {
                    TextView averagePowerTotal = view.findViewById(R.id.percent_power_total);
                    TextView averagePower = view.findViewById(R.id.percent_power);
                    TextView numberRoom = view.findViewById(R.id.number_room);
                    TextView numberDay = view.findViewById(R.id.number_day);

                    averagePowerTotal.setText(propertyStatistic.getAveragePower() + "%");
                    averagePower.setText(propertyStatistic.getAveragePower() + "%");
                    numberRoom.setText(propertyStatistic.getNumberOfProperties() + " căn");
                    numberDay.setText(propertyStatistic.getAverageTimesBookedPerMonthByAllProperties() + " ngày");

                    List<PropertyStatisticDetails> details = propertyStatistic.getDetails();

                    LinearLayout HousePowercontainer = view.findViewById(R.id.layoutHousePowerContainer);
                    LayoutInflater inflaterHousePower = LayoutInflater.from(getContext());

                    // Ví dụ: thêm 3 item
                    for (PropertyStatisticDetails detailPower : details) {
                        View itemView = inflaterHousePower.inflate(R.layout.item_house_power, HousePowercontainer, false);

                        // Cập nhật dữ liệu trong itemView nếu cần
                        TextView tvTitle = itemView.findViewById(R.id.property_title_power);
                        TextView tvPower = itemView.findViewById(R.id.percent_power_room);
                        TextView tvDays = itemView.findViewById(R.id.number_day_room);
                        ImageView tvImage = itemView.findViewById(R.id.property_image);

                        tvTitle.setText(detailPower.getName());
                        tvPower.setText(detailPower.getAveragePowerByOneRoom() + "%");
                        tvDays.setText(detailPower.getTimeUsedPerMonthByOneRoom() + " ngày");
                        Glide.with(itemView.getContext())
                                .load(detailPower.getMain_img_url())  // String URL ảnh
                                .placeholder(R.drawable.avatar_placeholder) // ảnh hiện tạm lúc load
                                .error(R.drawable.error_image) // ảnh lỗi load
                                .into(tvImage);


                        HousePowercontainer.addView(itemView);
                    }

                    Log.d("propertyStatistic", "averagePowerTotal:" + propertyStatistic.getAveragePower() +" cua " + finalDate +" ngay goc" + date);
                }, e -> {
                    Toast.makeText(requireContext(), "Lỗi lấy dữ liệu", Toast.LENGTH_SHORT).show();
                });

        statisticRepository.getAllReviewStatistic(hostId, date,
                reviewStatistic -> {
                    TextView averageRatingTotal = view.findViewById(R.id.average_rating_total);
                    TextView averageRating = view.findViewById(R.id.average_rating);
                    TextView numberReview = view.findViewById(R.id.number_reviews);
                    TextView numberFive = view.findViewById(R.id.rating_five);

                    averageRatingTotal.setText("★ " + reviewStatistic.getAverageRatings());
                    averageRating.setText("★ " + reviewStatistic.getAverageRatings() + "");
                    numberReview.setText(reviewStatistic.getNumberOfReviews() + " lượt");
                    numberFive.setText(reviewStatistic.getFiveStarRatingPercentage() + " %");

                    List<ReviewStatisticDetails> details = reviewStatistic.getDetails();

                    LinearLayout HouseRatingcontainer = view.findViewById(R.id.layoutHouseRatingContainer);
                    LayoutInflater inflaterHouseRating = LayoutInflater.from(getContext());

                    // Ví dụ: thêm 3 item
                    for (ReviewStatisticDetails detailRating : details) {
                        View itemView = inflaterHouseRating.inflate(R.layout.item_house_rating, HouseRatingcontainer, false);

                        // Cập nhật dữ liệu trong itemView nếu cần
                        TextView tvTitle = itemView.findViewById(R.id.property_title_rating);
                        TextView tvRating = itemView.findViewById(R.id.average_rating_room);
                        TextView tvReview = itemView.findViewById(R.id.number_review_room);
                        TextView tvFive = itemView.findViewById(R.id.rating_five_room);
                        ImageView tvImageRate = itemView.findViewById(R.id.property_image_rating);

                        tvTitle.setText(detailRating.getName());
                        tvRating.setText(detailRating.getAvg_rating() + "");
                        tvReview.setText(detailRating.getNumber_of_reviews() + " lượt");
                        tvFive.setText(detailRating.getFive_star_rating_percentage()+"%");
                        Glide.with(itemView.getContext())
                                .load(detailRating.getMain_photo_url())  // String URL ảnh
                                .placeholder(R.drawable.anh_avatar) // ảnh hiện tạm lúc load
                                .error(R.drawable.error_image) // ảnh lỗi load
                                .into(tvImageRate);


                        HouseRatingcontainer.addView(itemView);
                    }
                }, e -> {
                    Toast.makeText(requireContext(), "Lỗi lấy dữ liệu", Toast.LENGTH_SHORT).show();
                });

        statisticRepository.getPropertyPowerForChart(hostId, date,
                propertyStatisticDetails -> {
                    LineChart powerLineChart = view.findViewById(R.id.chartPower);
                    List<Entry> entries = new ArrayList<>();

                    for (int i = 1; i <= date.getMonthValue(); i++) {
                        double value = propertyStatisticDetails.getOrDefault(i, -1.0);
                        if (value >= 0) { // Bỏ qua dữ liệu lỗi
                            entries.add(new Entry(i, (float) value));
                        }
                    }

                    // Tạo DataSet và cấu hình
                    LineDataSet dataSet = new LineDataSet(entries, "Trung bình công suất");
                    dataSet.setColor(Color.parseColor("#FF4081"));
                    //dataSet.setColor(Color.RED);
                    dataSet.setDrawCircles(false);
                    dataSet.setLineWidth(2f);
                    dataSet.setValueTextSize(12f);
                    dataSet.setDrawValues(false);

                    // Bật fill (đổ nền dưới đường line)
                    dataSet.setDrawFilled(true);
                    dataSet.setFillAlpha(255); // Độ mờ fill

                    // Gradient drawable
                    Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fade_red);
                    dataSet.setFillDrawable(drawable);

                    // Tạo LineData và set cho LineChart
                    LineData lineData = new LineData(dataSet);
                    powerLineChart.setData(lineData);

                    // Cấu hình trục X
                    XAxis xAxis = powerLineChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setGranularity(1f); // Mỗi đơn vị là 1 tháng
                    xAxis.setGranularityEnabled(true);
                    xAxis.setLabelCount(entries.size(), true); // Đảm bảo hiển thị đủ nhãn
                    // Ẩn đường lưới trục X (dọc)
                    xAxis.setDrawGridLines(false);

                    // Ẩn trục Y phải và trái
                    powerLineChart.getAxisRight().setDrawLabels(false);
                    powerLineChart.getAxisRight().setEnabled(false); // hoặc .setDrawGridLines(false);

                    YAxis yAxisLeft = powerLineChart.getAxisLeft();
                    yAxisLeft.setAxisMinimum(0f); // Bắt đầu trục Y từ 0
                    yAxisLeft.setDrawLabels(false); // Ẩn nhãn nếu bạn không cần hiển thị số
                    yAxisLeft.setDrawGridLines(false);

                    powerLineChart.getDescription().setEnabled(false);

                    powerLineChart.invalidate(); // Vẽ lại biểu đồ

                }, e -> {
                    Toast.makeText(requireContext(), "Lỗi lấy dữ liệu", Toast.LENGTH_SHORT).show();
                });

        statisticRepository.getRatingForChart(hostId, date,
                reviewStatisticDetails -> {
                    LineChart ratingLineChart = view.findViewById(R.id.chartRating);
                    List<Entry> entries = new ArrayList<>();

                    for (int i = 1; i <= date.getMonthValue(); i++) {
                        double value = reviewStatisticDetails.getOrDefault(i, -1.0);
                        if (value >= 0) { // Bỏ qua dữ liệu lỗi
                            entries.add(new Entry(i, (float) value));
                        }
                    }

                    // Tạo DataSet và cấu hình
                    LineDataSet dataSet = new LineDataSet(entries, "Trung bình đánh giá");
                    dataSet.setColor(Color.parseColor("#FF4081"));
                    //dataSet.setColor(Color.RED);
                    dataSet.setDrawCircles(false);
                    dataSet.setLineWidth(2f);
                    dataSet.setValueTextSize(12f);
                    dataSet.setDrawValues(false);

                    // Bật fill (đổ nền dưới đường line)
                    dataSet.setDrawFilled(true);
                    dataSet.setFillAlpha(255); // Độ mờ fill

                    // Gradient drawable
                    Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fade_red);
                    dataSet.setFillDrawable(drawable);

                    // Tạo LineData và set cho LineChart
                    LineData lineData = new LineData(dataSet);
                    ratingLineChart.setData(lineData);

                    // Cấu hình trục X
                    XAxis xAxis = ratingLineChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setGranularity(1f); // Mỗi đơn vị là 1 tháng
                    xAxis.setGranularityEnabled(true);
                    xAxis.setLabelCount(entries.size(), true); // Đảm bảo hiển thị đủ nhãn
                    xAxis.setDrawGridLines(false);

                    // Ẩn trục Y phải và trái
                    ratingLineChart.getAxisRight().setDrawLabels(false);
                    ratingLineChart.getAxisRight().setEnabled(false); // hoặc .setDrawGridLines(false);

                    YAxis yAxisLeft = ratingLineChart.getAxisLeft();
                    yAxisLeft.setAxisMinimum(0f); // Bắt đầu trục Y từ 0
                    yAxisLeft.setDrawLabels(false); // Ẩn nhãn nếu bạn không cần hiển thị số
                    yAxisLeft.setDrawGridLines(false);

                    ratingLineChart.getDescription().setEnabled(false);

                    ratingLineChart.invalidate(); // Vẽ lại biểu đồ
                }, e -> {

                });
    }

    @SuppressLint("NewApi")
    private void showMonthYearPicker(TextView textViewMonthYear, String hostId, View view) {
        Dialog dialog = new Dialog(getContext(), R.style.MyDialogTheme);
        dialog.setContentView(R.layout.dialog_month_year_picker);

        NumberPicker monthPicker = dialog.findViewById(R.id.picker_month);
        NumberPicker yearPicker = dialog.findViewById(R.id.picker_year);

        if (monthPicker != null && yearPicker != null) {
            // Setup tháng
            monthPicker.setMinValue(1);
            monthPicker.setMaxValue(12);
            monthPicker.setValue(selectedDate.getMonthValue());

            // Setup năm
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            yearPicker.setMinValue(currentYear - 100);
            yearPicker.setMaxValue(currentYear + 20);
            yearPicker.setValue(selectedDate.getYear());


            // Đổi màu text nếu muốn
            int color = ContextCompat.getColor(requireContext(), R.color.black);
            forceSetNumberPickerTextColor(monthPicker, color);
            forceSetNumberPickerTextColor(yearPicker, color);
        }

        Button btnOk = dialog.findViewById(R.id.btn_ok);
        if (btnOk != null) {
            btnOk.setOnClickListener(v -> {
                int month = monthPicker.getValue();
                int year = yearPicker.getValue();

                textViewMonthYear.setText("Tháng " + month + ", " + year);

                // Luôn đặt ngày là 1
                selectedDate = LocalDate.of(year, month, 1);

                loadStatistics(selectedDate, hostId, view);
                dialog.dismiss();
            });
        }


        dialog.show();
    }
    private void forceSetNumberPickerTextColor(NumberPicker numberPicker, int color) {
        // Just set the text color for the input field - the safer approach
        try {
            // Find the EditText inside the NumberPicker
            for (int i = 0; i < numberPicker.getChildCount(); i++) {
                View child = numberPicker.getChildAt(i);
                if (child instanceof EditText) {
                    EditText editText = (EditText) child;
                    editText.setTextColor(color);
                    editText.setCursorVisible(false);
                    editText.setFocusable(false);
                    editText.setFocusableInTouchMode(false);

                    // To ensure consistent color, we also need to ensure the text doesn't change
                    // when it becomes selected
                    editText.setHighlightColor(color);
                    return;
                }
            }
        } catch (Exception e) {
            Log.e("NumberPicker", "Error setting text color: " + e.getMessage());
        }
    }
    private void highlightButton(Button button) {
        button.setBackgroundColor(Color.parseColor("black")); // Hồng
        button.setTextColor(Color.WHITE);
    }

    private void unhighlightButton(Button button) {
        button.setBackgroundColor(Color.LTGRAY); // Xám nhạt
        button.setTextColor(Color.BLACK);
    }


    private void showIncomeLayout() {
        layoutIncome.setVisibility(View.VISIBLE);
        layoutResult.setVisibility(View.GONE);
        highlightButton(buttonIncome);
        unhighlightButton(buttonResult);
    }

    private void showResultLayout() {
        layoutIncome.setVisibility(View.GONE);
        layoutResult.setVisibility(View.VISIBLE);
    }

    private Map.Entry<Double, Double> incomeTotal(List<Booking> bookings) {
        double currentMonthActual = 0;
        double currentMonthForecast = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH) + 1;

        for (Booking booking : bookings) {
            try {
                Calendar checkIn = Calendar.getInstance();
                Calendar checkOut = Calendar.getInstance();

                checkIn.setTime(sdf.parse(booking.check_in_day));
                checkOut.setTime(sdf.parse(booking.check_out_day));


                int year = checkOut.get(Calendar.YEAR);
                int month = checkOut.get(Calendar.MONTH) + 1;

                String key = String.format("%04d-%02d", year, month);

                if (year == currentYear && month == currentMonth) {
                    // Tháng hiện tại
                    if (checkOut.before(today)) {
                        currentMonthActual += booking.total_price;
                    } else {
                        currentMonthForecast += booking.total_price;
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return new AbstractMap.SimpleEntry<>(currentMonthActual + currentMonthForecast, currentMonthForecast);
    }

    private Map<String, Double> getIncomePerMonth(List<Booking> bookings) {
        Map<String, Double> monthlyTotal = new TreeMap<>(); // TreeMap tự sắp xếp theo key (yyyy-MM)
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

        Date minDate = null, maxDate = null;

        for (Booking booking : bookings) {
            try {
                Date checkOutDate = sdf.parse(booking.check_out_day);
                String monthKey = monthFormat.format(checkOutDate);

                monthlyTotal.merge(monthKey, booking.total_price, Double::sum);

                if (minDate == null || checkOutDate.before(minDate)) minDate = checkOutDate;
                if (maxDate == null || checkOutDate.after(maxDate)) maxDate = checkOutDate;

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (maxDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(maxDate);
            cal.add(Calendar.MONTH, -4); // Lùi về 4 tháng để có tổng 5 tháng

            for (int i = 0; i < 5; i++) {
                String key = monthFormat.format(cal.getTime());
                monthlyTotal.putIfAbsent(key, 0.0);
                cal.add(Calendar.MONTH, 1);
            }
        }


        // Lấy 5 tháng gần nhất
        int size = monthlyTotal.size();
        int fromIndex = Math.max(0, size - 5);
        List<Map.Entry<String, Double>> lastFive = new ArrayList<>(monthlyTotal.entrySet())
                .subList(fromIndex, size);

        // Trả về 5 tháng gần nhất (nếu muốn trả toàn bộ, return monthlyTotal)
        Map<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : lastFive) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private void loadupcomingBookings(LinearLayout container, List<Booking> bookings, int count) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
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
                View divider = new View(getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
                container.addView(divider);
            }
        }
    }

    private void loadCompletedBookings(LinearLayout container, List<Booking> bookings, int count) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
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
                View divider = new View(getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
                container.addView(divider);
            }
        }
    }

    private void showBookingDetailDialog(Booking booking, String propertyID) {
        if (booking == null) {
            Toast.makeText(getContext(), "Booking details unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create dialog with custom style
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_booking_detail);

        // Make dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Get screen width
            int screenWidth = getResources().getDisplayMetrics().widthPixels;

            // Set dialog width to 90% of screen width
            int dialogWidth = (int) (screenWidth * 0.9);

            dialog.getWindow().setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Find views
        ImageView propertyImage = dialog.findViewById(R.id.detail_property_image);
        TextView propertyName = dialog.findViewById(R.id.detail_property_name);
        TextView propertyLocation = dialog.findViewById(R.id.detail_property_location);
        TextView bookingDates = dialog.findViewById(R.id.detail_booking_dates);
        TextView status = dialog.findViewById(R.id.detail_status);
        TextView totalPrice = dialog.findViewById(R.id.detail_total_price);
        TextView bookingId = dialog.findViewById(R.id.detail_booking_id);
        TextView priceBreakdown = dialog.findViewById(R.id.detail_price_breakdown);
        MaterialButton closeButton = dialog.findViewById(R.id.btn_close_dialog);

        // Set booking dates and ID right away
        bookingDates.setText(getString(R.string.booking_dates_format,
                booking.check_in_day, booking.check_out_day));
        bookingId.setText(getString(R.string.booking_id_format, booking.id));

        // Set status with appropriate color based on booking status
        String statusText = "Trạng thái: ";
        int statusColor;

        switch(booking.status) {
            case IN_PROGRESS:
                statusText += "Đang tiến hành";
                statusColor = requireContext().getColor(R.color.black);
                break;
            case ACCEPTED:
                statusText += "Đã xác nhận";
                statusColor = requireContext().getColor(android.R.color.holo_green_dark);
                break;
            case COMPLETED:
                statusText += "Đã hoàn thành";
                statusColor = requireContext().getColor(android.R.color.holo_blue_dark);
                break;
            case CANCELLED:
                statusText += "Đã hủy";
                statusColor = requireContext().getColor(android.R.color.holo_red_dark);
                break;
            case REVIEWED:
                statusText += "Đã đánh giá";
                statusColor = requireContext().getColor(android.R.color.holo_purple);
                break;
            default:
                statusText += booking.status.toString();
                statusColor = requireContext().getColor(R.color.black);
        }

        status.setText(statusText);
        status.setTextColor(statusColor);

        // Format currency and set total price with VND instead of $
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        totalPrice.setText(getString(R.string.total_price_format,
                numberFormat.format(booking.total_price) + " VND"));

        // Calculate and set price breakdown
        try {
            // Define the expected date format - adjust this to match your actual format
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

            // Parse the dates
            Date checkInDate = dateFormat.parse(booking.check_in_day);
            Date checkOutDate = dateFormat.parse(booking.check_out_day);

            if (checkInDate != null && checkOutDate != null) {
                long differenceMs = checkOutDate.getTime() - checkInDate.getTime();
                int numberOfDays = (int) (differenceMs / (1000 * 60 * 60 * 24));

                // Ensure at least 1 day
                if (numberOfDays < 1) numberOfDays = 1;

                // Calculate price per night
                double pricePerNight = booking.total_price / numberOfDays;

                priceBreakdown.setText(String.format("%s VND × %d đêm",
                        numberFormat.format(pricePerNight),
                        numberOfDays));
            } else {
                // Fallback if parsing fails
                priceBreakdown.setText(String.format("%s VND (tổng cộng)",
                        numberFormat.format(booking.total_price)));
            }
        } catch (ParseException e) {
            // Log the error for debugging
            Log.e("BookingDetailDialog", "Date parsing error: " + e.getMessage());

            // Fallback
            priceBreakdown.setText(String.format("%s VND (tổng cộng)",
                    numberFormat.format(booking.total_price)));
        }

        // Set up close button
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Load property details
        PropertyRepository propertyRepository = new PropertyRepository(requireContext());
        propertyRepository.getPropertyById(propertyID,
                property -> {
                    // Set property name
                    propertyName.setText(property.getName());

                    // Format and set property location
                    if (property.getAddress() != null) {
                        StringBuilder addressBuilder = new StringBuilder();
                        if (property.getAddress().detailed_address != null && !property.getAddress().detailed_address.isEmpty())
                            addressBuilder.append(property.getAddress().detailed_address);
                        if (property.getAddress().ward_name != null && !property.getAddress().ward_name.isEmpty()) {
                            if (addressBuilder.length() > 0) addressBuilder.append(", ");
                            addressBuilder.append(property.getAddress().ward_name);
                        }
                        if (property.getAddress().district_name != null && !property.getAddress().district_name.isEmpty()) {
                            if (addressBuilder.length() > 0) addressBuilder.append(", ");
                            addressBuilder.append(property.getAddress().district_name);
                        }
                        if (property.getAddress().city_name != null && !property.getAddress().city_name.isEmpty()) {
                            if (addressBuilder.length() > 0) addressBuilder.append(", ");
                            addressBuilder.append(property.getAddress().city_name);
                        }
                        propertyLocation.setText(addressBuilder.toString());
                    } else {
                        propertyLocation.setText("Địa chỉ không khả dụng");
                    }

                    // Load property image with rounded corners
                    if (property.getMainPhoto() != null && !property.getMainPhoto().isEmpty()) {
                        Glide.with(requireContext())
                                .load(property.getMainPhoto())
                                .centerCrop()
                                .placeholder(R.drawable.loading_animation)
                                .error(R.drawable.avatar_placeholder)
                                .into(propertyImage);
                    } else {
                        propertyImage.setImageResource(R.drawable.avatar_placeholder);
                    }
                },
                error -> {
                    propertyName.setText("Không tìm thấy thông tin nhà");
                    propertyLocation.setText("Địa chỉ không khả dụng");
                    propertyImage.setImageResource(R.drawable.avatar_placeholder);
                }
        );

        dialog.show();
    }
}

