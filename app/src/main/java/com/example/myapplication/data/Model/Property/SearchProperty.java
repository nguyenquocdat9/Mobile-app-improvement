package com.example.myapplication.data.Model.Property;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchProperty {
    @SerializedName("objectID")
    private String objectID;

    @SerializedName("propertyName")
    private String propertyName;

    @SerializedName("city_code")
    private int city_code;

    @SerializedName("district_code")
    private int district_code;

    @SerializedName("max_guest")
    private int max_guest;

    @SerializedName("bed_rooms")
    private int bed_rooms;

    @SerializedName("price")
    private double price;

    @SerializedName("bookedDate")
    private List<String> bookedDate;

    @SerializedName("tv")
    private boolean tv;

    @SerializedName("petAllowance")
    private boolean petAllowance;

    @SerializedName("pool")
    private boolean pool;

    @SerializedName("washingMachine")
    private boolean washingMachine;

    @SerializedName("breakfast")
    private boolean breakfast;

    @SerializedName("bbq")
    private boolean bbq;

    @SerializedName("wifi")
    private boolean wifi;

    @SerializedName("airConditioner")
    private boolean airConditioner;

    // Constructor rỗng cần thiết cho Gson
    public SearchProperty() {
    }

    public SearchProperty(String propertyID, String propertyName, int city_code, int district_code,
                          int max_guest, int bed_rooms, double price, List<String> bookedDate, boolean tv,
                          boolean petAllowance, boolean pool, boolean washingMachine, boolean breakfast,
                          boolean bbq, boolean wifi, boolean airConditioner) {
        this.objectID = propertyID;
        this.propertyName = propertyName;
        this.city_code = city_code;
        this.district_code = district_code;
        this.max_guest = max_guest;
        this.bed_rooms = bed_rooms;
        this.price = price;
        this.bookedDate = bookedDate;
        this.tv = tv;
        this.petAllowance = petAllowance;
        this.pool = pool;
        this.washingMachine = washingMachine;
        this.breakfast = breakfast;
        this.bbq = bbq;
        this.wifi = wifi;
        this.airConditioner = airConditioner;
    }

    public SearchProperty(Property property) {
        this.objectID = property.getId();
        this.propertyName = property.getName();
        this.city_code = property.getAddress().getCity_code();
        this.district_code = property.getAddress().getDistrict_code();
        this.max_guest = property.getMax_guess();
        this.price = property.getNormal_price();
        this.bookedDate = property.getBooked_date();
        this.bed_rooms = property.getRooms().bedRooms;
        this.tv = property.getAmenities().getTv() == AmenityStatus.Available;
        this.petAllowance = property.getAmenities().getPetAllowance() == AmenityStatus.Available;
        this.pool = property.getAmenities().getPool() == AmenityStatus.Available;
        this.washingMachine = property.getAmenities().getWashingMachine() == AmenityStatus.Available;
        this.breakfast = property.getAmenities().getBreakfast() == AmenityStatus.Available;
        this.bbq = property.getAmenities().getBbq() == AmenityStatus.Available;
        this.wifi = property.getAmenities().getWifi() == AmenityStatus.Available;
        this.airConditioner = property.getAmenities().getAirConditioner() == AmenityStatus.Available;
    }

    // Getters
    public String getObjectID() {
        return objectID;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public int getCity_code() {
        return city_code;
    }

    public int getDistrict_code() {
        return district_code;
    }

    public int getMax_guest() {
        return max_guest;
    }

    public int getBed_rooms() {
        return bed_rooms;
    }

    public double getPrice() {
        return price;
    }

    public List<String> getBookedDate() {
        return bookedDate;
    }

    public boolean isTv() {
        return tv;
    }

    public boolean isPetAllowance() {
        return petAllowance;
    }

    public boolean isPool() {
        return pool;
    }

    public boolean isWashingMachine() {
        return washingMachine;
    }

    public boolean isBreakfast() {
        return breakfast;
    }

    public boolean isBbq() {
        return bbq;
    }

    public boolean isWifi() {
        return wifi;
    }

    public boolean isAirConditioner() {
        return airConditioner;
    }
}