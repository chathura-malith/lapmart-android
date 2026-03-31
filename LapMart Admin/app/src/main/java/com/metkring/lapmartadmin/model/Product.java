package com.metkring.lapmartadmin.model;

import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product implements Serializable {
    @DocumentId
    private String productId;

    private String brand;
    private String description;
    private String gpu;
    private List<String> imageUrls;
    private String model;
    private double buyingPrice;
    private double price;
    private String processor;
    private int qty;
    private String ram;
    private String storage;
    private long timestamp;
}