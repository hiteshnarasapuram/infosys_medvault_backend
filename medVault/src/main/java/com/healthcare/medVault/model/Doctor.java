package com.healthcare.medVault.model;

import com.healthcare.medVault.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="doctors")
@Data
public class Doctor {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String dob;
    private String gender;
    private String phone;
    private String email;
    private String address;

    private String specialization;
    @Column(nullable = false)
    private Double consultationFees = 100.0;
    private String hospital;
    @Column(nullable = false)
    private Integer experience = 0;

    private String profilePicLink;
    private String doctorCertificateLink;
    private String governmentIdProofLink;


    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;
    @Column(length = 1000)
    private String adminMessage;



    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
