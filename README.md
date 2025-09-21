# Android Application

Ứng dụng Android hiện đại được xây dựng với kiến trúc phân lớp rõ ràng, tích hợp Firebase và các dịch vụ cloud tiên tiến.

## 🏗️ Kiến trúc

Ứng dụng được thiết kế theo mô hình kiến trúc 3 lớp:

### UI Layer (Lớp Giao diện)
- **Activities/Fragments**: Quản lý giao diện người dùng
- **ViewModels**: Xử lý logic presentation và quản lý trạng thái UI
- **Pattern**: MVVM (Model-View-ViewModel)

### Repository Layer (Lớp Quản lý Dữ liệu)  
- **Data Management**: Quản lý và đồng bộ dữ liệu từ nhiều nguồn
- **Data Abstraction**: Cung cấp interface thống nhất cho các nguồn dữ liệu

### Backend Functions (Lớp Logic Nghiệp vụ)
- **API Integration**: Tích hợp với các dịch vụ bên ngoài
- **Data Processing**: Xử lý và biến đổi dữ liệu

## 🔧 Công nghệ & Dịch vụ

### Firebase Services
- **🔐 Firebase Authentication**: Xác thực người dùng an toàn
- **🗄️ Cloud Firestore**: Cơ sở dữ liệu NoSQL thời gian thực
- **📁 Firebase Storage**: Lưu trữ file và media
- **⚡ Cloud Functions**: Xử lý logic backend serverless
- **📱 Firebase Messaging**: Gửi thông báo push

### External Services
- **🔍 Algolia**: Tìm kiếm cơ bản với hiệu suất cao
- **🧠 FastAPI Server**: Xử lý NLP và tìm kiếm nâng cao

## ⚡ Tính năng chính

- **Xác thực đa phương thức**: Đăng nhập bảo mật với Firebase Auth
- **Tìm kiếm thông minh**: Tích hợp Algolia và NLP processing
- **Lưu trữ đám mây**: Quản lý file với Firebase Storage
- **Thông báo realtime**: Push notification và messaging
- **Auto-sync**: Đồng bộ dữ liệu tự động khi có kết nối

## 🚀 Cài đặt

### Yêu cầu hệ thống
- Android Studio Arctic Fox trở lên
- Android SDK 21+ (Android 5.0)
- Java 8+ hoặc Kotlin 1.5+


### Xác thực
- Hỗ trợ đăng nhập bằng email/password, Google
- Tự động đồng bộ profile người dùng

### Tìm kiếm
- **Tìm kiếm cơ bản**: Sử dụng Algolia cho tìm kiếm nhanh
- **Tìm kiếm nâng cao**: NLP processing qua FastAPI server

### Quản lý dữ liệu
- Upload/download file tự động với Firebase Storage
- Realtime sync với Cloud Firestore

## 🔄 Luồng dữ liệu

```
UI Layer → Repository Layer → Backend Functions → External Services
    ↑                                                      ↓
    └──────────── Data Flow ←──── Push Notifications ←──────┘
```

Link github 2 mô hình tìm kiếm
- Tìm kiếm bộ lọc: https://github.com/x0beR-143n/Mobile-Search.git
- Tìm kiếm bằng xử lý NLP: https://github.com/manhld2004/Mobile-NLP_Model.git

