package com.example.myapplication.ui.fragments.host;

// PropertyManagementFragment.java
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.R;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Repository.Auth.AuthRepository;
import com.example.myapplication.data.Repository.Property.PropertyRepository;
import com.example.myapplication.ui.activities.CreatePropertyActivity;
import com.example.myapplication.ui.activities.HostDetailPropertyActivity;
import com.example.myapplication.ui.adapters.PropertyAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class PropertyManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PropertyAdapter propertyAdapter;
    private List<Property> propertyList;

    // Inject your repository/database class here
    private PropertyRepository propertyRepository;
    private String currentUserId; // Get this from SharedPreferences or Auth

    private Button addButton;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Safe to use context here
        propertyRepository = new PropertyRepository(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        propertyList = new ArrayList<>();
        AuthRepository authRepository = new AuthRepository(getContext());
        currentUserId = authRepository.getUserUid(); // Replace with actual user ID
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_property_management, container, false);
        initViews(view);
        setupRecyclerView();
        loadProperties();
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        addButton = view.findViewById(R.id.addButton);

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreatePropertyActivity.class);
            startActivity(intent);
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadProperties();
        });
    }

    private void setupRecyclerView() {
        propertyAdapter = new PropertyAdapter(propertyList, new PropertyAdapter.OnPropertyActionListener() {
            @Override
            public void onUpdateProperty(Property property) {
                //Use Json to transfer property data
                Toast.makeText(getContext(), "Update Clicked", Toast.LENGTH_SHORT).show();
                Gson gson = new Gson();
                String json = gson.toJson(property);

                Intent intent = new Intent(requireContext(), HostDetailPropertyActivity.class);
                intent.putExtra("property_json", json);

                startActivity(intent);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(propertyAdapter);
    }

    private void loadProperties() {
        swipeRefreshLayout.setRefreshing(true);

        propertyRepository.getPropertyByUserID(currentUserId,
                new OnSuccessListener<List<Property>>() {
                    @Override
                    public void onSuccess(List<Property> properties) {
                        swipeRefreshLayout.setRefreshing(false);
                        propertyList.clear();
                        propertyList.addAll(properties);
                        propertyAdapter.notifyDataSetChanged();
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}
