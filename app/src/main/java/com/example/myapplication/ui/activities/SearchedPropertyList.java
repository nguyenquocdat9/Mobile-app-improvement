package com.example.myapplication.ui.activities;

import static com.example.myapplication.data.Enum.SortOption.None;
import static com.example.myapplication.data.Enum.SortOption.Price_High_To_Low;
import static com.example.myapplication.data.Enum.SortOption.Price_Low_To_High;
import static com.example.myapplication.data.Enum.SortOption.Rating_High_To_Low;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.Enum.SortOption;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Model.Search.SearchField;
import com.example.myapplication.data.Repository.Property.PropertyRepository;
import com.example.myapplication.ui.adapters.SearchedListAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchedPropertyList extends AppCompatActivity implements SearchedListAdapter.OnPropertyClickListener {

    private RecyclerView recyclerView;
    private SearchedListAdapter adapter;
    private ProgressBar progressBar;
    private List<Property> propertyList;
    private SearchField searchField;
    private List<String> propertyIds;
    private TextView noResultsText;
    private CardView searchCriteriaCard;

    // Thêm các TextView để hiển thị thông tin tìm kiếm
    private TextView propertyNameValue;
    private TextView maxGuestValue;
    private TextView bedRoomsValue;
    private TextView priceRangeValue;
    private TextView dateRangeValue;
    private TextView amenitiesValue;
    private TextView sortValue;
    public static final String EXTRA_SEARCH_FIELD = "extra_search_field";
    public static final String EXTRA_PROPERTY_IDS = "extra_property_ids";
    private SortOption sortOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searched_property_list);

        // Initialize UI components
        setupUI();

        // Get intent data
        getIntentData();

        // Initialize data
        initializeData();

        // Display search criteria
        displaySearchCriteria();

        // Set up RecyclerView
        setupRecyclerView();

        // Load data
        loadPropertyData();
    }

    private void setupUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Find UI components
        recyclerView = findViewById(R.id.property_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        noResultsText = findViewById(R.id.no_results_text);
        searchCriteriaCard = findViewById(R.id.search_criteria_card);

        // Find TextView components for search criteria
        propertyNameValue = findViewById(R.id.property_name_value);
        maxGuestValue = findViewById(R.id.max_guest_value);
        bedRoomsValue = findViewById(R.id.bed_rooms_value);
        priceRangeValue = findViewById(R.id.price_range_value);
        dateRangeValue = findViewById(R.id.date_range_value);
        amenitiesValue = findViewById(R.id.amenities_value);
        sortValue = findViewById(R.id.sort_value);
        // Set up navigation buttons

    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            // Nhận SearchField object
            if (intent.hasExtra(EXTRA_SEARCH_FIELD)) {
                searchField = intent.getParcelableExtra(EXTRA_SEARCH_FIELD);
            }

            // Nhận List<String> propertyIds
            if (intent.hasExtra(EXTRA_PROPERTY_IDS)) {
                propertyIds = intent.getStringArrayListExtra(EXTRA_PROPERTY_IDS);
                assert propertyIds != null;
                Log.d("ID","Nhận được " + propertyIds.size() + " ID");
            }
        }
    }

    private void initializeData() {
        propertyList = new ArrayList<>();

        // Log để debug
        if (searchField != null) {
            logSearchFieldInfo();
        }

        if (propertyIds != null && !propertyIds.isEmpty()) {
            Log.d("ID","Nhận được " + propertyIds.size() + " ID");
        }
    }

    private void displaySearchCriteria() {
        if (searchField == null) {
            searchCriteriaCard.setVisibility(View.GONE);
            return;
        }

        // Hiển thị property name nếu có
        if (!TextUtils.isEmpty(searchField.getPropertyName())) {
            propertyNameValue.setText(searchField.getPropertyName());
        } else {
            propertyNameValue.setText("Không");
        }

        // Hiển thị max guest
        maxGuestValue.setText(searchField.getMax_guest() > 0 ?
                String.valueOf(searchField.getMax_guest()) : "Không");

        // Hiển thị số phòng ngủ
        bedRoomsValue.setText(searchField.getBed_rooms() > 0 ?
                String.valueOf(searchField.getBed_rooms()) : "Không");

        // Hiển thị khoảng giá
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String minPrice = searchField.getMin_price() > 0 ?
                currencyFormat.format(searchField.getMin_price()) + "₫" : "0₫";
        String maxPrice = searchField.getMax_price() > 0 ?
                currencyFormat.format(searchField.getMax_price()) + "₫" : "Không giới hạn";

        if (searchField.getMin_price() > 0 || searchField.getMax_price() > 0) {
            priceRangeValue.setText(minPrice + " - " + maxPrice);
        } else {
            priceRangeValue.setText("Không");
        }

        // Hiển thị ngày check-in, check-out
        if (!TextUtils.isEmpty(searchField.getCheck_in_date()) &&
                !TextUtils.isEmpty(searchField.getCheck_out_date())) {
            dateRangeValue.setText(searchField.getCheck_in_date() + " - " +
                    searchField.getCheck_out_date());
        } else {
            dateRangeValue.setText("Không");
        }

        // Hiển thị tiện nghi
        List<String> amenities = new ArrayList<>();
        if (searchField.isTv()) amenities.add("TV");
        if (searchField.isWifi()) amenities.add("Wifi");
        if (searchField.isPool()) amenities.add("Hồ bơi");
        if (searchField.isWashingMachine()) amenities.add("Máy giặt");
        if (searchField.isBreakfast()) amenities.add("Bữa sáng");
        if (searchField.isBbq()) amenities.add("BBQ");
        if (searchField.isPetAllowance()) amenities.add("Cho phép thú cưng");
        if (searchField.isAirConditioner()) amenities.add("Điều hòa");

        if (!amenities.isEmpty()) {
            amenitiesValue.setText(TextUtils.join(", ", amenities));
        } else {
            amenitiesValue.setText("Không");
        }

        String sort = searchField.getSortOption();
        if(!TextUtils.isEmpty(sort)) {
            if(sort.equalsIgnoreCase(Rating_High_To_Low.toString())) {
                sortValue.setText("Đánh giá cao nhất");
                this.sortOption = Rating_High_To_Low;
            }
            if(sort.equalsIgnoreCase(Price_Low_To_High.toString())) {
                sortValue.setText("Giá tăng dần");
                this.sortOption = Price_Low_To_High;
            }
            if(sort.equalsIgnoreCase(Price_High_To_Low.toString())) {
                sortValue.setText("Giá giảm dần");
                this.sortOption = Price_High_To_Low;
            }
            if(sort.equalsIgnoreCase(None.toString())) {
                sortValue.setText("Không");
                this.sortOption = None;
            }
        } else {
            sortValue.setText("Không");
            this.sortOption = None;
        }

    }

    private void logSearchFieldInfo() {
        StringBuilder sb = new StringBuilder("SearchField: ");
        if (searchField.getPropertyName() != null) {
            sb.append("Name: ").append(searchField.getPropertyName()).append(", ");
        }
        sb.append("Max guest: ").append(searchField.getMax_guest()).append(", ");
        sb.append("Bedrooms: ").append(searchField.getBed_rooms());

        // Log property IDs từ SearchField nếu có
        if (searchField.getProperty_ids() != null && !searchField.getProperty_ids().isEmpty()) {
            sb.append(", Property IDs từ SearchField: ").append(searchField.getProperty_ids().size());
        }

        if (searchField.getCity_codes() != null && !searchField.getCity_codes().isEmpty()) {
            sb.append(", City codes: ");
            for (int code : searchField.getCity_codes()) {
                sb.append(code).append(" ");
            }
        }

        if (searchField.getDistrict_codes() != null && !searchField.getDistrict_codes().isEmpty()) {
            sb.append(", District codes: ");
            for (int code : searchField.getDistrict_codes()) {
                sb.append(code).append(" ");
            }
        }
        sb.append(searchField.getSortOption());

        Log.d("Search Field", sb.toString());
    }

    private void setupRecyclerView() {
        adapter = new SearchedListAdapter(this, propertyList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadPropertyData() {
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);

        if (propertyIds != null && !propertyIds.isEmpty()) {
            // Giả sử bạn có phương thức để lấy Property theo các ID
            fetchPropertiesByIds(propertyIds);
        }
        else {
            showNoResults("Không có thông tin tìm kiếm");
            progressBar.setVisibility(View.GONE);
        }
    }

    // Phương thức này sẽ được triển khai để gọi API hoặc truy vấn dữ liệu dựa trên các ID
    private void fetchPropertiesByIds(List<String> ids) {
        PropertyRepository propertyRepository = new PropertyRepository(this);


        if(this.sortOption == None) {
            List<Property> properties = new ArrayList<>();
            for (String id : ids) {
                propertyRepository.getPropertyById(id,
                        property -> {
                            properties.add(property);
                            if (properties.size() == ids.size()) {
                                updatePropertyList(properties);
                            }
                        }, e -> {
                            Toast.makeText(this, "Lỗi khi lấy thông tin của property", Toast.LENGTH_SHORT).show();
                        });
            }
        } else if (this.sortOption == Rating_High_To_Low) {
            propertyRepository.getPropertySortedByRating(ids, this::updatePropertyList, e -> {
                Toast.makeText(this, "Lỗi khi lấy theo xếp hạng", Toast.LENGTH_LONG).show();
            });
        } else if(this.sortOption == Price_Low_To_High) {
            propertyRepository.getPropertySortedByPriceAsc(ids, this::updatePropertyList, e -> {
                Toast.makeText(this, "Lỗi khi lấy theo giá tăng dần", Toast.LENGTH_LONG).show();
            });
        } else {
            propertyRepository.getPropertySortedByPriceDesc(ids, this::updatePropertyList, e -> {
                Toast.makeText(this, "Lỗi khi lấy theo giá giảm dần", Toast.LENGTH_LONG).show();
            });
        }
    }

    private void updatePropertyList(List<Property> properties) {
        // Hide loading indicator
        progressBar.setVisibility(View.GONE);

        if (properties == null || properties.isEmpty()) {
            showNoResults("Không tìm thấy kết quả phù hợp");
            return;
        }

        // Ẩn thông báo không có kết quả nếu có
        if (noResultsText != null) {
            noResultsText.setVisibility(View.GONE);
        }

        // Update the adapter with new data
        propertyList.clear();
        propertyList.addAll(properties);
        adapter.notifyDataSetChanged();
    }

    private void showNoResults(String message) {
        if (noResultsText != null) {
            noResultsText.setText(message);
            noResultsText.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onPropertyClick(Property property) {
        // Xử lý khi người dùng nhấp vào một property
        Toast.makeText(this, "Đã chọn: " + property.name, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onFavoriteClick(Property property, int position) {
        // Xử lý khi người dùng nhấp vào nút yêu thích
        Toast.makeText(this, "Đã thêm vào yêu thích: " + property.name, Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}