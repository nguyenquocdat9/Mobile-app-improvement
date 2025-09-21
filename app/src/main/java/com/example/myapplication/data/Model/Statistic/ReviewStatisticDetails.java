package com.example.myapplication.data.Model.Statistic;

public class ReviewStatisticDetails {

    private String main_photo_url;
    private String name;
    private double avg_rating;
    private int number_of_reviews;
    private double five_star_rating_percentage;
    private int five_star_rating_count;
    private int point_total;
    // Constructor
    public ReviewStatisticDetails(String main_photo_url, String name, double avg_rating, int number_of_reviews, double five_star_rating_percentage, int five_star_rating_count, int point_total) {
        this.main_photo_url = main_photo_url;
        this.name = name;
        this.avg_rating = avg_rating;
        this.number_of_reviews = number_of_reviews;
        this.five_star_rating_percentage = five_star_rating_percentage;
        this.five_star_rating_count = five_star_rating_count;
        this.point_total = point_total;
    }

    public ReviewStatisticDetails() {
        this.main_photo_url = "";
        this.name = "";
        this.avg_rating = 0.0;
        this.number_of_reviews = 0;
        this.five_star_rating_percentage = 0.0;
        five_star_rating_count = 0;
        point_total = 0;
    }

    // Getters and Setters
    public int getFive_star_rating_count() {
        return five_star_rating_count;
    }

    public void setFive_star_rating_count(int five_star_rating_count) {
        this.five_star_rating_count = five_star_rating_count;
    }

    public int getPoint_total() {
        return point_total;
    }

    public void setPoint_total(int point_total) {
        this.point_total = point_total;
    }

    public String getMain_photo_url() {
        return main_photo_url;
    }

    public void setMain_photo_url(String main_photo_url) {
        this.main_photo_url = main_photo_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAvg_rating() {
        return avg_rating;
    }

    public void setAvg_rating(double avg_rating) {
        this.avg_rating = avg_rating;
    }

    public int getNumber_of_reviews() {
        return number_of_reviews;
    }

    public void setNumber_of_reviews(int number_of_reviews) {
        this.number_of_reviews = number_of_reviews;
    }

    public double getFive_star_rating_percentage() {
        return five_star_rating_percentage;
    }

    public void setFive_star_rating_percentage(double five_star_rating_percentage) {
        this.five_star_rating_percentage = five_star_rating_percentage;
    }
}
