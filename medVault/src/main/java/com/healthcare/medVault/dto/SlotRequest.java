package com.healthcare.medVault.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SlotRequest {
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int slotIntervalMinutes = 30;


}
