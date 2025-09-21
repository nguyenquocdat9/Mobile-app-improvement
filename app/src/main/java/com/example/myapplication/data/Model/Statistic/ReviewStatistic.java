package com.example.myapplication.data.Model.Statistic;

import java.util.ArrayList;
import java.util.List;

public class ReviewStatistic {
    private int numberOfReviews;
    private double averageRatings;
    private double fiveStarRatingPercentage;
    private List<ReviewStatisticDetails> details;

    public ReviewStatistic() {
        this.numberOfReviews = 0;
        this.averageRatings = 0;
        this.fiveStarRatingPercentage = 0.0;
        this.details = new ArrayList<>();
    }

    // Constructor
    public ReviewStatistic(int numberOfReviews, double averageRatings, double fiveStarRatingPercentage, List<ReviewStatisticDetails> details) {
        this.numberOfReviews = numberOfReviews;
        this.averageRatings = averageRatings;
        this.fiveStarRatingPercentage = fiveStarRatingPercentage;
        this.details = details;
    }

    // Getter and Setter
    public int getNumberOfReviews() {
        return numberOfReviews;
    }

    public void setNumberOfReviews(int numberOfReviews) {
        this.numberOfReviews = numberOfReviews;
    }

    public double getAverageRatings() {
        return averageRatings;
    }

    public void setAverageRatings(double averageRatings) {
        this.averageRatings = averageRatings;
    }

    public double getFiveStarRatingPercentage() {
        return fiveStarRatingPercentage;
    }

    public void setFiveStarRatingPercentage(double fiveStarRatingPercentage) {
        this.fiveStarRatingPercentage = fiveStarRatingPercentage;
    }

    public List<ReviewStatisticDetails> getDetails() {
        return details;
    }

    public void setDetails(List<ReviewStatisticDetails> details) {
        this.details = details;
    }
}
