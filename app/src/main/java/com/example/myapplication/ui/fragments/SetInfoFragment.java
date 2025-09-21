package com.example.myapplication.ui.fragments;

import android.os.Bundle;
import android.provider.Telephony;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.data.Model.Location.District;
import com.example.myapplication.data.Model.Location.Province;
import com.example.myapplication.data.Model.Location.Ward;
import com.example.myapplication.data.Model.Property.Address;
import com.example.myapplication.data.Model.Property.Amenities;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Repository.Location.LocationAPIClient;
import com.example.myapplication.data.Repository.Location.LocationAPIService;
import com.example.myapplication.interfaces.IStepValidator;
import com.example.myapplication.ui.misc.PropertyViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SetInfoFragment extends Fragment implements IStepValidator {

    private LocationAPIClient locationApi;
    private AutoCompleteTextView actProvince, actDistrict, actWard;
    private TextInputLayout actProvinceLayout, actDistrictLayout, actWardLayout;
    private EditText detailAddress;
    private EditText houseRule;
    private TextInputEditText nameEditText;
    private Province selectedProvince = null;
    private District selectedDistrict = null;
    private Ward selectedWard = null;


    private PropertyViewModel viewModel;

    @Override
    public void applyData() {
        Property value = viewModel.getPropertyData().getValue();

        if (value.getAddress() != null) {
            Address addr = value.getAddress();

            locationApi.getAllDistrictsInProvince(addr.city_code, new DistrictCallbackHandler());
            locationApi.getAllWardsInDistrict(addr.district_code, new WardsCallbackHandler());

            actProvince.setText(addr.city_name, false);
            actDistrict.setText(addr.district_name, false);
            actWard.setText(addr.ward_name, false);

            selectedProvince = new Province(addr.city_name, addr.city_code, null, null, -1, null);
            selectedDistrict = new District(addr.district_name, addr.district_code, null, null, -1, null);
            selectedWard = new Ward(addr.ward_name, addr.ward_code, null, null, -1);

            actDistrict.setEnabled(true);
            actWard.setEnabled(true);

            detailAddress.setText(addr.detailed_address);
        }

        if (value.name != null) {
            nameEditText.setText(value.name);
        }

        if (value.amenities != null) {
            houseRule.setText(value.amenities.houseRules);
        }
    }

    @Override
    public void save() {
        Property newValue = viewModel.getPropertyData().getValue();
        if(newValue == null) newValue = new Property();

        newValue.name = nameEditText.getText().toString();

        if (newValue.amenities == null) newValue.amenities = new Amenities();
        newValue.amenities.houseRules = houseRule.getText().toString();

        if (isValidAddress()) {
            newValue.address = new Address(selectedProvince.code, selectedDistrict.code, selectedWard.code,
                    selectedProvince.name, selectedDistrict.name, selectedWard.name,
                    detailAddress.getText().toString());
        } else {
            Toast.makeText(getContext(), "Save Address failed", Toast.LENGTH_SHORT).show();
        }

        viewModel.setPropertyData(newValue);
    }

    @Override
    public boolean validate(String warning) {
//        if (!isValidAddress()) {
//            warning = new String("Địa chỉ không hợp lệ");
//            return false;
//        }
        return true;
    }

    @Override
    public int getStepIndex() {return 1;}

    class ProvincesCallbackHandler implements LocationAPIClient.OnProvinceListCallback {
        @Override
        public void onSuccess(List<Province> provinces) {
            //nap province adapter
            ArrayAdapter<Province> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_dropdown_item_1line, provinces);

            actProvince.setAdapter(adapter);
            actProvinceLayout.setEnabled(true);
        }

        @Override
        public void onError(String errorMessage) {
            actProvince.setAdapter(null);
            actProvinceLayout.setEnabled(false);
        }
    }

    class DistrictCallbackHandler implements LocationAPIClient.OnDistrictListCallback {
        @Override
        public void onSuccess(List<District> districts) {
            //nap province adapter
            ArrayAdapter<District> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_dropdown_item_1line, districts);

            actDistrict.setAdapter(adapter);
            actDistrictLayout.setEnabled(true);
        }

        @Override
        public void onError(String errorMessage) {
            actDistrict.setAdapter(null);
            actDistrictLayout.setEnabled(false);
        }
    }

    class WardsCallbackHandler implements LocationAPIClient.OnWardListCallback {
        @Override
        public void onSuccess(List<Ward> wards) {
            //nap province adapter
            ArrayAdapter<Ward> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_dropdown_item_1line, wards);

            actWard.setAdapter(adapter);
            actWardLayout.setEnabled(true);
        }

        @Override
        public void onError(String errorMessage) {
            actWard.setAdapter(null);
            actWardLayout.setEnabled(false);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_set_info, container, false);

        actProvince = view.findViewById(R.id.actProvince);
        actDistrict = view.findViewById(R.id.actDistrict);
        actWard = view.findViewById(R.id.actWard);

        actProvinceLayout = view.findViewById(R.id.ProvinceLayout);
        actDistrictLayout = view.findViewById(R.id.DistrictLayout);
        actWardLayout = view.findViewById(R.id.WardLayout);

        detailAddress = view.findViewById(R.id.detailAddress);
        nameEditText = view.findViewById(R.id.nameEditText);
        houseRule = view.findViewById(R.id.houseRule);

        locationApi = new LocationAPIClient();

        actDistrictLayout.setEnabled(false);
        actWardLayout.setEnabled(false);

        locationApi.getAllProvinces(new ProvincesCallbackHandler());
        setupProvince();

        viewModel = new ViewModelProvider(requireActivity()).get(PropertyViewModel.class);

        applyData();
        return view;
    }

    private void setupProvince() {

        actProvince.setOnItemClickListener((parent, view, pos, id) -> {
            selectedProvince = (Province) actProvince.getAdapter().getItem(pos);
            selectedDistrict = null;
            selectedWard = null;
            actDistrict.setText("");
            actWard.setText("");
            actDistrictLayout.setEnabled(false);
            actWardLayout.setEnabled(false);

            locationApi.getAllDistrictsInProvince(selectedProvince.code, new DistrictCallbackHandler());
        });

        actDistrict.setOnItemClickListener((parent, view, pos, id) -> {
            selectedDistrict = (District) actDistrict.getAdapter().getItem(pos);
            selectedWard = null;
            actWard.setText("");
            actWardLayout.setEnabled(false);

            locationApi.getAllWardsInDistrict(selectedDistrict.code, new WardsCallbackHandler());
        });

        actWard.setOnItemClickListener((parent, view, pos, id) -> {
            selectedWard = (Ward) actWard.getAdapter().getItem(pos);
        });
    }

    public boolean isValidAddress() {
        return selectedProvince != null && selectedDistrict != null && selectedWard != null && !detailAddress.getText().toString().equals(new String(""));
    }

    public int getProvinceCode() { return selectedProvince != null ? selectedProvince.code : null; }
    public int getDistrictCode() { return selectedDistrict != null ? selectedDistrict.code : null; }
    public int getWardCode() { return selectedWard != null ? selectedWard.code : null; }
}
