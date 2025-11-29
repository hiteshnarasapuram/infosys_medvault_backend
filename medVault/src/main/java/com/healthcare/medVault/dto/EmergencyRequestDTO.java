package com.healthcare.medVault.dto;

import com.healthcare.medVault.enums.Intensity;
import lombok.Data;

@Data
public class EmergencyRequestDTO {
    private String problem;
    private Intensity intensity;
    private String message;
    private String location;

}
