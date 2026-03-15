package com.metkring.lapmart.model;

public class Product {
    private String name;
    private double price;
    private int imageRes; // දැනට drawable එකේ තියෙන පින්තූරයක් පාවිච්චි කරමු

    public Product(String name, double price, int imageRes) {
        this.name = name;
        this.price = price;
        this.imageRes = imageRes;
    }

    public String getName() { return name; }
    public String getFormattedPrice() { return "Rs." + String.format("%.2f", price); }
    public int getImageRes() { return imageRes; }
}
