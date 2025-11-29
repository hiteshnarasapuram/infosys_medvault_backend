package com.healthcare.medVault.model;

import com.healthcare.medVault.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_history_access")
@Data
public class DoctorHistoryAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime expiry;
}
