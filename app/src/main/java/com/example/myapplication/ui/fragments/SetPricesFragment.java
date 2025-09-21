package com.example.myapplication.ui.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.interfaces.IStepValidator;
import com.example.myapplication.ui.misc.MoneyTextWatcher;
import com.example.myapplication.ui.misc.PropertyViewModel;

public class SetPricesFragment extends Fragment implements IStepValidator {

    private PropertyViewModel viewModel;

    private EditText normalPrice;
    private EditText weekendPrice;
    private EditText holidayPrice;
    private EditText deposit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_prices, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(PropertyViewModel.class);

        normalPrice = view.findViewById(R.id.normalPrice);
        weekendPrice = view.findViewById(R.id.weekendPrice);
        holidayPrice = view.findViewById(R.id.holidayPrice);
        deposit = view.findViewById(R.id.deposit);

        normalPrice.addTextChangedListener(new MoneyTextWatcher(normalPrice));
        weekendPrice.addTextChangedListener(new MoneyTextWatcher(weekendPrice));
        holidayPrice.addTextChangedListener(new MoneyTextWatcher(holidayPrice));
        deposit.addTextChangedListener(new MoneyTextWatcher(deposit));

        applyData();
        return view;
    }

    @Override
    public void applyData() {
        Property property = viewModel.getPropertyData().getValue();

        if (property != null) {
            normalPrice.setText(property.getNormal_price() == 0 ? "" : String.valueOf(property.getNormal_price()));
            Log.d("NORMAL PRICE APPLY" , "  " + property.getNormal_price());
            weekendPrice.setText(property.getWeekend_price() == 0 ? "" : String.valueOf(property.getWeekend_price()));
            holidayPrice.setText(property.getHoliday_price() == 0 ? "" : String.valueOf(property.getHoliday_price()));
            deposit.setText(property.getDeposit() == 0 ? "" : String.valueOf(property.getDeposit()));
        }
    }

    @Override
    public void save() {
        Property newValue = viewModel.getPropertyData().getValue();
        if (newValue == null) newValue = new Property();

        FloatWrapper val = new FloatWrapper();
        if (isValidMoney(normalPrice, val)) {
            newValue.normal_price = val.value;
            Log.d("NORMAL PRICE", "  " + val.value);
        }

        if (isValidMoney(weekendPrice, val))
            newValue.weekend_price = val.value;
        if (isValidMoney(holidayPrice, val))
            newValue.holiday_price = val.value;
        if (isValidMoney(deposit, val))
            newValue.deposit = val.value;
    }

    @Override
    public boolean validate(String warning) {
        return isValidMoney(normalPrice, null) &&
                isValidMoney(weekendPrice, null) &&
                isValidMoney(holidayPrice, null) &&
                isValidMoney(deposit, null);
    }

    class FloatWrapper {
        public float value;
    }

    private boolean isValidMoney(EditText editText, FloatWrapper res) {
        String value = editText.getText().toString().trim();

        if (value.isEmpty()) {
            return false;
        }

        value = value.replaceAll("[,\\s]", "");

        try {
            float a = Float.parseFloat(value);
            if (res != null) res.value = a;
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public int getStepIndex() {
        return 5;
    }
}
