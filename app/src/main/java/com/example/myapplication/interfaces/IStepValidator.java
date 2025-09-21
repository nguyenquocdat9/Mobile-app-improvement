package com.example.myapplication.interfaces;

public interface IStepValidator {
    void applyData();
    void save();
    boolean validate(String warning);
    int getStepIndex();
}
