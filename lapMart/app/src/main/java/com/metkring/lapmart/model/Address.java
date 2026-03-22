package com.metkring.lapmart.model;

import java.io.Serializable;

public class Address implements Serializable {
    private String fullName;
    private String email;
    private String contactNo;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postCode;

    public Address() {
    }

    public Address(String fullName, String email, String contactNo, String addressLine1, String addressLine2, String city, String postCode) {
        this.fullName = fullName;
        this.email = email;
        this.contactNo = contactNo;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.postCode = postCode;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostCode() { return postCode; }
    public void setPostCode(String postCode) { this.postCode = postCode; }
}