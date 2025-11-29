package com.healthcare.medVault.repository;

import com.healthcare.medVault.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientRepo extends JpaRepository<Patient, Long> {
    List<Patient> findByNameContainingIgnoreCase(String name);
    Patient findByUserId(Long userId);

}
