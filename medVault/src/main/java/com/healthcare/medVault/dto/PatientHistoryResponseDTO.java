package com.healthcare.medVault.dto;

import com.healthcare.medVault.enums.Intensity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientHistoryResponseDTO {
    private Long id;
    private String problem;
    private LocalDate date;
    private Intensity intensity;
    private String notes;
}
