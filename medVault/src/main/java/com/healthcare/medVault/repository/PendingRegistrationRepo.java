package com.healthcare.medVault.repository;

import com.healthcare.medVault.model.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingRegistrationRepo extends JpaRepository<PendingRegistration, Long> {
    boolean existsByEmail(String email);
}
