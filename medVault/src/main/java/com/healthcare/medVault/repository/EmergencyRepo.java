package com.healthcare.medVault.repository;

import com.healthcare.medVault.model.Doctor;
import com.healthcare.medVault.model.EmergencyRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface EmergencyRepo extends JpaRepository<EmergencyRequest, Long> {


    List<EmergencyRequest> findByStatusTrueAndDoctorIsNullOrderByCreatedAtDesc();

    List<EmergencyRequest> findByPatientId(Long id);

    List<EmergencyRequest> findByDoctorOrderByCreatedAtDesc(Doctor doctor);
}
