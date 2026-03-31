package com.metkring.lapmartadmin.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address implements Serializable {
    private String fullName;
    private String email;
    private String contactNo;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postCode;
}