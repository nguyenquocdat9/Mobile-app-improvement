package com.example.myapplication.data.Model.Statistic;

import java.util.ArrayList;
import java.util.List;

public class PropertyStatistic {
    private double averagePower;
    private int numberOfProperties;
    private double averageTimesBookedPerMonthByAllProperties;

    private List<PropertyStatisticDetails> details;

    public PropertyStatistic() {
        averagePower = 0;
        numberOfProperties = 0;
        averageTimesBookedPerMonthByAllProperties = 0;
        details = new ArrayList<>();
    }

    public PropertyStatistic(double averagePower, int numberOfProperties, double averageTimesBookedPerMonthByAllProperties, List<PropertyStatisticDetails> details) {
        this.averagePower = averagePower;
        this.numberOfProperties = numberOfProperties;
        this.averageTimesBookedPerMonthByAllProperties = averageTimesBookedPerMonthByAllProperties;
        this.details = details;
    }

    public void addPropertyStatisticDetails(PropertyStatisticDetails propertyStatisticDetails) {
        this.details.add(propertyStatisticDetails);
    }

    public void setAveragePower(double averagePower) {
        this.averagePower = averagePower;
    }

    public void setNumberOfProperties(int numberOfProperties) {
        this.numberOfProperties = numberOfProperties;
    }

    public void setAverageTimesBookedPerMonthByAllProperties(double averageTimesBookedPerMonthByAllProperties) {
        this.averageTimesBookedPerMonthByAllProperties = averageTimesBookedPerMonthByAllProperties;
    }

    public double getAveragePower() {
        return averagePower;
    }

    public int getNumberOfProperties() {
        return numberOfProperties;
    }

    public double getAverageTimesBookedPerMonthByAllProperties() {
        return averageTimesBookedPerMonthByAllProperties;
    }

    public List<PropertyStatisticDetails> getDetails() {
        return details;
    }

    public void setDetails(List<PropertyStatisticDetails> details) {
        this.details = details;
    }
}
