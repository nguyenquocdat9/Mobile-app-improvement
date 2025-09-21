package com.example.myapplication.data.Model.Review;

import java.util.UUID;

public class Review {
    public String id;
    public String booking_id;
    public int point;
    public String property_id;
    public String content;

    public Review() {};

    public Review(String booking_id, String property_id, int point, String content) {
        this.id = UUID.randomUUID().toString();
        this.point = point;
        this.content = content;
        this.property_id = property_id;
        this.booking_id = booking_id;
    }

}
