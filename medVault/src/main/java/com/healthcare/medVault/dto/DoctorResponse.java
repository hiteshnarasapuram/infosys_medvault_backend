package com.healthcare.medVault.dto;

import lombok.Data;

@Data
public class DoctorResponse {
    private String name;
    private String dob;
    private String gender;
    private String phone;
    private String address;
    private String specialization;
    private Double consultationFees;
    private String hospital;
    private int experience;
    private String doctorCertificateLink;
    private String governmentIdProofLink;
    private String profilePicLink;
}
