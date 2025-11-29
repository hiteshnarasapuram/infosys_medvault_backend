package com.healthcare.medVault.model;

import com.healthcare.medVault.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="patients")
@Data
public class Patient {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String dob;
    private String gender;
    private String phone;

    private String address;

    private String emergencyContactPhone;

    private String governmentIdProofLink;
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;


    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}


