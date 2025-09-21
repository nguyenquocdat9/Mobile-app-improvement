package com.example.myapplication.ui.misc;

import com.example.myapplication.data.Model.Property.AmenityStatus;

public class Amenity {
    public String name;
    public int iconResId;
    public AmenityStatus status;

    public Amenity(String name, int iconResId, AmenityStatus status) {
        this.name = name;
        this.iconResId = iconResId;
        this.status = status;
    }
}


