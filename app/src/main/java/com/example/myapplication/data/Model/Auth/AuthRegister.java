package com.example.myapplication.data.Model.Auth;

public class AuthRegister {
    public String email;
    public String password;
    public String full_name;
    public String phone_number;

    public AuthRegister(String email, String password, String full_name, String phone_number) {
        this.email = email;
        this.password = password;
        this.full_name = full_name;
        this.phone_number = phone_number;
    }
}
