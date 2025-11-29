package com.healthcare.medVault.dto;

import com.healthcare.medVault.enums.Intensity;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientHistoryRequestDTO {
    private String problem;
    private LocalDate date;
    private Intensity intensity;
    private String notes;
}
