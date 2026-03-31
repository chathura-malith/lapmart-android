package com.metkring.lapmart.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product implements Serializable {
    private String id;
    private String brand;
    private String description;
    private String gpu;
    private List<String> imageUrls;
    private String model;
    private double price;
    private String processor;
    private int qty;
    private String ram;
    private String storage;
}