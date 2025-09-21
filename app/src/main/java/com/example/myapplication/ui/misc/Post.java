package com.example.myapplication.ui.misc;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.myapplication.data.Model.Property.Amenities;

import java.util.List;

public class Post implements Parcelable {
    private String id, title, name, imageResId, location, detailAddress, distance, dateRange, detail;
    private int total_review;
    private double avg_ratings;
    private Amenities amenities;
    public String normal_price;
    private String hostId;


    private List<String> sub_photos; // ảnh phụ (URLs)

    public Post(String id, String hostId, String title, String name, String imageResId, String location, String detailAddress, String detail,
                String distance, String dateRange, String normal_price,
                int total_review, double avg_ratings, Amenities amenities, List<String> sub_photos) {
        this.id = id;
        this.hostId = hostId;
        this.title = title;
        this.name = name;
        this.imageResId = imageResId;
        this.location = location;
        this.detailAddress = detailAddress;
        this.detail = detail;
        this.distance = distance;
        this.dateRange = dateRange;
        this.normal_price = normal_price;
        this.total_review = total_review;
        this.avg_ratings = avg_ratings;
        this.amenities = amenities;
        this.sub_photos = sub_photos;
    }

    protected Post(Parcel in) {
        id = in.readString();
        hostId = in.readString();
        title = in.readString();
        name = in.readString();
        imageResId = in.readString();
        location = in.readString();
        detailAddress = in.readString();
        detail = in.readString();
        distance = in.readString();
        dateRange = in.readString();
        normal_price = in.readString();
        total_review = in.readInt();
        avg_ratings = in.readDouble();
        amenities = in.readParcelable(Amenities.class.getClassLoader());
        sub_photos = in.createStringArrayList();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(hostId);
        dest.writeString(title);
        dest.writeString(name);
        dest.writeString(imageResId);
        dest.writeString(location);
        dest.writeString(detailAddress);
        dest.writeString(detail);
        dest.writeString(distance);
        dest.writeString(dateRange);
        dest.writeString(normal_price);

        dest.writeInt(total_review);
        dest.writeDouble(avg_ratings);
        dest.writeParcelable(amenities, flags);
        dest.writeStringList(sub_photos);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }

    public String getImageResId() {
        return imageResId;
    }

    public String getLocation() {
        return location;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public String getDistance() {
        return distance;
    }

    public String getDateRange() {
        return dateRange;
    }

    public String getNormal_price() {
        return normal_price;
    }

    public String getDetail() {
        return detail;
    }

    public int getTotalReview() {
        return total_review;
    }

    public double getAvgRatings() {
        return avg_ratings;
    }

    public Amenities getAmenities() {
        return amenities;
    }

    public String getId() {
        return id;
    }

    public String getHostId() {
        return hostId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }


    // Getter
    public List<String> getSub_photos() {
        Log.d("Post", "getSub_photos size: " + (sub_photos != null ? sub_photos.size() : 0));
        return sub_photos;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Post other = (Post) obj;
        // Add null check for id
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
