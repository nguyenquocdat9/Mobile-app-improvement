package com.example.myapplication.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myapplication.R;
import com.example.myapplication.data.Enum.SortOption;
import com.example.myapplication.data.Model.Location.District;
import com.example.myapplication.data.Model.Location.Province;
import com.example.myapplication.data.Model.Property.SearchProperty;
import com.example.myapplication.data.Model.Search.SearchField;
import com.example.myapplication.data.Model.Search.SearchResponse;
import com.example.myapplication.data.Repository.Location.LocationAPIClient;
import com.example.myapplication.data.Repository.Search.PropertyAPIClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    // Tab system variables
    private LinearLayout tabHomes, tabServices, tabExperiences;
    private LinearLayout homesContent, servicesContent, experiencesContent;
    private ImageView iconHomes, iconServices, iconExperiences;
    private TextView textHomes, textServices, textExperiences;

    // Current active tab
    private enum Tab {
        HOMES, SERVICES, EXPERIENCES
    }
    private Tab currentTab = Tab.HOMES;

    // Original variables for Homes tab
    private CardView cardName, cardWhere, cardWhen;
    private LinearLayout layoutNameExpanded, layoutWhereExpanded, layoutWhenExpanded;
    private LinearLayout cardInnerLayout, cardWhereInnerLayout, cardWhenInnerLayout;
    private boolean isNameExpanded = false;
    private boolean isWhereExpanded = false;
    private boolean isWhenExpanded = false;
    private TextInputEditText editTextDepartureDate, editTextArrivalDate;
    private final Calendar departureCalendar = Calendar.getInstance();
    private final Calendar arrivalCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

    // Homes Element
    private TextInputEditText home_name;
    private TextInputEditText city_name;
    private TextInputEditText district_name;
    private TextInputEditText departure_date;
    private TextInputEditText arrival_date;

    // Services Element
    private SeekBar seekBarGuests;
    private TextView guestNumberText;
    private SeekBar seekBarBedroom;
    private TextView bedroomNumberText;
    private TextInputEditText priceText;
    private CheckBox wifi, pool, bbq, petAllow;
    // Sorting Element
    private RadioGroup sortGroup;
    private RadioButton price_asc, price_desc, rating_high, none;

    // Others ELement
    private TextView back, clearALL;
    private MaterialButton search_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize views
        initializeViews();
        setTabClickListeners();
        setClickListeners();
        setupDatePickers();

        // Set initial tab state
        switchToTab(Tab.HOMES);

        // xử lí dữ liệu nhập vào input
        // Xử lí dữ liệu tab home
        home_name = findViewById(R.id.editTextSearch);
        city_name = findViewById(R.id.editTextCity);
        district_name = findViewById(R.id.editTextDistrict);
        departure_date = findViewById(R.id.editTextDepartureDate);
        arrival_date = findViewById(R.id.editTextArrivalDate);

        seekBarGuests = findViewById(R.id.seekBarGuests);
        guestNumberText = findViewById(R.id.guest_number_max);

        // Xử lí dữ liệu tab Services
        seekBarGuests.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Tránh giá trị 0 (nếu bạn không muốn cho 0 khách)
                int guests = Math.max(progress, 1);
                String text;
                if (guests == seekBar.getMax()) {
                    text = guests + "+ ";
                } else {
                    text = guests + " ";
                }
                guestNumberText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarBedroom = findViewById(R.id.seekBarBedroom);
        bedroomNumberText = findViewById(R.id.bedroom_number_max);

        seekBarBedroom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Tránh giá trị 0 (nếu bạn không muốn cho 0 khách)
                int guests = Math.max(progress, 1);
                String text;
                if (guests == seekBar.getMax()) {
                    text = guests + "+ ";
                } else {
                    text = guests + " ";
                }
                bedroomNumberText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        priceText = findViewById(R.id.editPriceSearch);
        wifi = findViewById(R.id.checkbox_wifi);
        pool = findViewById(R.id.checkbox_pool);
        bbq = findViewById(R.id.checkbox_bbq);
        petAllow = findViewById(R.id.checkbox_pet);

        // Sort
        sortGroup = findViewById(R.id.radioGroupSort);
        price_asc = findViewById(R.id.radio_price_asc);
        price_desc = findViewById(R.id.radio_price_desc);
        rating_high = findViewById(R.id.radio_rating_high);
        none = findViewById(R.id.radio_none);

        // others
        back = findViewById(R.id.backTextView);
        clearALL = findViewById(R.id.textClearAll);
        search_button = findViewById(R.id.buttonSearch);

        search_button.setOnClickListener(v -> search());
        clearALL.setOnClickListener(v -> clearAll());
        back.setOnClickListener(v -> back());
    }

    private void search() {
        // Tab Home
        String homeName = home_name.getText().toString().trim();
        String cityName = city_name.getText().toString().trim();
        String districtName = district_name.getText().toString().trim();
        String departureDate = departure_date.getText().toString().trim();
        String arrivalDate = arrival_date.getText().toString().trim();

        // Tab Services
        int guestCount = seekBarGuests.getProgress();
        if (guestCount == 0) guestCount = 1;

        int bedroomCount = seekBarBedroom.getProgress();
        if (bedroomCount == 0) bedroomCount = 1;

        String maxPriceStr = priceText.getText().toString().trim();
        boolean hasWifi = wifi.isChecked();
        boolean hasPool = pool.isChecked();
        boolean hasBbq = bbq.isChecked();
        boolean allowsPet = petAllow.isChecked();

        SortOption selectedSortOption = SortOption.None; // mặc định
        int checkedId = sortGroup.getCheckedRadioButtonId();

        if (checkedId == R.id.radio_price_asc) {
            selectedSortOption = SortOption.Price_Low_To_High;
        } else if (checkedId == R.id.radio_price_desc) {
            selectedSortOption = SortOption.Price_High_To_Low;
        } else if (checkedId == R.id.radio_rating_high) {
            selectedSortOption = SortOption.Rating_High_To_Low;
        } else {
            selectedSortOption = SortOption.None;
        }


        // Khởi tạo builder
        SearchField.Builder builder = new SearchField.Builder();

        // Counters để theo dõi các API call
        final int[] apiCallsCompleted = {0};
        final int totalApiCalls = (!cityName.isEmpty() ? 1 : 0) + (!districtName.isEmpty() ? 1 : 0);

        // Nếu không có API calls cần thực hiện, thì build ngay
        if (totalApiCalls == 0) {
            SearchField searchField = buildSearchField(builder, homeName, guestCount, bedroomCount,
                    maxPriceStr, departureDate, arrivalDate,
                    hasWifi, hasPool, hasBbq, allowsPet,
                    null, null, selectedSortOption);
            performSearch(searchField);
            return;
        }

        // Lists để lưu kết quả từ API
        final List<Integer> cityCodesList = new ArrayList<>();
        final List<Integer> districtCodesList = new ArrayList<>();

        // Callback để check khi tất cả API calls hoàn thành
        int finalGuestCount = guestCount;
        int finalBedroomCount = bedroomCount;
        SortOption finalSelectedSortOption = selectedSortOption;
        Runnable checkCompletion = () -> {
            if (apiCallsCompleted[0] == totalApiCalls) {
                SearchField searchField = buildSearchField(builder, homeName, finalGuestCount, finalBedroomCount,
                        maxPriceStr, departureDate, arrivalDate,
                        hasWifi, hasPool, hasBbq, allowsPet,
                        cityCodesList.isEmpty() ? null : cityCodesList,
                        districtCodesList.isEmpty() ? null : districtCodesList, finalSelectedSortOption);
                performSearch(searchField);
            }
        };

        // Tạo LocationAPIClient
        LocationAPIClient locationAPIClient = new LocationAPIClient();

        // Tìm kiếm city code nếu có cityName
        if (!cityName.isEmpty()) {
            locationAPIClient.searchProvinceByName(cityName, new LocationAPIClient.OnProvinceListCallback() {
                @Override
                public void onSuccess(List<Province> provinces) {
                    for (Province province : provinces) {
                        cityCodesList.add(province.code);
                    }
                    apiCallsCompleted[0]++;
                    checkCompletion.run();
                }

                @Override
                public void onError(String error) {
                    Log.e("SEARCH", "Error getting city codes: " + error);
                    apiCallsCompleted[0]++;
                    checkCompletion.run();
                }
            });
        }

        // Tìm kiếm district code nếu có districtName
        if (!districtName.isEmpty()) {
            locationAPIClient.searchDistrictByName(districtName, new LocationAPIClient.OnDistrictListCallback() {
                @Override
                public void onSuccess(List<District> districts) {
                    int count = 0;
                    for (District district : districts) {
                        if(count < 10) {
                            districtCodesList.add(district.code);
                        }
                        count++;
                    }
                    apiCallsCompleted[0]++;
                    checkCompletion.run();
                }

                @Override
                public void onError(String error) {
                    Log.e("SEARCH", "Error getting district codes: " + error);
                    apiCallsCompleted[0]++;
                    checkCompletion.run();
                }
            });
        }
    }

    private SearchField buildSearchField(SearchField.Builder builder, String homeName,
                                         int guestCount, int bedroomCount, String maxPriceStr,
                                         String departureDate, String arrivalDate,
                                         boolean hasWifi, boolean hasPool, boolean hasBbq,
                                         boolean allowsPet, List<Integer> cityCodes,
                                         List<Integer> districtCodes, SortOption sortOption) {

        // Chỉ thêm các giá trị không rỗng vào builder

        // Property name
        if (!homeName.isEmpty()) {
            builder.propertyName(homeName);
        }

        // City codes
        if (cityCodes != null && !cityCodes.isEmpty()) {
            builder.cityCode(cityCodes);
        }

        // District codes
        if (districtCodes != null && !districtCodes.isEmpty()) {
            builder.districtCode(districtCodes);
        }

        // Guest count (luôn có giá trị >= 1)
        builder.maxGuest(guestCount);

        // Bedroom count (luôn có giá trị >= 1)
        builder.bedRooms(bedroomCount);

        // Price range
        if (!maxPriceStr.isEmpty()) {
            try {
                double maxPrice = Double.parseDouble(maxPriceStr);
                if (maxPrice > 0) {
                    builder.priceRange(0, maxPrice); // min price = 0, max price từ input
                }
            } catch (NumberFormatException e) {
                Log.w("SEARCH", "Invalid price format: " + maxPriceStr);
            }
        }

        // Date range
        if (!departureDate.isEmpty() && !arrivalDate.isEmpty()) {
            builder.dateRange(departureDate, arrivalDate);
        }

        // Amenities - chỉ thêm những cái được check
        if (hasWifi) {
            builder.wifi(true);
        }

        if (hasPool) {
            builder.pool(true);
        }

        if (hasBbq) {
            builder.bbq(true);
        }

        if (allowsPet) {
            builder.petAllowance(true);
        }

        if(sortOption != null) {
            builder.sortOptions(sortOption.toString());
        }
        // Default pagination
        builder.pagination(0, 20);

        return builder.build();
    }

    private void performSearch(SearchField searchField) {
        String departureDate = searchField.getCheck_in_date();
        String arrivalDate = searchField.getCheck_out_date();

        if(departureDate != null) {
            departureDate = departureDate.replace("/", "-");
        }
        if(arrivalDate != null) {
            arrivalDate = arrivalDate.replace("/", "-");
        }

        searchField.setCheck_in_date(departureDate);
        searchField.setCheck_out_date(arrivalDate);

        /*
        Log.d("SEARCH_RESULT", "=== SEARCH FIELD DETAILS ===");
        Log.d("SEARCH_RESULT", "Property Name: " + searchField.getPropertyName());
        Log.d("SEARCH_RESULT", "City Code: " + searchField.getCity_codes());
        Log.d("SEARCH_RESULT", "District Code: " + searchField.getDistrict_codes());
        Log.d("SEARCH_RESULT", "Max Guest: " + searchField.getMax_guest());
        Log.d("SEARCH_RESULT", "Bed Rooms: " + searchField.getBed_rooms());
        Log.d("SEARCH_RESULT", "Min Price: " + searchField.getMin_price());
        Log.d("SEARCH_RESULT", "Max Price: " + searchField.getMax_price());
        Log.d("SEARCH_RESULT", "Check In Date: " + searchField.getCheck_in_date());
        Log.d("SEARCH_RESULT", "Check Out Date: " + searchField.getCheck_out_date());
        Log.d("SEARCH_RESULT", "TV: " + searchField.isTv());
        Log.d("SEARCH_RESULT", "Pet Allowance: " + searchField.isPetAllowance());
        Log.d("SEARCH_RESULT", "Pool: " + searchField.isPool());
        Log.d("SEARCH_RESULT", "Washing Machine: " + searchField.isWashingMachine());
        Log.d("SEARCH_RESULT", "Breakfast: " + searchField.isBreakfast());
        Log.d("SEARCH_RESULT", "BBQ: " + searchField.isBbq());
        Log.d("SEARCH_RESULT", "WiFi: " + searchField.isWifi());
        Log.d("SEARCH_RESULT", "Air Conditioner: " + searchField.isAirConditioner());
        Log.d("SEARCH_RESULT", "Page: " + searchField.getPage());
        Log.d("SEARCH_RESULT", "Hits Per Page: " + searchField.getHitsPerPage());
        Log.d("SEARCH_RESULT", "========================");
        In ra tất cả các trường của SearchField
        Hà Nội
        Ba Vì
        Sóc Sơn
         */

        PropertyAPIClient propertyAPIClient = new PropertyAPIClient();
        propertyAPIClient.searchProperties(searchField, new PropertyAPIClient.OnPropertyCallback() {
            @Override
            public void onSuccess(SearchResponse response) {
                List<String> result_id = new ArrayList<>();
                List<SearchProperty> hits = response.getResults().getHits();
                for(SearchProperty property : hits) {
                    result_id.add(property.getObjectID());
                }
                // Log.d("SEARCH ID", String.valueOf(result_id.size()));

                Intent intent = new Intent(SearchActivity.this, SearchedPropertyList.class);
                intent.putExtra(SearchedPropertyList.EXTRA_SEARCH_FIELD, searchField);
                intent.putStringArrayListExtra(SearchedPropertyList.EXTRA_PROPERTY_IDS, (ArrayList<String>) result_id);
                startActivity(intent);

            }

            @Override
            public void onError(String errorMessage) {
                Log.e("SEARCH_RESULT", "Error searching properties: " + errorMessage);
            }
        });

    }

    private void clearAll() {
        // Tab Home
        home_name.setText("");
        city_name.setText("");
        district_name.setText("");
        departure_date.setText("");
        arrival_date.setText("");

        // Tab Services
        seekBarGuests.setProgress(10);
        priceText.setText("");
        wifi.setChecked(false);
        pool.setChecked(false);
        bbq.setChecked(false);
        petAllow.setChecked(false);

        // Sort
        sortGroup.clearCheck();
    }

    private void back() {
        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // optional: kết thúc activity hiện tại
    }

    /**
     * Initialize all view components including tabs
     */
    private void initializeViews() {
        // Tab views
        tabHomes = findViewById(R.id.tabHomes);
        tabServices = findViewById(R.id.tabServices);
        tabExperiences = findViewById(R.id.tabExperiences);

        // Content views
        homesContent = findViewById(R.id.homesContent);
        servicesContent = findViewById(R.id.servicesContent);
        experiencesContent = findViewById(R.id.experiencesContent);

        // Tab icons and texts
        iconHomes = findViewById(R.id.iconHomes);
        iconServices = findViewById(R.id.iconServices);
        iconExperiences = findViewById(R.id.iconExperiences);
        textHomes = findViewById(R.id.textHomes);
        textServices = findViewById(R.id.textServices);
        textExperiences = findViewById(R.id.textExperiences);

        // Name section (Homes tab)
        cardName = findViewById(R.id.cardName);
        layoutNameExpanded = findViewById(R.id.layoutNameExpanded);
        cardInnerLayout = findViewById(R.id.cardInnerLayout);

        // Where section (Homes tab)
        cardWhere = findViewById(R.id.cardWhere);
        layoutWhereExpanded = findViewById(R.id.layoutWhereExpanded);
        cardWhereInnerLayout = findViewById(R.id.cardWhereInnerLayout);

        // When section (Homes tab)
        cardWhen = findViewById(R.id.cardWhen);
        layoutWhenExpanded = findViewById(R.id.layoutWhenExpanded);
        cardWhenInnerLayout = findViewById(R.id.cardWhenInnerLayout);
        editTextDepartureDate = findViewById(R.id.editTextDepartureDate);
        editTextArrivalDate = findViewById(R.id.editTextArrivalDate);

        // Set tomorrow as default for arrival date
        arrivalCalendar.add(Calendar.DAY_OF_MONTH, 1);
    }

    /**
     * Set up click listeners for tab switching
     */
    private void setTabClickListeners() {
        tabHomes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToTab(Tab.HOMES);
            }
        });

        tabServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToTab(Tab.SERVICES);
            }
        });

        tabExperiences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToTab(Tab.EXPERIENCES);
            }
        });
    }

    /**
     * Switch to the specified tab
     */
    private void switchToTab(Tab tab) {
        currentTab = tab;

        // Hide all content
        homesContent.setVisibility(View.GONE);
        servicesContent.setVisibility(View.GONE);
        experiencesContent.setVisibility(View.GONE);

        // Reset all tab styles
        resetTabStyles();

        // Show selected content and update tab style
        switch (tab) {
            case HOMES:
                homesContent.setVisibility(View.VISIBLE);
                setActiveTabStyle(textHomes, iconHomes);
                break;
            case SERVICES:
                servicesContent.setVisibility(View.VISIBLE);
                setActiveTabStyle(textServices, iconServices);
                break;
            case EXPERIENCES:
                experiencesContent.setVisibility(View.VISIBLE);
                setActiveTabStyle(textExperiences, iconExperiences);
                break;
        }
    }

    /**
     * Reset all tab styles to inactive state
     */
    private void resetTabStyles() {
        // Reset text colors
        textHomes.setTextColor(getResources().getColor(android.R.color.darker_gray));
        textServices.setTextColor(getResources().getColor(android.R.color.darker_gray));
        textExperiences.setTextColor(getResources().getColor(android.R.color.darker_gray));

        // Reset text styles
        textHomes.setTypeface(null, android.graphics.Typeface.NORMAL);
        textServices.setTypeface(null, android.graphics.Typeface.NORMAL);
        textExperiences.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Reset icon colors (you might need to create different drawable resources for different states)
        iconHomes.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        iconServices.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        iconExperiences.setColorFilter(getResources().getColor(android.R.color.darker_gray));
    }

    /**
     * Set active style for the selected tab
     */
    private void setActiveTabStyle(TextView textView, ImageView iconView) {
        // Set active text color and style
        textView.setTextColor(getResources().getColor(R.color.colorAccent)); // You might need to define this color
        textView.setTypeface(null, android.graphics.Typeface.BOLD);

        // Set active icon color
        iconView.setColorFilter(getResources().getColor(R.color.colorAccent));
    }

    /**
     * Set up click listeners for all expandable cards (Homes tab only)
     */
    private void setClickListeners() {
        if (cardName != null) {
            cardName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleNameExpansion();
                }
            });
        }

        if (cardWhere != null) {
            cardWhere.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleWhereExpansion();
                }
            });
        }

        if (cardWhen != null) {
            cardWhen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleWhenExpansion();
                }
            });
        }
    }

    /**
     * Set up date pickers for departure and arrival date fields
     */
    private void setupDatePickers() {
        if (editTextDepartureDate != null) {
            editTextDepartureDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog(true);
                }
            });
        }

        if (editTextArrivalDate != null) {
            editTextArrivalDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog(false);
                }
            });
        }
    }

    /**
     * Show date picker dialog for selecting dates
     */
    private void showDatePickerDialog(final boolean isDeparture) {
        Calendar calendar = isDeparture ? departureCalendar : arrivalCalendar;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        String formattedDate = dateFormat.format(selectedDate.getTime());

                        if (isDeparture) {
                            departureCalendar.set(year, month, dayOfMonth);
                            editTextDepartureDate.setText(formattedDate);

                            // If arrival date is before departure date, update it
                            if (arrivalCalendar.before(departureCalendar)) {
                                Calendar newArrival = (Calendar) departureCalendar.clone();
                                newArrival.add(Calendar.DAY_OF_MONTH, 1);
                                arrivalCalendar.setTime(newArrival.getTime());
                                editTextArrivalDate.setText(dateFormat.format(arrivalCalendar.getTime()));
                            }
                        } else {
                            arrivalCalendar.set(year, month, dayOfMonth);
                            editTextArrivalDate.setText(formattedDate);
                        }
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Set minimum date for departure to today
        if (isDeparture) {
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        } else {
            // Set minimum date for arrival to departure date + 1 day
            Calendar minDate = Calendar.getInstance();
            minDate.setTime(departureCalendar.getTime());
            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    /**
     * Toggle the expansion of the Name search card
     */
    private void toggleNameExpansion() {
        if (currentTab != Tab.HOMES) return;

        if (isNameExpanded) {
            // Collapse the name section
            layoutNameExpanded.setVisibility(View.GONE);
            cardInnerLayout.setBackgroundResource(android.R.color.white); // Remove border
        } else {
            // Expand the name section
            layoutNameExpanded.setVisibility(View.VISIBLE);
            cardInnerLayout.setBackgroundResource(R.drawable.bg_card_with_border); // Add border
        }
        isNameExpanded = !isNameExpanded;
    }

    /**
     * Toggle the expansion of the Where search card
     */
    private void toggleWhereExpansion() {
        if (currentTab != Tab.HOMES) return;

        if (isWhereExpanded) {
            // Collapse the where section
            layoutWhereExpanded.setVisibility(View.GONE);
            cardWhereInnerLayout.setBackgroundResource(android.R.color.white); // Remove border
        } else {
            // Expand the where section
            layoutWhereExpanded.setVisibility(View.VISIBLE);
            cardWhereInnerLayout.setBackgroundResource(R.drawable.bg_card_with_border); // Add border
        }
        isWhereExpanded = !isWhereExpanded;
    }

    /**
     * Toggle the expansion of the When search card
     */
    private void toggleWhenExpansion() {
        if (currentTab != Tab.HOMES) return;

        if (isWhenExpanded) {
            // Collapse the when section
            layoutWhenExpanded.setVisibility(View.GONE);
            cardWhenInnerLayout.setBackgroundResource(android.R.color.white); // Remove border
        } else {
            // Expand the when section
            layoutWhenExpanded.setVisibility(View.VISIBLE);
            cardWhenInnerLayout.setBackgroundResource(R.drawable.bg_card_with_border); // Add border
        }
        isWhenExpanded = !isWhenExpanded;
    }
}