package com.example.myapplication.data.Repository.Search;

import com.example.myapplication.data.Model.Property.SearchProperty;
import com.example.myapplication.data.Model.Search.BookedDateRequest;
import com.example.myapplication.data.Model.Search.SearchField;
import com.example.myapplication.data.Model.Search.SearchResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PropertyAPIService {
    // Thêm property vào Algolia
    @POST("api/properties")
    Call<SearchResponse> addPropertyToAlgolia(@Body SearchProperty property);

    // Lấy thông tin property theo ID
    @GET("api/properties/{id}")
    Call<SearchResponse> getPropertyById(@Path("id") String propertyId);

    // Xóa property theo ID
    @DELETE("api/properties/{id}")
    Call<SearchResponse> deletePropertyById(@Path("id") String propertyId);

    // Tìm kiếm property theo các tiêu chí
    @POST("api/properties/search")
    Call<SearchResponse> searchProperties(@Body SearchField searchField);

    @PATCH("/api/properties/{id}/add-booked-dates")
    Call<SearchResponse> updateBookedDateByPropertyID(@Path("id") String propertyId, @Body BookedDateRequest bookedDatesRequest );

    @PATCH("/api/properties/{id}/remove-booked-dates")
    Call<SearchResponse> removeBookedDateByPropertyID(@Path("id") String propertyId, @Body BookedDateRequest bookedDatesRequest );

}