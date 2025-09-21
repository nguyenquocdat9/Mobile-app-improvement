package com.example.myapplication.data.Model.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.common.internal.Objects;

import java.util.List;

public class District {
    public String name;
    public int code;
    public String codename;
    public String division_type;
    public int province_code;
    public List<Ward> wards;

    public District(String name, int code, String codename, String division_type, int province_code, List<Ward> wards) {
        this.code = code;
        this.name = name;;
        this.codename = codename;
        this.division_type = division_type;
        this.province_code = province_code;
        this.wards = wards;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
