package com.healthcare.medVault.model;

import com.healthcare.medVault.enums.Intensity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "patient_history")
@Data
public class PatientHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    private String problem;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private Intensity intensity;

    @Column(length = 1000)
    private String notes;
}

