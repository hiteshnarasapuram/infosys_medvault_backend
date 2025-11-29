package com.healthcare.medVault.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@AllArgsConstructor
public class MedicalRecordViewResponse {
    private Resource resource;
    private String filename;
}

