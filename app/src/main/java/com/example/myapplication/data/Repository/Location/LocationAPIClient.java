package com.example.myapplication.data.Repository.Location;


import com.example.myapplication.data.Model.Location.District;
import com.example.myapplication.data.Model.Location.Province;
import com.example.myapplication.data.Model.Location.Ward;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationAPIClient {
    private static final String BASE_URL = "https://provinces.open-api.vn/api/";
    private LocationAPIService apiService;

    public interface OnProvinceListCallback {
        void onSuccess(List<Province> provinces);
        void onError(String errorMessage);
    }

    public interface OnDistrictListCallback {
        void onSuccess(List<District> districts);
        void onError(String errorMessage);
    }

    public interface OnWardListCallback {
        void onSuccess(List<Ward> wards);
        void onError(String errorMessage);
    }

    public LocationAPIClient() {
            Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
            apiService = retrofit.create(LocationAPIService.class);
    }

    public void getAllProvinces(final OnProvinceListCallback callback) {
        apiService.getAllProvinces().enqueue(new Callback<List<Province>>() {
            @Override
            public void onResponse(Call<List<Province>> call, Response<List<Province>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch provinces: " +  response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Province>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void getAllDistrictsInProvince(int provinceCode, final OnDistrictListCallback callback) {
        apiService.getAllDistrictsInProvince(provinceCode, 2).enqueue(new Callback<Province>() {
            @Override
            public void onResponse(Call<Province> call, Response<Province> response) {
                if(response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().districts);
                } else {
                    callback.onError("Failed to fetch provinces: " +  response.message());
                }
            }

            @Override
            public void onFailure(Call<Province> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void getAllWardsInDistrict(int districtCode, final OnWardListCallback callback) {
        apiService.getAllWardsInDistrict(districtCode, 2).enqueue(new Callback<District>() {
            @Override
            public void onResponse(Call<District> call, Response<District> response) {
                if(response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().wards);
                } else {
                    callback.onError("Failed to fetch provinces: " +  response.message());
                }
            }

            @Override
            public void onFailure(Call<District> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void searchProvinceByName(String name, final OnProvinceListCallback callback) {
        apiService.searchProvincesByName(name).enqueue(new Callback<List<Province>>() {
            @Override
            public void onResponse(Call<List<Province>> call, Response<List<Province>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không tìm thấy tỉnh phù hợp");
                }
            }

            @Override
            public void onFailure(Call<List<Province>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void searchDistrictByName(String name, final OnDistrictListCallback callback) {
        apiService.searchDistrictsByName(name).enqueue(new Callback<List<District>>() {
            @Override
            public void onResponse(Call<List<District>> call, Response<List<District>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không tìm thấy quận/huyện phù hợp");
                }
            }

            @Override
            public void onFailure(Call<List<District>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void searchWardByName(String name, final OnWardListCallback callback) {
        apiService.searchWardsByName(name).enqueue(new Callback<List<Ward>>() {
            @Override
            public void onResponse(Call<List<Ward>> call, Response<List<Ward>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không tìm thấy xã/phường phù hợp");
                }
            }

            @Override
            public void onFailure(Call<List<Ward>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

}
