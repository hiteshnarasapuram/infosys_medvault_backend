package com.healthcare.medVault.repository;

import com.healthcare.medVault.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepo extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatientId(Long id);
}