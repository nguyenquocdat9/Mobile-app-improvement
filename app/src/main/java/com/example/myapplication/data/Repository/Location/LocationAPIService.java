package com.example.myapplication.data.Repository.Location;

import com.example.myapplication.data.Model.Location.District;
import com.example.myapplication.data.Model.Location.Province;
import com.example.myapplication.data.Model.Location.Ward;

import java.util.List;

import retrofit2.Call;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LocationAPIService {
    // Lấy tất cả các tỉnh/thành phố
    @GET("p/")
    Call<List<Province>> getAllProvinces();

    // Lấy thông tin của các quận huyện của một tỉnh/thành phố cụ thể
    @GET("p/{provinceCode}")
    Call<Province> getAllDistrictsInProvince(@Path("provinceCode") int provinceCode, @Query("depth") int depth);

    // Lấy thông tin của các xã phường của một quận/huyện cụ thể
    @GET("d/{districtCode}")
    Call<District> getAllWardsInDistrict(@Path("districtCode") int districtCode, @Query("depth") int depth);

    @GET("p/search/")
    Call<List<Province>> searchProvincesByName(@Query("q") String name);

    // 🔍 Tìm quận theo tên
    @GET("d/search/")
    Call<List<District>> searchDistrictsByName(@Query("q") String name);

    // 🔍 Tìm xã theo tên
    @GET("w/search/")
    Call<List<Ward>> searchWardsByName(@Query("q") String name);
}
