package com.example.myapplication.data.Repository.Booking;

import com.example.myapplication.data.Enum.Booking_status;
import com.example.myapplication.data.Enum.PropertyStatus;
import com.example.myapplication.data.Model.Booking.Booking;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Repository.FirebaseService;
import com.example.myapplication.data.Repository.Property.PropertyRepository;
import com.example.myapplication.data.Repository.User.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BookingRepository {
    private final FirebaseFirestore db;
    private final String COLLECTION_NAME = "bookings"; // Tên collection trong Firestore
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public BookingRepository(Context context) {
        this.db = FirebaseService.getInstance(context).getFireStore();
        this.propertyRepository = new PropertyRepository(context);
        this.userRepository = new UserRepository(context);
    }

    public void createBooking(Booking booking, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.propertyRepository.getPropertyById(booking.property_id, property -> {
            if(property.links == null || property.links.isEmpty()) {
                this.propertyRepository.updateBookedDate(booking.property_id, booking.check_in_day, booking.check_out_day,
                        unused1 -> {
                            this.propertyRepository.updatePropertyStatus(booking.property_id, PropertyStatus.Idle, unused -> {
                                this.db.collection(COLLECTION_NAME).document(booking.id).set(booking)
                                        .addOnSuccessListener(onSuccess)
                                        .addOnFailureListener(e -> {
                                            onFailure.onFailure(new Exception("Can not create Booking"));
                                        });
                            }, onFailure);
                        }, e -> {
                            onFailure.onFailure(new Exception("Can not add booked date to Property"));
                        });
            } else {
                this.propertyRepository.updateBookedDateWithLinksTransaction(booking.property_id, booking.check_in_day, booking.check_out_day,
                        unused1 -> {
                            this.propertyRepository.updatePropertyStatusWithLinksTransaction(booking.property_id, PropertyStatus.Idle, unused -> {
                                this.db.collection(COLLECTION_NAME).document(booking.id).set(booking)
                                        .addOnSuccessListener(onSuccess)
                                        .addOnFailureListener(e -> {
                                            onFailure.onFailure(new Exception("Can not create Booking"));
                                        });
                            }, onFailure);
                        }, e -> {
                            onFailure.onFailure(new Exception("Can not add booked date to Property"));
                        });
            }
        }, e-> {
            onFailure.onFailure(new Exception("Can not get property By ID"));
        });
    }

    public void getAllBookingsByHostID(String userID, OnSuccessListener<List<Booking>> onSuccess, OnFailureListener onFailure) {
        this.db.collection(COLLECTION_NAME).whereEqualTo("host_id", userID).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Booking> bookingList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Booking property = document.toObject(Booking.class);
                        bookingList.add(property);
                    }
                    onSuccess.onSuccess(bookingList);
                })
                .addOnFailureListener(onFailure);
    }

    public void getAllBookingByGuestID(String userID, OnSuccessListener<List<Booking>> onSuccess, OnFailureListener onFailure) {
        this.db.collection(COLLECTION_NAME).whereEqualTo("guest_id", userID).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Booking> bookingList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Booking property = document.toObject(Booking.class);
                        bookingList.add(property);
                    }
                    onSuccess.onSuccess(bookingList);
                })
                .addOnFailureListener(onFailure);
    }

    public void getBookingById(String bookingID, OnSuccessListener<Booking> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .document(bookingID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Booking booking = documentSnapshot.toObject(Booking.class);
                        if (booking != null) {
                            onSuccess.onSuccess(booking);
                        } else {
                            onFailure.onFailure(new Exception("Booking is null"));
                        }
                    } else {
                        onFailure.onFailure(new Exception("Booking not found"));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    // Host confirm that guest start the booking
    public void InProgressBooking(String currentUserID, String bookingId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.getBookingById(bookingId,
                booking -> {
                    if(booking.status != Booking_status.ACCEPTED && Objects.equals(booking.host_id, currentUserID)) {
                        HashMap<String, Object> updates = new HashMap<>();
                        updates.put("status", Booking_status.IN_PROGRESS);
                        updates.put("updated_at", new Date());
                        this.db.collection(COLLECTION_NAME).document(bookingId).update(updates)
                                .addOnSuccessListener(unused -> {
                                    this.propertyRepository.getPropertyById(booking.property_id, property -> {
                                        if(property.links == null || property.links.isEmpty()) {
                                            this.propertyRepository.updatePropertyStatus(booking.property_id, PropertyStatus.Renting, onSuccess, onFailure);
                                        } else {
                                            this.propertyRepository.updatePropertyStatusWithLinksTransaction(booking.property_id, PropertyStatus.Renting, onSuccess, onFailure);
                                        }
                                    }, e-> {
                                        onFailure.onFailure(new Exception("Can not update property status"));
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    onFailure.onFailure(new Exception("Can not update booking status"));
                                });
                    } else {
                        onFailure.onFailure(new Exception("Booking status must be PENDING"));
                    }
                },
                e -> {
                    onFailure.onFailure(new Exception("Can not get booking by ID"));
                });
    }

    public void setReviewedBooking(String bookingId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.getBookingById(bookingId,
                booking -> {
                    if(booking.status == Booking_status.COMPLETED) {
                        HashMap<String, Object> updates = new HashMap<>();
                        updates.put("status", Booking_status.REVIEWED);
                        updates.put("updated_at", new Date());
                        this.db.collection(COLLECTION_NAME).document(bookingId).update(updates)
                                .addOnSuccessListener(onSuccess)
                                .addOnFailureListener(onFailure);
                    } else {
                        onFailure.onFailure(new Exception("Booking status must be COMPLETED"));
                    }
                },
                onFailure);
    }

    // host or guest cancel booking -> Xóa những ngày đã đặt khỏi PropertyID.
    public void cancelBooking(String userID, String bookingId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.getBookingById(bookingId,
                booking -> {
                    if(booking.status != Booking_status.COMPLETED
                            && booking.status != Booking_status.IN_PROGRESS && booking.status != Booking_status.REVIEWED
                            && (booking.host_id.equals(userID) || booking.guest_id.equals(userID))) {
                        HashMap<String, Object> updates = new HashMap<>();
                        updates.put("status", Booking_status.CANCELLED);
                        updates.put("updated_at", new Date());
                        this.db.collection(COLLECTION_NAME).document(bookingId).update(updates)
                                .addOnSuccessListener(unused  ->{
                                    this.propertyRepository.getPropertyById(booking.property_id, property -> {
                                        if(property.links == null || property.links.isEmpty()) {
                                            // Remove booked dates from property
                                            propertyRepository.removeBookedDates(
                                                    booking.property_id,
                                                    booking.check_in_day,
                                                    booking.check_out_day,
                                                    unused1 -> {
                                                        this.propertyRepository.updatePropertyStatus(booking.property_id, PropertyStatus.Idle, onSuccess, onFailure);
                                                    },
                                                    e -> {
                                                        onFailure.onFailure(new Exception("Can not update property status"));
                                                    }
                                            );
                                        } else {
                                            // nếu có links thì phải xóa cả nhà của links đi nữa
                                            propertyRepository.removeBookedDatesWithLinkTransaction(booking.property_id, booking.check_in_day, booking.check_out_day, unused1 -> {
                                                propertyRepository.updatePropertyStatusWithLinksTransaction(booking.property_id, PropertyStatus.Idle, onSuccess, onFailure);
                                            }, e -> {
                                                onFailure.onFailure(new Exception("Can not update property status an its links"));
                                            });
                                        }
                                    }, e-> {
                                        onFailure.onFailure(new Exception("Can not update property status"));
                                    });
                                })
                                .addOnFailureListener(onFailure);
                    } else {
                        onFailure.onFailure(new Exception("Booking status must be PENDING"));
                    }
                },
                onFailure);
    }

    // booking complete: host to confirm
    public void completeBooking(String currentUserID, String bookingId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.getBookingById(bookingId,
                booking -> {
                    if(booking.status == Booking_status.IN_PROGRESS && Objects.equals(booking.host_id, currentUserID)) {
                        HashMap<String, Object> updates = new HashMap<>();
                        updates.put("status", Booking_status.COMPLETED);
                        updates.put("updated_at", new Date());
                        this.db.collection(COLLECTION_NAME).document(bookingId).update(updates)
                                .addOnSuccessListener(unused -> {
                                    this.userRepository.addRentingHistory(booking.guest_id, booking.id,
                                            unused1 -> {
                                                this.propertyRepository.getPropertyById(booking.property_id, property -> {
                                                    if(property.links == null || property.links.isEmpty()) {
                                                        this.propertyRepository.updatePropertyStatus(booking.property_id, PropertyStatus.Idle, onSuccess, onFailure);
                                                    } else {
                                                        this.propertyRepository.updatePropertyStatusWithLinksTransaction(booking.property_id, PropertyStatus.Idle, onSuccess, onFailure);
                                                    }
                                                }, e-> {
                                                    onFailure.onFailure(new Exception("Can not update property status"));
                                                });                                            },
                                            e -> {
                                                onFailure.onFailure(new Exception("Can not add to user Renting History: " + e.getMessage()));
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    onFailure.onFailure(new Exception("Không thể câp nhật trang thái"));
                                });
                    } else {
                        onFailure.onFailure(new Exception("Booking status must be ACCEPTED"));
                    }
                },
                onFailure);
    }

    // kiểm tra xem lệu guest có đang hoặc đã đặt phòng nào của host không ?
    public void checkGuestIsBookingHostProperty(String guestID, String hostID, OnSuccessListener<Booking_status> onSuccess, OnFailureListener onFailure) {
       this.db.collection(COLLECTION_NAME).whereEqualTo("host_id", hostID)
               .whereEqualTo("guest_id", guestID)
               .orderBy("updated_at", Query.Direction.DESCENDING)
               .limit(1) // Chỉ lấy 1 cái mới nhất
               .get()
               .addOnSuccessListener(queryDocumentSnapshots -> {
                   if (!queryDocumentSnapshots.isEmpty()) {
                       DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                       Booking booking = documentSnapshot.toObject(Booking.class);
                       if (booking != null) {
                           onSuccess.onSuccess(booking.status);
                       } else {
                           onFailure.onFailure(new Exception("Booking is null"));
                       }
                   } else {
                       onSuccess.onSuccess(Booking_status.CANCELLED);
                   }
               })
               .addOnFailureListener(onFailure);
    }

    // lấy booking theo propertyID
    public void getBookingsByPropertyId(String propertyId, OnSuccessListener<List<Booking>> onSuccess, OnFailureListener onFailure) {
        db.collection("bookings")
                .whereEqualTo("property_id", propertyId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Booking> bookings = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Booking booking = document.toObject(Booking.class);
                        if (booking != null) {
                            bookings.add(booking);
                        }
                    }
                    onSuccess.onSuccess(bookings);
                })
                .addOnFailureListener(onFailure);
    }

    public void getCompletedBooking(OnSuccessListener<List<Booking>> onSuccess, OnFailureListener onFailure) {
        db.collection("bookings")
                .whereEqualTo("status", Booking_status.COMPLETED)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Booking> bookings = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Booking booking = document.toObject(Booking.class);
                        if (booking != null) {
                            bookings.add(booking);
                        }
                    }
                    onSuccess.onSuccess(bookings);
                })
                .addOnFailureListener(onFailure);
    }

    public void deleteBooking(String id, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.db.collection(COLLECTION_NAME).document(id).delete().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }
}
