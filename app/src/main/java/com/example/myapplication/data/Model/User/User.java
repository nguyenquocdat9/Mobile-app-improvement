package com.example.myapplication.data.Model.User;

import com.example.myapplication.data.Enum.Role;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User {
    public String uid;
    public String full_name;
    public String phone_number;
    public String avatar_link;
    public Role role;
    public List<String> rentingHistory;
    public List<String> wish_list;
    public Date created_at;
    public List<String> recent_list;
    public List<String> wishlist;
    public User() {} // FireStore cần constructor rỗng

    public User(String uid, String full_name, String phone_number) {
        this.uid = uid;
        this.full_name = full_name;
        this.phone_number = phone_number;
        this.created_at = new Date();
        this.role = Role.USER;
        this.rentingHistory = new ArrayList<>();
        this.wish_list = new ArrayList<>();
        this.avatar_link = "";
        this.recent_list = new ArrayList<>();
        this.wishlist = new ArrayList<>();
    }

    public void setAvatar_link(String avatar_link) {
        this.avatar_link = avatar_link;
    }

    public void setRentingHistory(List<String> rentingHistory) {
        this.rentingHistory = rentingHistory;
    }

    public void setWish_list(List<String> wish_list) {
        this.wish_list = wish_list;
    }
}
