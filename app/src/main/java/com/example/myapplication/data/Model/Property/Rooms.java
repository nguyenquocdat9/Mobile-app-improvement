package com.example.myapplication.data.Model.Property;

public class Rooms {
    public int bedRooms; // co bao nhieu phong ngu
    public int livingRooms;
    public int kitchen;

    // Constructors
    public Rooms() {}

    public Rooms(int bedRooms, int livingRooms, int kitchen) {
        this.bedRooms = bedRooms;
        this.livingRooms = livingRooms;
        this.kitchen = kitchen;
    }

}
