package com.example.myapplication.data.Model.Property;

public class Address {
    public int city_code;
    public int district_code;
    public int ward_code;
    public String city_name;
    public String district_name;
    public String ward_name;
    public String detailed_address;

    public Address() {}

    public Address(int city, int district, int ward, String detailed_add) {
        this.city_code = city;
        this.district_code = district;
        this.ward_code = ward;
        this.detailed_address = detailed_add;
        this.district_name = "";
        this.city_name = "";
        this.ward_name = "";
    }

    public Address(int city_code, int district_code, int ward_code, String city_name, String district_name, String ward_name, String detailed_add) {
        this.city_code = city_code;
        this.district_code = district_code;
        this.ward_code = ward_code;
        this.city_name = city_name;
        this.district_name = district_name;
        this.ward_name = ward_name;
        this.detailed_address = detailed_add;
    }

    public int getCity_code() {
        return city_code;
    }

    public void setCity_code(int city_code) {
        this.city_code = city_code;
    }

    public int getDistrict_code() {
        return district_code;
    }

    public void setDistrict_code(int district_code) {
        this.district_code = district_code;
    }

    public int getWard_code() {
        return ward_code;
    }

    public void setWard_code(int ward_code) {
        this.ward_code = ward_code;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public String getDistrict_name() {
        return district_name;
    }

    public void setDistrict_name(String district_name) {
        this.district_name = district_name;
    }

    public String getWard_name() {
        return ward_name;
    }

    public void setWard_name(String ward_name) {
        this.ward_name = ward_name;
    }

    public String getDetailed_address() {
        return detailed_address;
    }

    public void setDetailed_address(String detailed_address) {
        this.detailed_address = detailed_address;
    }

    public String getDetailAddress() {
        return detailed_address;
    }

    public String getFullAddress() {
        return detailed_address + ", " + ward_name + ", " + district_name + ", " + city_name;
    }

    // lưu địa chỉ city district và ward theo dạng code nhé để dễ search
    // https://provinces.open-api.vn/
}
