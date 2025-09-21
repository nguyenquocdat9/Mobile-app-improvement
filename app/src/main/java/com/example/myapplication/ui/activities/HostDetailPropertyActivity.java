package com.example.myapplication.ui.activities;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.Enum.Booking_status;
import com.example.myapplication.data.Model.Booking.Booking;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Repository.Booking.BookingRepository;
import com.example.myapplication.data.Repository.Property.PropertyRepository;
import com.example.myapplication.ui.adapters.LinkingIDAdapter;
import com.example.myapplication.utils.PostConverter;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.ViewGroup;
import android.widget.CalendarView;
import com.example.myapplication.data.Repository.Booking.BookingRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

public class HostDetailPropertyActivity extends AppCompatActivity {
    private Property propertyData;

    private ImageView[] images;
    private TextView nameProperty;
    private TextView detailProperty;
    private TextView roomId;
    private ImageButton copyIDButton;
    private Button updateButton;
    private Button newLinkButton;

    private RecyclerView linkingRecycler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_detail_property);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("property_json")) {
            String json = intent.getStringExtra("property_json");

            if (json != null && !json.isEmpty()) {
                try {
                    Property property = new Gson().fromJson(json, Property.class);

                    if (property != null) {
                        propertyData = property;
                        Log.d("IntentCheck", "Nhận property thành công: " + property.name);
                    } else {
                        Log.e("IntentCheck", "Không thể parse JSON thành Property");
                    }

                } catch (JsonSyntaxException e) {
                    Log.e("IntentCheck", "Lỗi parse JSON: " + e.getMessage());
                }
            } else {
                Log.w("IntentCheck", "JSON rỗng hoặc null");
            }
        } else {
            Log.w("IntentCheck", "Không nhận được extra 'property_json'");
        }

        if (propertyData == null) {
            finish();
            return;
        }

        images = new ImageView[]{
                findViewById(R.id.image6),
                findViewById(R.id.image4),
                findViewById(R.id.image5)
        };

        nameProperty = findViewById(R.id.nameProperty);
        detailProperty = findViewById(R.id.typeProperty);

        linkingRecycler = findViewById(R.id.linkingRecycler);
        linkingRecycler.setLayoutManager(new LinearLayoutManager(this));

        roomId = findViewById(R.id.textId);
        copyIDButton = findViewById(R.id.copyButton);
        copyIDButton.setOnClickListener(v -> {
            ClipboardManager clipboardManager = ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE));
            ClipData clipData = ClipData.newPlainText("label", propertyData.id);

            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(this, "Sao chép thành công", Toast.LENGTH_SHORT).show();
        });

        FetchLinking(propertyData);
        UpdateDisplay(propertyData);

        updateButton = findViewById(R.id.updateButton);
        newLinkButton = findViewById(R.id.addLinkButton);

        updateButton.setOnClickListener(v -> {
            Gson gson = new Gson();
            String json = gson.toJson(propertyData);

            Intent createIntent = new Intent(this, CreatePropertyActivity.class);
            createIntent.putExtra("property_json", json);

            startActivity(createIntent);
        });

        newLinkButton.setOnClickListener(v -> showLinkDialog());

        ImageButton backBtn = findViewById(R.id.btnBack);
        backBtn.setOnClickListener(v -> finish());

        Button viewCalendarButton = findViewById(R.id.viewCalendarButton);
        viewCalendarButton.setOnClickListener(v -> showReadOnlyCalendarDialog());
    }

    // Call this in your activity to show the dialog
    private void showReadOnlyCalendarDialog() {
//        BookingRepository bookingRepository = new BookingRepository(this);
//        bookingRepository.getBookingsByPropertyId(propertyData.getId(),
//                bookings -> {
//                    List<long[]> bookedRanges = new ArrayList<>();
//                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
//                    for (Booking booking : bookings) {
//                        if (booking.status == Booking_status.CANCELLED) continue;
//                        try {
//                            Date start = sdf.parse(booking.check_in_day);
//                            Date end = sdf.parse(booking.check_out_day);
//                            if (start != null && end != null) {
//                                bookedRanges.add(new long[]{start.getTime(), end.getTime()});
//                            }
//                        } catch (ParseException e) {
//                            Log.e("CalendarDialog", "Date parsing error: " + e.getMessage());
//                        }
//                    }
//                    CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
//                    constraintsBuilder.setValidator(new DisabledDateRangeValidator(bookedRanges));
//
//                    // Create MaterialDatePicker without custom theme
//                    MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
//                            .setTitleText("Lịch phòng: " + propertyData.getName())
//                            .setCalendarConstraints(constraintsBuilder.build())
//                            .build();
//
//                    // Disable selection by not handling positive button
//                    datePicker.addOnPositiveButtonClickListener(selection -> { /* Do nothing */ });
//
//                    datePicker.show(getSupportFragmentManager(), "calendar_dialog");
//                },
//                error -> Toast.makeText(this, "Không thể tải lịch", Toast.LENGTH_SHORT).show()
//        );

        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Lịch phòng: " + propertyData.getName());
        builder.setTheme(R.style.CustomDatePickerStyle);

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long today = calendar.getTimeInMillis();

        List<String> bookedDates = propertyData.booked_date != null ? propertyData.booked_date : new java.util.ArrayList<>(); // dd-MM-yyyy

        CalendarConstraints.DateValidator bookedDatesValidator = new CalendarConstraints.DateValidator() {
            @Override
            public boolean isValid(long date) {
                if (date < today) return false;
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(date);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                String dateStr = sdf.format(cal.getTime());
                return bookedDates == null || !bookedDates.contains(dateStr);
            }
            @Override public int describeContents() { return 0; }
            @Override public void writeToParcel(android.os.Parcel dest, int flags) {}
        };

        constraintsBuilder.setStart(today);
        constraintsBuilder.setValidator(bookedDatesValidator);
        builder.setCalendarConstraints(constraintsBuilder.build());

        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        picker.show(getSupportFragmentManager(), picker.toString());
    }

    private void UpdateDisplay(Property property) {
        Glide.with(this)
                .load(property.getMainPhoto()) // nếu là URL, nên đổi tên hàm thành getImageUrl()
                .placeholder(R.drawable.photo1)
                .error(R.drawable.photo1)
                .into(images[0]);

        if (!property.getSub_photos().isEmpty()) {
            Glide.with(this)
                    .load(property.getSub_photos().get(0)) // nếu là URL, nên đổi tên hàm thành getImageUrl()
                    .placeholder(R.drawable.photo1)
                    .error(R.drawable.photo1)
                    .into(images[1]);
        }

        if (property.getSub_photos().size() >= 2) {
            Glide.with(this)
                    .load(property.getSub_photos().get(1)) // nếu là URL, nên đổi tên hàm thành getImageUrl()
                    .placeholder(R.drawable.photo1)
                    .error(R.drawable.photo1)
                    .into(images[2]);
        }

        nameProperty.setText(property.name);
        detailProperty.setText(PostConverter.convertPropertyToPost(property).getDetail());

        roomId.setText(String.format("ID: %s", property.id));
    }

    private void FetchProperty() {
        PropertyRepository repo = new PropertyRepository(this);

        repo.getPropertyById(propertyData.id, unused -> {
            propertyData = unused;
            FetchLinking(propertyData);
        }, e -> {
            Toast.makeText(this, "Fetch Data failed", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void FetchLinking(Property property) {
        LinkingIDAdapter adapter = new LinkingIDAdapter(property.links, null);
        linkingRecycler.setAdapter(adapter);
    }

    // Gọi hàm này trong activity hoặc fragment khi cần hiện dialog
    private void showLinkDialog() {
        // Tạo dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate layout từ file XML
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_field, null);

        // Gắn layout vào dialog
        builder.setView(dialogView);

        // Tạo AlertDialog từ builder
        AlertDialog alertDialog = builder.create();

        // Tìm các view trong dialog để xử lý sự kiện
        EditText editText = dialogView.findViewById(R.id.dialog_input);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        // Bắt sự kiện Cancel
        btnCancel.setOnClickListener(v -> alertDialog.dismiss());

        // Bắt sự kiện Save
        btnSave.setOnClickListener(v -> {
            String input = editText.getText().toString().trim();
            btnSave.setEnabled(false);

            // Xử lý giá trị nhập vào ở đây
            PropertyRepository propertyRepo = new PropertyRepository(this);

            propertyRepo.addLinksToBothProperty(propertyData.id, input, unused -> {
               Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
               FetchProperty();
               alertDialog.dismiss();
            }, e -> {
                Toast.makeText(this, "Không thành công", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            });

            alertDialog.dismiss();
        });

        // Hiển thị dialog
        alertDialog.show();
    }

    public static class DisabledDateRangeValidator implements CalendarConstraints.DateValidator {
        private final List<long[]> bookedRanges;

        public DisabledDateRangeValidator(List<long[]> bookedRanges) {
            this.bookedRanges = bookedRanges;
        }

        @Override
        public boolean isValid(long date) {
            for (long[] range : bookedRanges) {
                if (date >= range[0] && date <= range[1]) return false;
            }
            return true;
        }

        @Override
        public int describeContents() { return 0; }

        @Override
        public void writeToParcel(android.os.Parcel dest, int flags) {
            dest.writeInt(bookedRanges.size());
            for (long[] range : bookedRanges) {
                dest.writeLong(range[0]);
                dest.writeLong(range[1]);
            }
        }

        public static final Creator<DisabledDateRangeValidator> CREATOR = new Creator<DisabledDateRangeValidator>() {
            @Override
            public DisabledDateRangeValidator createFromParcel(android.os.Parcel in) {
                int size = in.readInt();
                List<long[]> ranges = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    long start = in.readLong();
                    long end = in.readLong();
                    ranges.add(new long[]{start, end});
                }
                return new DisabledDateRangeValidator(ranges);
            }

            @Override
            public DisabledDateRangeValidator[] newArray(int size) {
                return new DisabledDateRangeValidator[size];
            }
        };
    }
}
