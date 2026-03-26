package com.metkring.lapmartadmin.model;

import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;
import java.util.List;

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

    public Product() {
    }

    public Product(
            String brand, String description, String gpu, List<String> imageUrls, String model,
            double buyingPrice, double price, String processor, int qty, String ram, String storage,
            long timestamp
    ) {
        this.brand = brand;
        this.description = description;
        this.gpu = gpu;
        this.imageUrls = imageUrls;
        this.model = model;
        this.buyingPrice = buyingPrice;
        this.price = price;
        this.processor = processor;
        this.qty = qty;
        this.ram = ram;
        this.storage = storage;
        this.timestamp = timestamp;
    }


    public double getBuyingPrice() {
        return buyingPrice;
    }

    public void setBuyingPrice(double buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGpu() {
        return gpu;
    }

    public void setGpu(String gpu) {
        this.gpu = gpu;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}