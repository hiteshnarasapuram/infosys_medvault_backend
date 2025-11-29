package com.healthcare.medVault.model;

import com.healthcare.medVault.enums.Intensity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class EmergencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Patient patient;

    @ManyToOne
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    private Intensity intensity;

    private String problem;

    private String message;

    private String location;

    private LocalDateTime createdAt;

    private Boolean status = true;
}
