package com.example.myapplication.data.Model.Booking;

import com.example.myapplication.data.Enum.Booking_status;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class Booking implements Serializable {
    public String id;
    public String property_id;
    public String guest_id;
    public String host_id;
    @PropertyName("status")
    public Booking_status status;
    public String check_in_day;
    public String check_out_day;
    public double total_price;
    public String guest_note;
    public Date created_at;
    public Date updated_at;

    public Booking() {};

    public Booking(String property_id, String guest_id, String host_id, String check_in_day, String check_out_day, double total_price, String guest_note) {
        this.id = UUID.randomUUID().toString();
        this.property_id = property_id;
        this.guest_id = guest_id;
        this.host_id = host_id;
        this.status = Booking_status.ACCEPTED;
        this.check_in_day = check_in_day;
        this.check_out_day = check_out_day;
        this.total_price = total_price;
        this.guest_note = guest_note;
        this.created_at = new Date();
        this.updated_at = new Date();
    }

    public String getId() {
        return id;
    }

    @Exclude
    public Booking_status getBooking_status() {
        return status;
    }

    @PropertyName("status")
    public void setStatus(Booking_status status) {
        this.status = status;
    }
}
