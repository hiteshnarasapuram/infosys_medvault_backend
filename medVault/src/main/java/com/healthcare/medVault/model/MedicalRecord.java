package com.healthcare.medVault.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name="medical_records")
@Data
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String fileLink;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

}

