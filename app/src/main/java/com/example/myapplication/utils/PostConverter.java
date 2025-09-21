// Tạo mới file: PostConverter.java
package com.example.myapplication.utils;

import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.ui.misc.Post;

import java.util.ArrayList;
import java.util.List;

public class PostConverter {
    public static Post convertPropertyToPost(Property property) {
        String normal_Price = "₫" + String.format("%,.0f", property.getNormal_price()) + " cho 1 đêm";
        String weekend_Price = "₫" + String.format("%,.0f", property.getWeekend_price()) + " cho 1 đêm";
        String holiday_Price = "₫" + String.format("%,.0f", property.getHoliday_price()) + " cho 1 đêm";

        // Handle null address case
        String title = property.address.getDetailAddress() != null ?
                property.address.getDetailAddress() : "No location";

        String propertyType = property.property_type.toString();
        int maxGuest = property.max_guess;
        int bedRooms = property.rooms.bedRooms;


        int livingRoomStatus = property.rooms.livingRooms;
        int kitchenStatus = property.rooms.kitchen;

        // Nếu có phòng khách, chỉ ghi "· living room"
        String livingRoomText = "";
        if (livingRoomStatus > 0) {
            livingRoomText = " . " + livingRoomStatus + " phòng khách";
        } else {
            livingRoomText = "";
        }

        // Nếu có phòng khách, chỉ ghi "· living room"
        String kitchenText = "";
        if (kitchenStatus > 0) {
            kitchenText = " · " + kitchenStatus + " phòng bếp";
        } else {
            kitchenText = "";
        }
        // Ghép chuỗi mô tả chi tiết
        String detail = propertyType + " · " + maxGuest + " khách" + " · " + bedRooms + " phòng ngủ" + livingRoomText + kitchenText;

        return new Post(
                property.id,
                property.getHost_id(),
                title,                    // title
                property.getName(),
                property.getMainPhoto(),               // placeholder image
                property.name,                        // address string
                property.address.getFullAddress(),
                detail,// property type as detail
                "1.200 km",                          // no distance available
                "Available now",                 // placeholder date range
                normal_Price,
                property.total_reviews,
                property.avg_ratings,
                property.amenities,
                property.sub_photos
        );
    }

    public static List<Post> convertPropertiesToPosts(List<Property> properties) {
        List<Post> posts = new ArrayList<>();
        for (Property property : properties) {
            posts.add(convertPropertyToPost(property));
        }
        return posts;
    }

}

