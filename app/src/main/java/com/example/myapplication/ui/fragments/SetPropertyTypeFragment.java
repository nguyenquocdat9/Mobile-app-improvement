package com.example.myapplication.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.data.Enum.PropertyType;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.interfaces.IStepValidator;
import com.example.myapplication.ui.adapters.RoomTypeAdapter;
import com.example.myapplication.ui.misc.PropertyViewModel;

import java.util.Arrays;
import java.util.List;

public class SetPropertyTypeFragment extends Fragment implements IStepValidator {

    private RecyclerView recyclerView;
    private RoomTypeAdapter propertyAdapter;
    private List<PropertyType> propertyList;

    private PropertyType selecting = null;

    private PropertyViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_set_property_type, container, false);

        // Initialize the property list and add some data
        propertyList = Arrays.asList(PropertyType.values());

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize and set adapter
        propertyAdapter = new RoomTypeAdapter(propertyList, new RoomTypeAdapter.OnRoomTypeSelectedListener() {
            @Override
            public void onRoomTypeSelected(PropertyType propertyType) {
                selecting = propertyType;
            }
        });

        recyclerView.setAdapter(propertyAdapter);

        viewModel = new ViewModelProvider(requireActivity()).get(PropertyViewModel.class);

        applyData();
        return view;
    }

    @Override
    public void applyData() {
        Property property = viewModel.getPropertyData().getValue();

        if(property.getProperty_type() != null) {
            recyclerView.scrollToPosition(property.getProperty_type().ordinal());
            recyclerView.post(() -> {
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(property.getProperty_type().ordinal());

                //Toast.makeText(getContext(), "VALUE PROPERTY NOT NULL " + property.property_type.ordinal() + " " + (holder == null), Toast.LENGTH_SHORT).show();
                if (holder != null) {
                    // làm việc với holder
                    RoomTypeAdapter.RoomTypeViewHolder viewHolder = (RoomTypeAdapter.RoomTypeViewHolder)  holder;
                    viewHolder.radioButton.performClick();

                }
            });


        }
    }

    @Override
    public void save() {
        Property newValue = viewModel.getPropertyData().getValue();
        if (newValue == null) newValue = new Property();
        newValue.property_type = selecting;

        viewModel.setPropertyData(newValue);
    }

    @Override
    public boolean validate(String warning) {
        if (selecting == null){
            warning = new String("Chọn loại hình cho thuê của bạn");
            return false;
        }
        else return true;
    }

    @Override
    public int getStepIndex() {return 0;}
}