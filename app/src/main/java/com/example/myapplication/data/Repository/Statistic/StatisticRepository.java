package com.example.myapplication.data.Repository.Statistic;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.myapplication.data.Enum.Booking_status;
import com.example.myapplication.data.Model.Booking.Booking;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Model.Review.ReviewWithReviewerName;
import com.example.myapplication.data.Model.Statistic.NoPropertyException;
import com.example.myapplication.data.Model.Statistic.PropertyStatistic;
import com.example.myapplication.data.Model.Statistic.PropertyStatisticDetails;
import com.example.myapplication.data.Model.Statistic.ReviewStatistic;
import com.example.myapplication.data.Model.Statistic.ReviewStatisticDetails;
import com.example.myapplication.data.Repository.Booking.BookingRepository;
import com.example.myapplication.data.Repository.Property.PropertyRepository;
import com.example.myapplication.data.Repository.Review.ReviewRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class StatisticRepository {
    public BookingRepository bookingRepository;
    public ReviewRepository reviewRepository;
    public PropertyRepository propertyRepository;

    public StatisticRepository(Context context) {
        this.bookingRepository = new BookingRepository(context);
        this.reviewRepository = new ReviewRepository(context);
        this.propertyRepository = new PropertyRepository(context);
    }


    // Truyền vào tháng và năm muốn lấy dữ liệu của biến localDate
    public void getSinglePropertyStatisticByMonth(String propertyID, LocalDate localDate, OnSuccessListener<PropertyStatisticDetails> onSuccess, OnFailureListener onFailure) {
        PropertyStatisticDetails propertyStatisticDetails = new PropertyStatisticDetails();
        this.bookingRepository.getBookingsByPropertyId(propertyID, (bookings) -> {
            int date_count = 0;
            for (Booking booking : bookings) {
                if(booking.status == Booking_status.CANCELLED) {
                    continue;
                }
                String[] times = booking.check_in_day.split("-");
                String bookingYear = times[2];
                String bookingMonth = times[1];
                int currentYear = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    currentYear = localDate.getYear();
                }
                if (!bookingYear.equals(String.valueOf(currentYear))) {
                    continue;
                }
                int currentMonth = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    currentMonth = localDate.getMonthValue();
                }
                String mothValue = "";
                if (currentMonth < 10) {
                    mothValue = "0" + String.valueOf(currentMonth);
                } else {
                    mothValue = String.valueOf(currentMonth);
                }
                if (!bookingMonth.equals(mothValue)) {
                    continue;
                }
                List<String> bookingDateSeries = generateDateSeries(booking.check_in_day, booking.check_out_day);
                if (bookingDateSeries == null) {
                    continue;
                }
                for (String bookingDate : bookingDateSeries) {
                    String[] bookingDateSplit = bookingDate.split("-");
                    String bookingDateMonth = bookingDateSplit[1];

                    if (bookingDateMonth.equals(bookingMonth)) {
                        date_count++;
                    }
                }
            }
            // sau khi đã lấy được tất cả các ngày trong tháng mà phòng được sử dụng
            int numberOfDaysInThatMonth = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                numberOfDaysInThatMonth = getNumberOfDays(localDate);
            }
            double power = ((double) date_count / (double) numberOfDaysInThatMonth) * 100;
            power = Math.round(power * 100.0) / 100.0;
            propertyStatisticDetails.setAveragePowerByOneRoom(power);
            propertyStatisticDetails.setTimeUsedPerMonthByOneRoom(date_count);
            this.propertyRepository.getPropertyById(propertyID, (property) -> {
                propertyStatisticDetails.setMain_img_url(property.main_photo);
                propertyStatisticDetails.setName(property.name);
                onSuccess.onSuccess(propertyStatisticDetails);
            }, e -> {
                onFailure.onFailure(new Exception("Can not get property by propertyID"));
            });
        }, e -> {
            onFailure.onFailure(new Exception("Can not get bookings by propertyID"));
        });

    }

    public void getAllPropertyStatistic(String hostID, LocalDate localDate, OnSuccessListener<PropertyStatistic> onSuccess, OnFailureListener onFailure) {
        this.propertyRepository.getPropertyByUserID(hostID, (properties) -> {
            if(properties.isEmpty()) {
                onFailure.onFailure(new NoPropertyException("User has no properties"));
                return;
            }

            int size = properties.size();

            // Sử dụng object để synchronize và track state
            final Object lock = new Object();
            final AtomicInteger completedCount = new AtomicInteger(0);
            final AtomicReference<Double> totalPower = new AtomicReference<>(0.0);
            final AtomicInteger totalTimeUsed = new AtomicInteger(0);
            final List<PropertyStatisticDetails> detailsList = Collections.synchronizedList(new ArrayList<>());
            final AtomicBoolean isResultHandled = new AtomicBoolean(false);

            for(Property property : properties) {
                this.getSinglePropertyStatisticByMonth(property.id, localDate,
                        propertyStatisticDetails -> {
                            synchronized(lock) {
                                // Thêm vào kết quả
                                totalPower.updateAndGet(v -> v + propertyStatisticDetails.getAveragePowerByOneRoom());
                                totalTimeUsed.addAndGet(propertyStatisticDetails.getTimeUsedPerMonthByOneRoom());
                                detailsList.add(propertyStatisticDetails);

                                // Tăng counter và kiểm tra xem đã hoàn thành chưa
                                int completed = completedCount.incrementAndGet();

                                // Chỉ xử lý kết quả một lần duy nhất
                                if (completed == size && !isResultHandled.getAndSet(true)) {
                                    handleFinalResult(size, totalPower.get(), totalTimeUsed.get(), detailsList, onSuccess);
                                }
                            }
                        },
                        e -> {
                            synchronized(lock) {
                                // Tạo PropertyStatisticDetails rỗng với giá trị mặc định
                                PropertyStatisticDetails emptyDetails = new PropertyStatisticDetails();
                                emptyDetails.setAveragePowerByOneRoom(0.0);
                                emptyDetails.setTimeUsedPerMonthByOneRoom(0);
                                emptyDetails.setMain_img_url("");
                                emptyDetails.setName("N/A");

                                detailsList.add(emptyDetails);
                                // Không cộng vào totalPower và totalTimeUsed vì là 0

                                int completed = completedCount.incrementAndGet();

                                // Chỉ xử lý kết quả một lần duy nhất
                                if (completed == size && !isResultHandled.getAndSet(true)) {
                                    handleFinalResult(size, totalPower.get(), totalTimeUsed.get(), detailsList, onSuccess);
                                }
                            }
                        }
                );
            }
        }, e -> {
            onFailure.onFailure(new Exception("Can not get properties by userID"));
        });
    }

    // Method hỗ trợ với timeout để tránh treo vô hạn (optional)
    public void getAllPropertyStatisticWithTimeout(String hostID, LocalDate localDate, OnSuccessListener<PropertyStatistic> onSuccess, OnFailureListener onFailure, long timeoutMs) {
        Handler timeoutHandler = new Handler(Looper.getMainLooper());
        AtomicBoolean isCompleted = new AtomicBoolean(false);

        // Set timeout
        Runnable timeoutRunnable = () -> {
            if (isCompleted.compareAndSet(false, true)) {
                onFailure.onFailure(new Exception("Request timeout"));
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, timeoutMs);

        // Wrap callbacks để remove timeout khi complete
        OnSuccessListener<PropertyStatistic> wrappedSuccess = result -> {
            if (isCompleted.compareAndSet(false, true)) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                onSuccess.onSuccess(result);
            }
        };

        OnFailureListener wrappedFailure = error -> {
            if (isCompleted.compareAndSet(false, true)) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                onFailure.onFailure(error);
            }
        };

        // Gọi method chính
        getAllPropertyStatistic(hostID, localDate, wrappedSuccess, wrappedFailure);
    }

    // Enhanced handleFinalResult với validation
    private void handleFinalResult(int size,
                                   double totalPower,
                                   int totalTimeUsed,
                                   List<PropertyStatisticDetails> details,
                                   OnSuccessListener<PropertyStatistic> onSuccess) {

        // Validation
        if (details.size() != size) {
            Log.w("PropertyStats", "Details size mismatch: expected=" + size + ", actual=" + details.size());
        }

        // Tính toán average
        double powerAVG = size > 0 ? totalPower / (double) size : 0.0;
        powerAVG = Math.round(powerAVG * 100.0) / 100.0;

        double timeUsedAVG = size > 0 ? (double) totalTimeUsed / (double) size : 0.0;
        timeUsedAVG = Math.round(timeUsedAVG * 100.0) / 100.0;

        PropertyStatistic propertyStatistic = new PropertyStatistic();
        propertyStatistic.setNumberOfProperties(size);
        propertyStatistic.setAveragePower(powerAVG);
        propertyStatistic.setAverageTimesBookedPerMonthByAllProperties(timeUsedAVG);
        propertyStatistic.setDetails(details);

        // Log để debug
        Log.d("PropertyStats", "Final Result - Size: " + size +
                ", Total Power: " + totalPower +
                ", Average Power: " + powerAVG +
                ", Total Time Used: " + totalTimeUsed +
                ", Average Time Used: " + timeUsedAVG);

        onSuccess.onSuccess(propertyStatistic);
    }

    public List<String> generateDateSeries(String startDay, String endDate) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        List<String> dateSeries = new ArrayList<>();

        try {
            LocalDate start = LocalDate.parse(startDay, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            // Kiểm tra nếu ngày bắt đầu sau ngày kết thúc
            if (start.isAfter(end)) {
                return dateSeries; // Trả về danh sách rỗng
            }

            // Lặp từ ngày bắt đầu đến ngày kết thúc
            while (!start.isAfter(end)) {
                dateSeries.add(start.format(formatter));
                start = start.plusDays(1); // Tăng thêm 1 ngày
            }
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format. Please use dd-MM-yyyy.");
        }

        return dateSeries;
    }

    //targetDate là tháng muốn lấy dữ liệu và month là tháng hiện tại
    public int getNumberOfDays(LocalDate targetDate) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return 0;
        }
        return targetDate.lengthOfMonth();
    }

    public void getSinglePropertyReviewByMonth(String propertyID, LocalDate localDate, OnSuccessListener<ReviewStatisticDetails> onSuccess, OnFailureListener onFailure) {
        this.reviewRepository.getAllReviewByPropertyID(propertyID, reviews -> {
            int review_number = 0;
            int point = 0;
            double avg_ratings = 0;
            int five_star_count = 0;
            for(ReviewWithReviewerName review : reviews) {
                LocalDate createdAt;
                int year;
                int month;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createdAt = review.created_at.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    year = createdAt.getYear();
                    month = createdAt.getMonthValue();
                    if(year != localDate.getYear() || month != localDate.getMonthValue()) {
                        continue;
                    }
                } else {
                    onFailure.onFailure(new Exception("Can not convert date due to version"));
                }
                point += review.point;
                review_number++;
                if(review.point == 5) {
                    five_star_count++;
                }
            }
            avg_ratings = (double) point / (double) review_number;
            avg_ratings = Math.round(avg_ratings * 100.0) / 100.0;
            double five_star_rating_percentage = ((double) five_star_count / (double) review_number) * 100;
            five_star_rating_percentage = Math.round(five_star_rating_percentage * 100.0) / 100.0;
            double finalAvg_ratings = avg_ratings;
            int finalReview_number = review_number;
            int finalFive_star_count = five_star_count;
            double finalFive_star_rating_percentage = five_star_rating_percentage;
            int finalPoint = point;
            this.propertyRepository.getPropertyById(propertyID, property -> {
                ReviewStatisticDetails reviewStatisticDetails = new ReviewStatisticDetails(property.main_photo, property.name, finalAvg_ratings, finalReview_number, finalFive_star_rating_percentage, finalFive_star_count, finalPoint);
                onSuccess.onSuccess(reviewStatisticDetails);
            }, e-> {
                onFailure.onFailure(new Exception("Can not get property by propertyID"));
            });
        }, e -> {
            onFailure.onFailure(new Exception("Can not get reviews by propertyID"));
        });
    }

    public void getAllReviewStatistic(String hostID, LocalDate localDate, OnSuccessListener<ReviewStatistic> onSuccess, OnFailureListener onFailure) {
        this.propertyRepository.getPropertyByUserID(hostID, (properties) -> {
            if(properties.isEmpty()) {
                onFailure.onFailure(new NoPropertyException("User has no properties"));
                return;
            }
            // counting index
            AtomicInteger count = new AtomicInteger(0);
            int size = properties.size();
            // value to return
            AtomicInteger allRoomsPoint = new AtomicInteger(0);
            AtomicInteger allRoomsReviewsCount = new AtomicInteger(0);
            AtomicInteger allRoomsFiveStarCount = new AtomicInteger(0);
            AtomicReference<List<ReviewStatisticDetails>> details = new AtomicReference<>(new ArrayList<>());
            // duyệt qua âsc
            for(Property property : properties) {
                this.getSinglePropertyReviewByMonth(property.id, localDate, reviewStatisticDetails -> {
                    allRoomsPoint.updateAndGet(v -> v + reviewStatisticDetails.getPoint_total());
                    allRoomsReviewsCount.updateAndGet(v -> v + reviewStatisticDetails.getNumber_of_reviews());
                    allRoomsFiveStarCount.updateAndGet(v -> v + reviewStatisticDetails.getFive_star_rating_count());
                    details.updateAndGet(v -> {
                        v.add(reviewStatisticDetails);
                        return v;
                    });
                    count.incrementAndGet();
                    if(count.get() == size) {
                        double avg_Rating = allRoomsReviewsCount.get() == 0 ? 0.0 :
                                Math.round(((double) allRoomsPoint.get() / allRoomsReviewsCount.get()) * 100.0) / 100.0;

                        double five_star_rating_percentage = allRoomsReviewsCount.get() == 0 ? 0.0 :
                                Math.round(((double) allRoomsFiveStarCount.get() / allRoomsReviewsCount.get()) * 10000.0) / 100.0;

                        ReviewStatistic reviewStatistic = new ReviewStatistic(allRoomsReviewsCount.get(), avg_Rating, five_star_rating_percentage, details.get());
                        onSuccess.onSuccess(reviewStatistic);
                    }
                }, e-> {
                    ReviewStatisticDetails tmp  = new ReviewStatisticDetails();
                    details.updateAndGet(v -> {
                        v.add(tmp);
                        return v;
                    });
                    count.incrementAndGet();
                    if(count.get() == size) {
                        double avg_Rating = (double) allRoomsPoint.get() / (double) allRoomsReviewsCount.get();
                        avg_Rating = Math.round(avg_Rating * 100.0) / 100.0;
                        double five_star_rating_percentage = ((double) allRoomsFiveStarCount.get() / (double) allRoomsReviewsCount.get()) * 100;
                        five_star_rating_percentage = Math.round(five_star_rating_percentage * 100.0) / 100.0;
                        ReviewStatistic reviewStatistic = new ReviewStatistic(allRoomsReviewsCount.get(), avg_Rating, five_star_rating_percentage, details.get());
                        onSuccess.onSuccess(reviewStatistic);
                    }
                });
            }
        }, e-> {
            onFailure.onFailure(new Exception("Failed to get properties by user ID"));
        });
    }


    // Trả về giá trị từ tháng 1 đến tháng của localDate truyền vào, định dạng map với key là tháng còn value là double avgPower
    public void getPropertyPowerForChart(String hostID, LocalDate localDate, OnSuccessListener<Map<Integer, Double>> onSuccess, OnFailureListener onFailure) {
        int month;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            month = localDate.getMonthValue();
        } else {
            month = 0;
            onFailure.onFailure(new Exception("Can not convert date due to version"));
            return;
        }

        Map<Integer, Double> powerResultForChart = new ConcurrentHashMap<>();
        AtomicInteger count = new AtomicInteger(0);
        AtomicBoolean callbackCalled = new AtomicBoolean(false);

        for(int i = 1; i <= month; i++) {
            int m = i;
            int y = 0;
            y = localDate.getYear();

            this.getAllPropertyStatistic(hostID, LocalDate.of(y, m, 1),
                    propertyStatistic -> {
                        powerResultForChart.put(m, propertyStatistic.getAveragePower());
                        if (count.incrementAndGet() == month && callbackCalled.compareAndSet(false, true)) {
                            onSuccess.onSuccess(powerResultForChart);
                        }
                    },
                    e -> {
                        powerResultForChart.put(m, -1.0);
                        if (count.incrementAndGet() == month && callbackCalled.compareAndSet(false, true)) {
                            onSuccess.onSuccess(powerResultForChart);
                        }
                    });
        }
    }

    // Trả về giá trị từ tháng 1 đến tháng của localDate truyền vào, định dạng map với key là tháng còn value là double avgRating
    public void getRatingForChart(String hostID, LocalDate localDate, OnSuccessListener<Map<Integer, Double>> onSuccess, OnFailureListener onFailure) {
        int month;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            month = localDate.getMonthValue();
        } else {
            month = 0;
            onFailure.onFailure(new Exception("Can not convert date due to version"));
            return;
        }

        Map<Integer, Double> powerResultForChart = new ConcurrentHashMap<>();
        AtomicInteger count = new AtomicInteger(0);
        AtomicBoolean callbackCalled = new AtomicBoolean(false);

        for(int i = 1; i <= month; i++) {
            int m = i;
            int y = 0;
            y = localDate.getYear();

            this.getAllReviewStatistic(hostID, LocalDate.of(y, m, 1),
                    reviewStatistic -> {
                        powerResultForChart.put(m, reviewStatistic.getAverageRatings());
                        if (count.incrementAndGet() == month && callbackCalled.compareAndSet(false, true)) {
                            onSuccess.onSuccess(powerResultForChart);
                        }
                    },
                    e -> {
                        powerResultForChart.put(m, -1.0);
                        if (count.incrementAndGet() == month && callbackCalled.compareAndSet(false, true)) {
                            onSuccess.onSuccess(powerResultForChart);
                        }
                    });
        }
    }
}
