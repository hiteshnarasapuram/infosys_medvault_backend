package com.healthcare.medVault.dto;


import lombok.Data;

@Data
public class PatientResponse {
    private String name;
    private String dob;
    private String gender;
    private String phone;
    private String address;
    private String governmentIdProofLink;
    private String emergencyContactPhone;

}
