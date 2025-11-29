package com.healthcare.medVault.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="admins")
@Data
public class Admin {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
