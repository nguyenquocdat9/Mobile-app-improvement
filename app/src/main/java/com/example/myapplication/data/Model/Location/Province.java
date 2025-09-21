package com.example.myapplication.data.Model.Location;

import androidx.annotation.NonNull;

import java.util.List;

public class Province {
    public String name;
    public int code;
    public String codename;
    public String division_type;
    public int phone_code;
    public List<District> districts;

    public Province(String name, int code, String codename, String division_type, int phone_code, List<District> districts) {
        this.name = name;
        this.code = code;
        this.codename = codename;
        this.division_type = division_type;
        this.phone_code = phone_code;
        this.districts = districts;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
