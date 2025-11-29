package com.healthcare.medVault.dto;

import com.healthcare.medVault.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppointmentDTO {
    private Long id;
    private String slotDate;
    private AppointmentStatus status;
}
