package com.example.myapplication.data.Model.Search;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.myapplication.data.Enum.SortOption;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SearchField implements Parcelable {
    @SerializedName("propertyName")
    private String propertyName;

    @SerializedName("city_codes")
    private List<Integer> city_codes;

    @SerializedName("district_codes")
    private List<Integer> district_codes;

    @SerializedName("property_ids")
    private List<String> property_ids;

    @SerializedName("max_guest")
    private int max_guest;

    @SerializedName("bed_rooms")
    private int bed_rooms;

    @SerializedName("min_price")
    private double min_price;

    @SerializedName("max_price")
    private double max_price;

    @SerializedName("check_in_date")
    private String check_in_date;

    @SerializedName("check_out_date")
    private String check_out_date;

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

    @SerializedName("sort")
    private String sort;

    @SerializedName("page")
    private int page;

    @SerializedName("hitsPerPage")
    private int hitsPerPage;

    // Constructor mặc định
    public SearchField() {
        // Khởi tạo các giá trị mặc định
        this.page = 0;
        this.hitsPerPage = 30;
    }

    // Constructor cho Parcelable
    protected SearchField(Parcel in) {
        propertyName = in.readString();
        city_codes = new ArrayList<>();
        in.readList(city_codes, Integer.class.getClassLoader());
        district_codes = new ArrayList<>();
        in.readList(district_codes, Integer.class.getClassLoader());
        property_ids = new ArrayList<>();
        in.readStringList(property_ids);
        max_guest = in.readInt();
        bed_rooms = in.readInt();
        min_price = in.readDouble();
        max_price = in.readDouble();
        check_in_date = in.readString();
        check_out_date = in.readString();
        tv = in.readByte() != 0;
        petAllowance = in.readByte() != 0;
        pool = in.readByte() != 0;
        washingMachine = in.readByte() != 0;
        breakfast = in.readByte() != 0;
        bbq = in.readByte() != 0;
        wifi = in.readByte() != 0;
        airConditioner = in.readByte() != 0;
        sort = in.readString();
        page = in.readInt();
        hitsPerPage = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(propertyName);
        dest.writeList(city_codes);
        dest.writeList(district_codes);
        dest.writeStringList(property_ids);
        dest.writeInt(max_guest);
        dest.writeInt(bed_rooms);
        dest.writeDouble(min_price);
        dest.writeDouble(max_price);
        dest.writeString(check_in_date);
        dest.writeString(check_out_date);
        dest.writeByte((byte) (tv ? 1 : 0));
        dest.writeByte((byte) (petAllowance ? 1 : 0));
        dest.writeByte((byte) (pool ? 1 : 0));
        dest.writeByte((byte) (washingMachine ? 1 : 0));
        dest.writeByte((byte) (breakfast ? 1 : 0));
        dest.writeByte((byte) (bbq ? 1 : 0));
        dest.writeByte((byte) (wifi ? 1 : 0));
        dest.writeByte((byte) (airConditioner ? 1 : 0));
        dest.writeString(sort);
        dest.writeInt(page);
        dest.writeInt(hitsPerPage);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SearchField> CREATOR = new Creator<SearchField>() {
        @Override
        public SearchField createFromParcel(Parcel in) {
            return new SearchField(in);
        }

        @Override
        public SearchField[] newArray(int size) {
            return new SearchField[size];
        }
    };

    // Getters and Setters
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public List<Integer> getCity_codes() {
        return city_codes;
    }

    public void setCity_codes(List<Integer> city_codes) {
        this.city_codes = city_codes;
    }

    public List<Integer> getDistrict_codes() {
        return district_codes;
    }

    public void setDistrict_codes(List<Integer> district_codes) {
        this.district_codes = district_codes;
    }

    public List<String> getProperty_ids() {
        return property_ids;
    }

    public void setProperty_ids(List<String> property_ids) {
        this.property_ids = property_ids;
    }

    public int getMax_guest() {
        return max_guest;
    }

    public void setMax_guest(int max_guest) {
        this.max_guest = max_guest;
    }

    public int getBed_rooms() {
        return bed_rooms;
    }

    public void setBed_rooms(int bed_rooms) {
        this.bed_rooms = bed_rooms;
    }

    public double getMin_price() {
        return min_price;
    }

    public void setMin_price(double min_price) {
        this.min_price = min_price;
    }

    public double getMax_price() {
        return max_price;
    }

    public void setMax_price(double max_price) {
        this.max_price = max_price;
    }

    public String getCheck_in_date() {
        return check_in_date;
    }

    public void setCheck_in_date(String check_in_date) {
        this.check_in_date = check_in_date;
    }

    public String getCheck_out_date() {
        return check_out_date;
    }

    public void setCheck_out_date(String check_out_date) {
        this.check_out_date = check_out_date;
    }

    public boolean isTv() {
        return tv;
    }

    public void setTv(boolean tv) {
        this.tv = tv;
    }

    public boolean isPetAllowance() {
        return petAllowance;
    }

    public void setPetAllowance(boolean petAllowance) {
        this.petAllowance = petAllowance;
    }

    public boolean isPool() {
        return pool;
    }

    public void setPool(boolean pool) {
        this.pool = pool;
    }

    public boolean isWashingMachine() {
        return washingMachine;
    }

    public void setWashingMachine(boolean washingMachine) {
        this.washingMachine = washingMachine;
    }

    public boolean isBreakfast() {
        return breakfast;
    }

    public void setBreakfast(boolean breakfast) {
        this.breakfast = breakfast;
    }

    public boolean isBbq() {
        return bbq;
    }

    public void setBbq(boolean bbq) {
        this.bbq = bbq;
    }

    public boolean isWifi() {
        return wifi;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public boolean isAirConditioner() {
        return airConditioner;
    }

    public void setAirConditioner(boolean airConditioner) {
        this.airConditioner = airConditioner;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getHitsPerPage() {
        return hitsPerPage;
    }

    public void setHitsPerPage(int hitsPerPage) {
        this.hitsPerPage = hitsPerPage;
    }

    public void setSortOption(String sort) {
        this.sort = sort;
    }

    public String getSortOption() {
        return this.sort;
    }

    // Builder pattern để dễ dàng tạo đối tượng tìm kiếm
    public static class Builder {
        private SearchField searchField;

        public Builder() {
            searchField = new SearchField();
        }

        public Builder propertyName(String propertyName) {
            searchField.setPropertyName(propertyName);
            return this;
        }

        public Builder cityCode(List<Integer> cityCode) {
            searchField.setCity_codes(cityCode);
            return this;
        }

        public Builder districtCode(List<Integer> districtCode) {
            searchField.setDistrict_codes(districtCode);
            return this;
        }

        public Builder propertyIds(List<String> propertyIds) {
            searchField.setProperty_ids(propertyIds);
            return this;
        }

        public Builder maxGuest(int maxGuest) {
            searchField.setMax_guest(maxGuest);
            return this;
        }

        public Builder bedRooms(int bedRooms) {
            searchField.setBed_rooms(bedRooms);
            return this;
        }

        public Builder priceRange(double minPrice, double maxPrice) {
            searchField.setMin_price(minPrice);
            searchField.setMax_price(maxPrice);
            return this;
        }

        public Builder dateRange(String checkInDate, String checkOutDate) {
            searchField.setCheck_in_date(checkInDate);
            searchField.setCheck_out_date(checkOutDate);
            return this;
        }

        public Builder tv(boolean hasTv) {
            searchField.setTv(hasTv);
            return this;
        }

        public Builder petAllowance(boolean allowsPets) {
            searchField.setPetAllowance(allowsPets);
            return this;
        }

        public Builder pool(boolean hasPool) {
            searchField.setPool(hasPool);
            return this;
        }

        public Builder washingMachine(boolean hasWashingMachine) {
            searchField.setWashingMachine(hasWashingMachine);
            return this;
        }

        public Builder breakfast(boolean hasBreakfast) {
            searchField.setBreakfast(hasBreakfast);
            return this;
        }

        public Builder bbq(boolean hasBbq) {
            searchField.setBbq(hasBbq);
            return this;
        }

        public Builder wifi(boolean hasWifi) {
            searchField.setWifi(hasWifi);
            return this;
        }

        public Builder airConditioner(boolean hasAirConditioner) {
            searchField.setAirConditioner(hasAirConditioner);
            return this;
        }

        public Builder sortOptions(String sort) {
            searchField.setSortOption(sort);
            return this;
        }

        public Builder pagination(int page, int hitsPerPage) {
            searchField.setPage(page);
            searchField.setHitsPerPage(hitsPerPage);
            return this;
        }

        public SearchField build() {
            return searchField;
        }
    }
}