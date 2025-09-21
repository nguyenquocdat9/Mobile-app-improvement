package com.example.myapplication.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.Model.Property.Amenities;
import com.example.myapplication.data.Model.Property.AmenityStatus;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Model.Property.Rooms;
import com.example.myapplication.interfaces.IStepValidator;
import com.example.myapplication.ui.adapters.AmenitySetupAdapter;
import com.example.myapplication.ui.customviews.NumberSelectorView;
import com.example.myapplication.ui.misc.Amenity;
import com.example.myapplication.ui.misc.PropertyViewModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SetAmenitiesFragment extends Fragment implements IStepValidator {
    private PropertyViewModel viewModel;

    NumberSelectorView numBedroom;
    NumberSelectorView numLivingroom;
    NumberSelectorView numKitchen;
    NumberSelectorView maxGuess;

    RecyclerView amenityRecycler;
    AmenitySetupAdapter adapter;

    EditText moreInfoEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_amentities, container, false);

        numBedroom = view.findViewById(R.id.numBedroom);
        numLivingroom = view.findViewById(R.id.numLivingroom);
        numKitchen = view.findViewById(R.id.numKitchen);
        maxGuess = view.findViewById(R.id.maxGuess);

        //Amenity setup
        amenityRecycler = view.findViewById(R.id.amenityRecycler);
        amenityRecycler.setLayoutManager(new LinearLayoutManager(this.getContext()));

        List<Amenity> amenityList = Arrays.asList(
                new Amenity("TV", R.drawable.ic_tv, AmenityStatus.Hidden),
                new Amenity("Wi-Fi", R.drawable.ic_wifi, AmenityStatus.Hidden),
                new Amenity("Thú cưng", R.drawable.ic_pets, AmenityStatus.Hidden),
                new Amenity("Hồ bơi", R.drawable.ic_pool, AmenityStatus.Hidden),
                new Amenity("Máy giặt", R.drawable.ic_bed, AmenityStatus.Hidden),
                new Amenity("Bữa sáng", R.drawable.ic_free_breakfast, AmenityStatus.Hidden),
                new Amenity("Máy lạnh", R.drawable.ic_airconditioner, AmenityStatus.Hidden),
                new Amenity("BBQ", R.drawable.ic_outdoor_grill, AmenityStatus.Hidden)
        );

        adapter = new AmenitySetupAdapter(amenityList);
        amenityRecycler.setAdapter(adapter);

        moreInfoEditText = view.findViewById(R.id.moreInfo);

        viewModel = new ViewModelProvider(requireActivity()).get(PropertyViewModel.class);

        applyData();
        return view;
    }

    @Override
    public void applyData() {
        Property property = viewModel.getPropertyData().getValue();
        //room apply
        if (property.getRooms() == null) property.rooms = new Rooms(0, 0, 0);

        numBedroom.setCount(property.getRooms().bedRooms);
        numLivingroom.setCount(property.getRooms().livingRooms);
        numKitchen.setCount(property.getRooms().kitchen);
        maxGuess.setCount(property.getMax_guess());
        //need count for living room and kitchen

        //amenities

        if (property.getAmenities() != null) {
            Amenities am = property.getAmenities();
            List<Amenity> amenityList = Arrays.asList(
                    new Amenity("TV", R.drawable.ic_tv, am.tv),
                    new Amenity("Wi-Fi", R.drawable.ic_wifi, am.wifi),
                    new Amenity("Thú cưng", R.drawable.ic_pets, am.petAllowance),
                    new Amenity("Hồ bơi", R.drawable.ic_pool, am.pool),
                    new Amenity("Máy giặt", R.drawable.ic_bed, am.washingMachine),
                    new Amenity("Bữa sáng", R.drawable.ic_free_breakfast, am.breakfast),
                    new Amenity("Máy lạnh", R.drawable.ic_airconditioner, am.airConditioner),
                    new Amenity("BBQ", R.drawable.ic_outdoor_grill, am.bbq)
            );

            moreInfoEditText.setText(am.more);

            adapter = new AmenitySetupAdapter(amenityList);
            amenityRecycler.setAdapter(adapter);
        }

    }

    @Override
    public void save() {
        Property newValue = viewModel.getPropertyData().getValue();
        if(newValue == null) newValue = new Property();

        newValue.rooms = new Rooms(numBedroom.getCount(), numLivingroom.getCount(), numKitchen.getCount());

        newValue.max_guess = maxGuess.getCount();

        //amenities
        List<Amenity> amList = adapter.GetAmenities();
        Amenities am = new Amenities(
                amList.get(0).status,
                amList.get(1).status,
                amList.get(2).status,
                amList.get(3).status,
                amList.get(4).status,
                amList.get(5).status,
                amList.get(6).status,
                amList.get(7).status,
                moreInfoEditText.getText().toString(),
                "rule"
        );
        if (newValue.amenities != null) am.houseRules = newValue.amenities.houseRules;
        newValue.amenities = am;

        viewModel.setPropertyData(newValue);
    }

    @Override
    public boolean validate(String warning) {
        return true;
    }

    @Override
    public int getStepIndex() {return 2;}
}
