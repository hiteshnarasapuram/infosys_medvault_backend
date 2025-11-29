package com.healthcare.medVault.dto;

import com.healthcare.medVault.model.Doctor;
import com.healthcare.medVault.model.Patient;
import lombok.Data;


@Data
public class DashboardResponse {
    private boolean firstLogin;
    private String message;
    private Doctor doctor;
    private Patient patient;
}

