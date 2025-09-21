package com.example.myapplication.data.Enum;

import com.example.myapplication.R;

public enum PropertyType {
    House(R.drawable.ic_house),
    Apartment(R.drawable.ic_apartment),
    Villa(R.drawable.ic_villa),
    Homestay(R.drawable.ic_homestay),
    Hotel(R.drawable.ic_hotel);

    // Thêm thuộc tính cho ID tài nguyên ảnh
    private final int iconResId;

    // Constructor để khởi tạo giá trị cho mỗi loại PropertyType
    PropertyType(int iconResId) {
        this.iconResId = iconResId;
    }

    // Phương thức trả về ID tài nguyên ảnh
    public int getIconResId() {
        return iconResId;
    }

    public String getDescription() {
        switch (this) {
            case House:
                return "Nhà nguyên căn rộng rãi và riêng tư";

            case Apartment:
                return "Căn hộ tiện nghi trong tòa chung cư";
            case Villa:
                return "Biệt thự sân vườn cao cấp";
            case Homestay:
                return "Chia sẻ không gian sống";
            case Hotel:
                return "Phòng riêng tiện nghi với dịch vụ chuyên nghiệp";
            default:
                return "Loại chỗ ở không xác định";
        }
    }

    public String getValue() {
        return name(); // hoặc toString(), đều cho kết quả tương tự
    }
}
