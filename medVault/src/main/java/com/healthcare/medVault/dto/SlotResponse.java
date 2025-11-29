package com.healthcare.medVault.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SlotResponse {
    private Long id;
    private String slotDate;
    private String startTime;
}
