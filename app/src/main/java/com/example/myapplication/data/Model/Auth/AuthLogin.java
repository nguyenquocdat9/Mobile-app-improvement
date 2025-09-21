package com.example.myapplication.data.Model.Auth;

public class AuthLogin {
    public String email;
    public String password;

    public AuthLogin(String login_email, String login_password) {
        this.email = login_email;
        this.password = login_password;
    }
}
