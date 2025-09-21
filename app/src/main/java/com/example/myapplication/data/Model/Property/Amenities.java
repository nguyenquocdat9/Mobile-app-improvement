package com.example.myapplication.data.Model.Property;

import android.os.Parcel;
import android.os.Parcelable;

public class Amenities implements Parcelable {
    public AmenityStatus tv;
    public AmenityStatus wifi;
    public AmenityStatus petAllowance;
    public AmenityStatus pool;
    public AmenityStatus washingMachine;
    public AmenityStatus breakfast;
    public AmenityStatus airConditioner;
    public AmenityStatus bbq;
    public String more;
    public String houseRules;

    public Amenities() {}

    public Amenities(AmenityStatus tv, AmenityStatus wifi, AmenityStatus petAllowance, AmenityStatus pool, AmenityStatus washingMachine,
                     AmenityStatus breakfast, AmenityStatus airConditioner, AmenityStatus bbq, String more, String houseRules) {
        this.tv = tv;
        this.wifi = wifi;
        this.petAllowance = petAllowance;
        this.pool = pool;
        this.washingMachine = washingMachine;
        this.breakfast = breakfast;
        this.airConditioner = airConditioner;
        this.bbq = bbq;
        this.more = more;
        this.houseRules = houseRules;
    }

    protected Amenities(Parcel in) {
        tv = AmenityStatus.valueOf(in.readString());
        wifi = AmenityStatus.valueOf(in.readString());
        petAllowance = AmenityStatus.valueOf(in.readString());
        pool = AmenityStatus.valueOf(in.readString());
        washingMachine = AmenityStatus.valueOf(in.readString());
        breakfast = AmenityStatus.valueOf(in.readString());
        airConditioner = AmenityStatus.valueOf(in.readString());
        bbq = AmenityStatus.valueOf(in.readString());
        more = in.readString();
        houseRules = in.readString();
    }

    public static final Creator<Amenities> CREATOR = new Creator<Amenities>() {
        @Override
        public Amenities createFromParcel(Parcel in) {
            return new Amenities(in);
        }

        @Override
        public Amenities[] newArray(int size) {
            return new Amenities[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tv.name());
        dest.writeString(wifi.name());
        dest.writeString(petAllowance.name());
        dest.writeString(pool.name());
        dest.writeString(washingMachine.name());
        dest.writeString(breakfast.name());
        dest.writeString(airConditioner.name());
        dest.writeString(bbq.name());
        dest.writeString(more);
        dest.writeString(houseRules);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters and Setters

    public AmenityStatus getTv() {
        return tv;
    }

    public void setTv(AmenityStatus tv) {
        this.tv = tv;
    }

    public AmenityStatus getWifi() {
        return wifi;
    }

    public void setWifi(AmenityStatus wifi) {
        this.wifi = wifi;
    }

    public AmenityStatus getPetAllowance() {
        return petAllowance;
    }

    public void setPetAllowance(AmenityStatus petAllowance) {
        this.petAllowance = petAllowance;
    }

    public AmenityStatus getPool() {
        return pool;
    }

    public void setPool(AmenityStatus pool) {
        this.pool = pool;
    }

    public AmenityStatus getWashingMachine() {
        return washingMachine;
    }

    public void setWashingMachine(AmenityStatus washingMachine) {
        this.washingMachine = washingMachine;
    }

    public AmenityStatus getBreakfast() {
        return breakfast;
    }

    public void setBreakfast(AmenityStatus breakfast) {
        this.breakfast = breakfast;
    }

    public AmenityStatus getAirConditioner() {
        return airConditioner;
    }

    public void setAirConditioner(AmenityStatus airConditioner) {
        this.airConditioner = airConditioner;
    }

    public AmenityStatus getBbq() {
        return bbq;
    }

    public void setBbq(AmenityStatus bbq) {
        this.bbq = bbq;
    }

    public String getMore() {
        return more;
    }

    public void setMore(String more) {
        this.more = more;
    }

    public String getHouseRules() {
        return houseRules;
    }

    public void setHouseRules(String houseRules) {
        this.houseRules = houseRules;
    }
}
