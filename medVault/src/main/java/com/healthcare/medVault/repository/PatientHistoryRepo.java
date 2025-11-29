package com.healthcare.medVault.repository;

import com.healthcare.medVault.model.PatientHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientHistoryRepo extends JpaRepository<PatientHistory, Long> {
    List<PatientHistory> findByPatientIdOrderByDateDesc(Long patientId);

    List<PatientHistory> findByPatientId(Long patientId);
}

