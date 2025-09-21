package com.example.myapplication.ui.misc;

import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.data.Enum.PropertyType;
import com.example.myapplication.data.Model.Property.Property;

public class PropertyViewModel extends ViewModel {
    private final MutableLiveData<Property> propertyData = new MutableLiveData<>(new Property());

    public LiveData<Property> getPropertyData() {
        return propertyData;
    }

    public void setPropertyData(Property data) {
        propertyData.setValue(data);
    }
}
