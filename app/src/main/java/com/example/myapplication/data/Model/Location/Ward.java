package com.example.myapplication.data.Model.Location;

import androidx.annotation.NonNull;

public class Ward {
    public String name;
    public int code;
    public String division_type;
    public String codename;
    public int district_code;

    public Ward(String name, int code, String division_type, String codename, int district_code) {
        this.name = name;
        this.code = code;
        this.division_type = division_type;
        this.codename = codename;
        this.district_code = district_code;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
