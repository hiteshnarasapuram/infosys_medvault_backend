package com.healthcare.medVault.model;

import com.healthcare.medVault.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "appointments")
@Data
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "slot_id", nullable = false)
    private DoctorSlot slot;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(length = 2000)
    private String feedback;

    private Integer rating;

    private boolean rescheduled = false;

    private String rescheduleReason;



}
