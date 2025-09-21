package com.example.myapplication.data.Repository.Property;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.example.myapplication.data.Enum.PropertyStatus;

import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Model.Property.SearchProperty;
import com.example.myapplication.data.Model.Search.BookedDateRequest;
import com.example.myapplication.data.Model.Search.SearchResponse;
import com.example.myapplication.data.Repository.FirebaseService;
import com.example.myapplication.data.Repository.Search.PropertyAPIClient;
import com.example.myapplication.data.Repository.Storage.StorageRepository;
import com.example.myapplication.ui.fragments.LinkValidator;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.firebase.firestore.Transaction;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PropertyRepository {
    private final FirebaseFirestore db;
    private final StorageRepository storageRepository;
    private final String COLLECTION_NAME = "properties"; // Tên collection trong Firestore
    private final PropertyAPIClient propertyAPIClient;
    public PropertyRepository(Context context) {
        this.db = FirebaseService.getInstance(context).getFireStore();
        this.propertyAPIClient = new PropertyAPIClient();
        this.storageRepository = new StorageRepository(context);
    }

    public void addProperty(Property property, Uri main_image ,List<Uri> sub_images , OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        String propertyID = property.id;
        storageRepository.uploadMainImage(propertyID, main_image, mainImageUrl -> {
            property.setMainPhoto(mainImageUrl);

            // 2. Upload các ảnh phụ
            storageRepository.uploadHouseSubImages(propertyID, sub_images, subImageUrls -> {
                property.setSub_photos(subImageUrls);

                // 3. Khi cả hai xong thì mới add vào Firestore
                db.collection(COLLECTION_NAME).document(property.id)
                        .set(property)
                        .addOnSuccessListener(onSuccess)
                        .addOnFailureListener(onFailure);
            }, onFailure);

        }, onFailure);
    }

    /*
    public void addProperty(Property property, String main_image, List<String> sub_images, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        String propertyID = property.id;
        property.setMainPhoto(main_image);
        property.setSub_photos(sub_images);
        db.collection(COLLECTION_NAME).document(property.id)
                .set(property)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
     */

    // Nếu main img.png là url rồi thì không cần làm gì
    // Nếu main img.png là uri thì phải up lên storage
    public void addProperty(Property property, Context context, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        String ID = UUID.randomUUID().toString();
        property.id = ID;
        List<String> sub_img = property.sub_photos;
        String main_images = property.main_photo;

        Uri main_img_uri = null;

        // kiểm tra xem main img.png có phải uri không
        if(LinkValidator.isValidUri(context, main_images)) {
            main_img_uri = Uri.parse(main_images);
        }

        List<Uri> sub_photos_uri = new ArrayList<>();
        List<String> sub_photos_url = new ArrayList<>();

        for(String uri : sub_img) {
            if(LinkValidator.isValidUri(context, uri)) {
               sub_photos_uri.add(Uri.parse(uri));
            } else {
                sub_photos_url.add(uri);
            }
        }

        if(main_img_uri != null) {
            this.storageRepository.uploadMainImage(ID, main_img_uri, main_img_url -> {
                property.setMainPhoto(main_img_url);
                this.storageRepository.uploadHouseSubImages(ID, sub_photos_uri, sub_img_urls -> {
                    sub_img_urls.addAll(sub_photos_url);
                    property.setSub_photos(sub_img_urls);
                    this.db.collection(COLLECTION_NAME).document(ID)
                            .set(property)
                            .addOnSuccessListener(unused -> {

                                propertyAPIClient.addPropertyToAlgolia(new SearchProperty(property), new PropertyAPIClient.OnPropertyCallback() {
                                    @Override
                                    public void onSuccess(SearchResponse response) {
                                        onSuccess.onSuccess(null);
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        onFailure.onFailure(new Exception("Can not add property into Algolia"));
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                onFailure.onFailure(new Exception("Can not add property into db"));
                            });
                }, e -> {
                    onFailure.onFailure(new Exception("Can not upload Sub images to Storage"));
                });
            }, e-> {
                onFailure.onFailure(new Exception("Can not upload main image to Storage"));
            });
        } else {
            this.storageRepository.uploadHouseSubImages(ID, sub_photos_uri, sub_img_urls -> {
                sub_img_urls.addAll(sub_photos_url);
                property.setSub_photos(sub_img_urls);
                this.db.collection(COLLECTION_NAME).document(ID)
                        .set(property)
                        .addOnSuccessListener(unused -> {

                        })
                        .addOnFailureListener(onFailure);
            }, e -> {
                onFailure.onFailure(new Exception("Can not upload Sub images to Storage"));
            });
        }
    }

    public void updateProperty(String oldProperty_ID, Property property, Context context, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        String ID = oldProperty_ID;
        List<String> sub_img = property.sub_photos;
        String main_images = property.main_photo;

        Uri main_img_uri = null;

        // kiểm tra xem main img.png có phải uri không
        if(LinkValidator.isValidUri(context, main_images)) {
            main_img_uri = Uri.parse(main_images);
        }

        List<Uri> sub_photos_uri = new ArrayList<>();
        List<String> sub_photos_url = new ArrayList<>();

        for(String uri : sub_img) {
            if(LinkValidator.isValidUri(context, uri)) {
                sub_photos_uri.add(Uri.parse(uri));
            } else {
                sub_photos_url.add(uri);
            }
        }

        if(main_img_uri != null) {
            this.storageRepository.uploadMainImage(ID, main_img_uri, main_img_url -> {
                property.setMainPhoto(main_img_url);
                this.storageRepository.uploadHouseSubImages(ID, sub_photos_uri, sub_img_urls -> {
                    sub_img_urls.addAll(sub_photos_url);
                    property.setSub_photos(sub_img_urls);
                    this.db.collection(COLLECTION_NAME).document(ID)
                            .set(property)
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(e -> {
                                onFailure.onFailure(new Exception("Can not add property into db"));
                            });
                }, e -> {
                    onFailure.onFailure(new Exception("Can not upload Sub images to Storage"));
                });
            }, e-> {
                onFailure.onFailure(new Exception("Can not upload main image to Storage"));
            });
        } else {
            this.storageRepository.uploadHouseSubImages(ID, sub_photos_uri, sub_img_urls -> {
                sub_img_urls.addAll(sub_photos_url);
                property.setSub_photos(sub_img_urls);
                this.db.collection(COLLECTION_NAME).document(ID)
                        .set(property)
                        .addOnSuccessListener(onSuccess)
                        .addOnFailureListener(onFailure);
            }, e -> {
                onFailure.onFailure(new Exception("Can not upload Sub images to Storage"));
            });
        }
    }

    public void updatePropertyAvgRatings(String id, int point ,OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.getPropertyById(id,
                property -> {
                    double avg_rating = property.avg_ratings;
                    double total_point = avg_rating * property.total_reviews;
                    double new_avg_rating = (total_point + point) / (property.total_reviews + 1);
                    db.collection(COLLECTION_NAME).document(id).update("avg_ratings", new_avg_rating)
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(onFailure);
                },
                onFailure);
    }

    public void updatePropertyAvgRatingWhenReviewModified(String id, int old_point, int new_point ,OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.getPropertyById(id,
                property -> {
                    double avg_rating = property.avg_ratings;
                    double total_point = avg_rating * property.total_reviews;
                    double new_avg_rating = (total_point + new_point - old_point) / (property.total_reviews);
                    db.collection(COLLECTION_NAME).document(id).update("avg_ratings", new_avg_rating)
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(onFailure);
                },
                onFailure);
    }

    public void updatePropertyTotalReviews(String id, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME).document(id).update("total_reviews", FieldValue.increment(1))
                .addOnFailureListener(onFailure)
                .addOnSuccessListener(onSuccess);
    }

    public void deleteProperty(String id, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void getAllProperties(OnSuccessListener<List<Property>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Property> propertyList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Property property = document.toObject(Property.class);
                        propertyList.add(property);
                    }
                    List<String> targetIDs = Arrays.asList(
                            "4d36355e-5613-4154-8b19-dacf94faec04",
                            "5d3c3511-09a4-4604-9391-50b1092e8e44",
                            "7a850e26-fb7f-484d-b908-acdd6370f240",
                            "9a6779e1-4ffb-494e-961e-eb1809d45651",
                            "a158940d-0f68-4e16-adba-a5896ab2e21f"
                    );
                    List<Property> prioritized = new ArrayList<>();
                    List<Property> remaining = new ArrayList<>();

                    for (Property property : propertyList) {
                        if (targetIDs.contains(property.id)) {
                            prioritized.add(property);
                        } else {
                            remaining.add(property);
                        }
                    }
                    prioritized.addAll(remaining);
                    onSuccess.onSuccess(prioritized);
                })
                .addOnFailureListener(onFailure);
    }

    public void getPropertyById(String id, OnSuccessListener<Property> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Property property = documentSnapshot.toObject(Property.class);
                        if (property != null) {
                            onSuccess.onSuccess(property);
                        } else {
                            onFailure.onFailure(new Exception("Property is null"));
                        }
                    } else {
                        onFailure.onFailure(new Exception("Property not found"));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void getPropertyByName(String name, OnSuccessListener<List<Property>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("name", name)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots  -> {
                    List<Property> propertyList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Property property = document.toObject(Property.class);
                        propertyList.add(property);
                    }
                    onSuccess.onSuccess(propertyList);
                })
                .addOnFailureListener(onFailure);
    }

    public void getPropertyByUserID(String userID, OnSuccessListener<List<Property>> onSuccess, OnFailureListener onFailure) {
        this.db.collection(COLLECTION_NAME).whereEqualTo("host_id", userID).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Property> propertyList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Property property = document.toObject(Property.class);
                        propertyList.add(property);
                    }
                    onSuccess.onSuccess(propertyList);
                })
                .addOnFailureListener(onFailure);
    }

    private List<String> generateDateSeries(String startDay, String endDate) {
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

    public boolean validateBookedDate(List<String> existingDates, List<String> targetDates) {
        // Tạo Set để tối ưu việc kiểm tra tồn tại
        Set<String> existingDateSet = new HashSet<>(existingDates);

        // Nếu danh sách targetDates rỗng (ngày không hợp lệ hoặc startDate > endDate)
        if (targetDates.isEmpty()) {
            return false;
        }

        // Kiểm tra từng ngày trong targetDates
        for (String date : targetDates) {
            if (existingDateSet.contains(date)) {
                return false; // Có ngày đã đặt → không hợp lệ
            }
        }

        return true; // Không có ngày nào trùng → hợp lệ
    }

    /*
    //Gọi cái này để thêm vào link nhé, không gọi cái dưới
    public void addLinksToBothProperty(String propertyID, String link_id, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.addLinksToProperty(propertyID, link_id, unused -> {
            this.addLinksToProperty(link_id, propertyID, onSuccess, onFailure);
        }, e-> {
            onFailure.onFailure(new Exception("Can not add links to property: " + e.getMessage()));
        });
    }

    public void addLinksToProperty(String propertyID, String link_id, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.getPropertyById(link_id, property -> {
            this.db.collection(COLLECTION_NAME).document(propertyID).update("links", FieldValue.arrayUnion(link_id))
                    .addOnSuccessListener(onSuccess)
                    .addOnFailureListener(e1 -> {
                        onFailure.onFailure(new Exception("Can not add links to property: " + e1.getMessage() ));
                    });
        }, e-> {
            onFailure.onFailure(new Exception("Link Id của Property không tồn tại"));
        });
    }
    */
    public void addLinksToBothProperty(String propertyID, String link_id, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {

                        DocumentReference mainPropertyReference = db.collection("properties").document(propertyID);
                        DocumentSnapshot mainPropertySnapshot = transaction.get(mainPropertyReference);

                        DocumentReference linkedPropertyReference = db.collection("properties").document(link_id);
                        DocumentSnapshot linkedPropertySnapshot = transaction.get(linkedPropertyReference);
                        if (!mainPropertySnapshot.exists() || !linkedPropertySnapshot.exists()) {
                            throw new FirebaseFirestoreException("One or both properties not found",
                                    FirebaseFirestoreException.Code.NOT_FOUND);
                        }
                        Property mainProperty = mainPropertySnapshot.toObject(Property.class);
                        Property linkProperty = linkedPropertySnapshot.toObject(Property.class);

                        List<String> mainPropertyBookedDate = mainProperty.booked_date;
                        List<String> linkPropertyBookedDate = linkProperty.booked_date;

                        if (mainPropertyBookedDate == null || linkPropertyBookedDate == null) {
                            throw new FirebaseFirestoreException("One or both bookedDate is null",
                                    FirebaseFirestoreException.Code.NOT_FOUND);
                        }

                        Set<String> mergedSet = new HashSet<>();
                        mergedSet.addAll(mainPropertyBookedDate);
                        mergedSet.addAll(linkPropertyBookedDate);

                        List<String> combinedBookedDate = new ArrayList<>(mergedSet);
                        transaction.update(linkedPropertyReference, "booked_date", combinedBookedDate);
                        transaction.update(mainPropertyReference, "booked_date", combinedBookedDate);

                        transaction.update(mainPropertyReference, "links", FieldValue.arrayUnion(link_id));
                        transaction.update(linkedPropertyReference, "links", FieldValue.arrayUnion(propertyID));
                        return null; // Transaction thành công
                    }
                }).addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }


    public void deleteLinksToProperty(String propertyID, String link_id, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.getPropertyById(link_id, property -> {
            this.db.collection(COLLECTION_NAME).document(propertyID).update("links", FieldValue.arrayRemove(link_id))
                    .addOnSuccessListener(onSuccess)
                    .addOnFailureListener(e1 -> {
                        onFailure.onFailure(new Exception("Can not add links to property: " + e1.getMessage() ));
                    });
        }, e -> {
           onFailure.onFailure(new Exception("Link Id của Property không tồn tại"));
        });
    }

    public void updatePropertyStatus(String propertyID, PropertyStatus status, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.db.collection(COLLECTION_NAME).document(propertyID).update("status", status)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void updatePropertyStatusWithLinksTransaction(String propertyID, PropertyStatus status, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.db.runTransaction(new Transaction.Function<Void>() {

            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                // READ PHASE
                if(propertyID == null) {
                    throw new FirebaseFirestoreException("Property not found",
                            FirebaseFirestoreException.Code.NOT_FOUND);
                }
                DocumentReference mainPropertyReference = db.collection("properties").document(propertyID);
                DocumentSnapshot mainPropertySnapshot = transaction.get(mainPropertyReference);

                if (!mainPropertySnapshot.exists()) {
                    throw new FirebaseFirestoreException("Property not found",
                            FirebaseFirestoreException.Code.NOT_FOUND);
                }

                Property mainProperty = mainPropertySnapshot.toObject(Property.class);
                if(mainProperty == null) {
                    throw new FirebaseFirestoreException("Property not found",
                            FirebaseFirestoreException.Code.NOT_FOUND);
                }
                List<String> links = mainProperty.links;
                // WRITE PHASE
                transaction.update(mainPropertyReference, "status", status);
                transaction.update(mainPropertyReference, "updated_at", new Date());
                for (String link : links) {
                    DocumentReference linkPropertyReference = db.collection("properties").document(link);
                    transaction.update(linkPropertyReference, "status", status);
                    transaction.update(linkPropertyReference, "updated_at", new Date());
                }

                return null;
            }
        }).addOnSuccessListener(onSuccess)
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new Exception("Can not update status for property and its LINK"));
                });
    }

    // Lưu theo định dạng dd-MM-yyyy - Front end check xem ngày Start có lớn hơn ngày End không
    public void updateBookedDate(String propertyId, String startDate, String endDate, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.getPropertyById(propertyId,
                property -> {
                    List<String> dateSeries = this.generateDateSeries(startDate, endDate);
                    if(dateSeries == null) {
                        onFailure.onFailure(new Exception("Can not generate booked Date"));
                    } else {
                        if(dateSeries.isEmpty()) {
                            onFailure.onFailure(new Exception("Can not generate booked Date"));
                        } else {
                            if (validateBookedDate(property.booked_date, dateSeries)) {
                                this.db.collection(COLLECTION_NAME).document(propertyId)
                                        .update("booked_date", FieldValue.arrayUnion(dateSeries.toArray()))
                                        .addOnSuccessListener(unused -> {
                                            propertyAPIClient.addBookedDate(propertyId, new BookedDateRequest(dateSeries), new PropertyAPIClient.OnPropertyCallback() {
                                                @Override
                                                public void onSuccess(SearchResponse response) {
                                                    onSuccess.onSuccess(null);
                                                }

                                                @Override
                                                public void onError(String errorMessage) {
                                                    onFailure.onFailure(new Exception("Can not add booked-Date into Algolia"));
                                                }
                                            });
                                        })
                                        .addOnFailureListener(onFailure);
                            } else {
                                onFailure.onFailure(new Exception("Booked date is invalid"));
                            }
                        }
                    }
                },
                e -> {
                    onFailure.onFailure(new Exception("Property ID is invalid"));
                });
    }

    public void updateBookedDateWithLinksTransaction(String propertyID, String startDate, String endDate,
                                                     OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {

        this.db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {

                // ========== PHASE 1: TẤT CẢ READS TRƯỚC ==========

                // 1. Đọc main property
                DocumentReference mainPropertyRef = db.collection("properties").document(propertyID);
                DocumentSnapshot mainPropertySnapshot = transaction.get(mainPropertyRef);

                if (!mainPropertySnapshot.exists()) {
                    throw new FirebaseFirestoreException("Property not found",
                            FirebaseFirestoreException.Code.NOT_FOUND);
                }

                Property mainProperty = mainPropertySnapshot.toObject(Property.class);
                if (mainProperty == null) {
                    throw new FirebaseFirestoreException("Cannot parse property data",
                            FirebaseFirestoreException.Code.DATA_LOSS);
                }

                // 2. Đọc TẤT CẢ linked properties trước (nếu có)
                Map<String, DocumentSnapshot> linkedSnapshots = new HashMap<>();
                if (mainProperty.links != null && !mainProperty.links.isEmpty()) {
                    for (String linkId : mainProperty.links) {
                        DocumentReference linkPropertyRef = db.collection("properties").document(linkId);
                        DocumentSnapshot linkSnapshot = transaction.get(linkPropertyRef);

                        if (!linkSnapshot.exists()) {
                            throw new FirebaseFirestoreException("Linked property not found: " + linkId,
                                    FirebaseFirestoreException.Code.NOT_FOUND);
                        }

                        linkedSnapshots.put(linkId, linkSnapshot);
                    }
                }

                // ========== PHASE 2: VALIDATION ==========

                // 3. Generate date series
                List<String> dateSeries = generateDateSeries(startDate, endDate);
                if (dateSeries == null || dateSeries.isEmpty()) {
                    throw new FirebaseFirestoreException("Cannot generate date series",
                            FirebaseFirestoreException.Code.INVALID_ARGUMENT);
                }

                // 4. Validate main property
                if (!validateBookedDate(mainProperty.booked_date, dateSeries)) {
                    throw new FirebaseFirestoreException("Invalid booked date for main property",
                            FirebaseFirestoreException.Code.INVALID_ARGUMENT);
                }

                // 5. Validate tất cả linked properties
                for (Map.Entry<String, DocumentSnapshot> entry : linkedSnapshots.entrySet()) {
                    String linkId = entry.getKey();
                    DocumentSnapshot linkSnapshot = entry.getValue();

                    Property linkProperty = linkSnapshot.toObject(Property.class);
                    if (linkProperty == null) {
                        throw new FirebaseFirestoreException("Cannot parse linked property data: " + linkId,
                                FirebaseFirestoreException.Code.DATA_LOSS);
                    }

                    if (!validateBookedDate(linkProperty.booked_date, dateSeries)) {
                        throw new FirebaseFirestoreException("Invalid booked date for linked property: " + linkId,
                                FirebaseFirestoreException.Code.INVALID_ARGUMENT);
                    }
                }

                // ========== PHASE 3: TẤT CẢ WRITES SAU ==========

                // 6. Update main property
                transaction.update(mainPropertyRef, "booked_date", FieldValue.arrayUnion(dateSeries.toArray()));

                // 7. Update tất cả linked properties
                for (String linkId : linkedSnapshots.keySet()) {
                    DocumentReference linkPropertyRef = db.collection("properties").document(linkId);
                    transaction.update(linkPropertyRef, "booked_date", FieldValue.arrayUnion(dateSeries.toArray()));
                }

                return null; // Transaction thành công
            }
        }).addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // khi remove thì remove cả nhà cũng được link luôn
    public void removeBookedDates(String propertyId, String checkIn, String checkOut, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        // Fetch property, remove dates in range [checkIn, checkOut] from booked_date, and update
        db.collection("properties").document(propertyId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> bookedDates = (List<String>) documentSnapshot.get("booked_date");
                    if (bookedDates == null) bookedDates = new ArrayList<>();
                    List<String> toRemove = getDateRange(checkIn, checkOut); // implement this utility
                    bookedDates.removeAll(toRemove);
                    db.collection("properties").document(propertyId)
                            .update("booked_date", bookedDates)
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    public void removeBookedDatesWithLinkTransaction(String propertyID, String checkIn, String checkOut,  OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                // READ PHASE
                DocumentReference mainPropertyReference = db.collection(COLLECTION_NAME).document(propertyID);
                DocumentSnapshot mainPropertySnapshot = transaction.get(mainPropertyReference);

                if(!mainPropertySnapshot.exists()) {
                    throw new FirebaseFirestoreException("Property not found",
                            FirebaseFirestoreException.Code.NOT_FOUND);
                }

                Property mainProperty = mainPropertySnapshot.toObject(Property.class);
                if(mainProperty == null) {
                    throw new FirebaseFirestoreException("Property not found",
                            FirebaseFirestoreException.Code.NOT_FOUND);
                }
                List<String> links = mainProperty.links;
                List<String> mainPropertyBookedDates = mainProperty.booked_date;
                List<List<String>> linkedPropertyBookedDates = new ArrayList<>();
                for(String link: links) {
                    DocumentReference linkPropertyReference = db.collection(COLLECTION_NAME).document(link);
                    DocumentSnapshot linkPropertySnapshot = transaction.get(linkPropertyReference);

                    Property linkedProperty = linkPropertySnapshot.toObject(Property.class);
                    if(linkedProperty == null) {
                        throw new FirebaseFirestoreException("Property linked null",
                                FirebaseFirestoreException.Code.NOT_FOUND);
                    }
                    linkedPropertyBookedDates.add(linkedProperty.booked_date);
                }

                // WRITE PHASE
                List<String> toRemove = generateDateSeries(checkIn, checkOut);
                if(toRemove == null) {
                    throw new FirebaseFirestoreException("Date series is null",
                            FirebaseFirestoreException.Code.INVALID_ARGUMENT);
                }
                mainPropertyBookedDates.removeAll(toRemove);
                transaction.update(mainPropertyReference, "booked_date", mainPropertyBookedDates);

                for(int i = 0; i< links.size(); i++) {
                    List<String> booked_date = linkedPropertyBookedDates.get(i);
                    booked_date.removeAll(toRemove);
                    DocumentReference linkedProperty = db.collection(COLLECTION_NAME).document(links.get(i));
                    transaction.update(linkedProperty, "booked_date", booked_date);
                }

                return null;
            }
        }).addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // Utility to get all dates between checkIn and checkOut (inclusive) in dd-MM-yyyy format
    private List<String> getDateRange(String start, String end) {
        List<String> dates = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date startDate = sdf.parse(start);
            Date endDate = sdf.parse(end);
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            while (!cal.getTime().after(endDate)) {
                dates.add(sdf.format(cal.getTime()));
                cal.add(Calendar.DATE, 1);
            }
        } catch (Exception e) {
            // handle parse error
        }
        return dates;
    }

    public void getPropertySortedByPriceAsc(List<String> property_ids, OnSuccessListener<List<Property>> onSuccess, OnFailureListener onFailure) {
        if (property_ids == null || property_ids.isEmpty()) {
            onFailure.onFailure(new Exception("Property IDs list is empty"));
            return;
        }

        List<Property> properties = new ArrayList<>();
        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicBoolean hasError = new AtomicBoolean(false);

        for (String id : property_ids) {
            getPropertyById(id,
                    property -> {
                        synchronized (properties) {
                            if (!hasError.get()) {
                                property.id = (id); // Set ID cho property
                                properties.add(property);

                                // Kiểm tra xem đã lấy hết tất cả property chưa
                                if (completedCount.incrementAndGet() == property_ids.size()) {
                                    // Sắp xếp theo giá tăng dần
                                    properties.sort(Comparator.comparingDouble(p -> p.getNormal_price()));

                                    onSuccess.onSuccess(properties);
                                }
                            }
                        }
                    },
                    exception -> {
                        if (!hasError.getAndSet(true)) {
                            // Chỉ gọi onFailure một lần
                            onFailure.onFailure(exception);
                        }
                    }
            );
        }
    }

    public void getPropertySortedByPriceDesc(List<String> property_ids, OnSuccessListener<List<Property>> onSuccess, OnFailureListener onFailure) {
        if (property_ids == null || property_ids.isEmpty()) {
            onFailure.onFailure(new Exception("Property IDs list is empty"));
            return;
        }

        List<Property> properties = new ArrayList<>();
        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicBoolean hasError = new AtomicBoolean(false);

        for (String id : property_ids) {
            getPropertyById(id,
                    property -> {
                        synchronized (properties) {
                            if (!hasError.get()) {
                                property.id = (id); // Set ID cho property
                                properties.add(property);

                                // Kiểm tra xem đã lấy hết tất cả property chưa
                                if (completedCount.incrementAndGet() == property_ids.size()) {
                                    // Sắp xếp theo giá giảm dần
                                    properties.sort((p1, p2) ->
                                            Double.compare(p2.getNormal_price(), p1.getNormal_price()));

                                    onSuccess.onSuccess(properties);
                                }
                            }
                        }
                    },
                    exception -> {
                        if (!hasError.getAndSet(true)) {
                            // Chỉ gọi onFailure một lần
                            onFailure.onFailure(exception);
                        }
                    }
            );
        }
    }

    public void getPropertySortedByRating(List<String> property_ids, OnSuccessListener<List<Property>> onSuccess, OnFailureListener onFailure) {
        if (property_ids == null || property_ids.isEmpty()) {
            onFailure.onFailure(new Exception("Property IDs list is empty"));
            return;
        }

        List<Property> properties = new ArrayList<>();
        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicBoolean hasError = new AtomicBoolean(false);

        for (String id : property_ids) {
            getPropertyById(id,
                    property -> {
                        synchronized (properties) {
                            if (!hasError.get()) {
                                property.id = (id); // Set ID cho property
                                properties.add(property);

                                // Kiểm tra xem đã lấy hết tất cả property chưa
                                if (completedCount.incrementAndGet() == property_ids.size()) {
                                    // Sắp xếp theo ratings giảm dần
                                    properties.sort((p1, p2) ->
                                            Double.compare(p2.avg_ratings, p1.avg_ratings));

                                    onSuccess.onSuccess(properties);
                                }
                            }
                        }
                    },
                    exception -> {
                        if (!hasError.getAndSet(true)) {
                            // Chỉ gọi onFailure một lần
                            onFailure.onFailure(exception);
                        }
                    }
            );
        }
    }
}
