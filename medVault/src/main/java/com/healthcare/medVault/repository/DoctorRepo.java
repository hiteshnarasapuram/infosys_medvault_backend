package com.healthcare.medVault.repository;

import com.healthcare.medVault.enums.ApprovalStatus;
import com.healthcare.medVault.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepo extends JpaRepository<Doctor, Long> {
    List<Doctor> findByNameContainingIgnoreCase(String name);
    Optional<Doctor> findByUserId(Long userId);

    List<Doctor> findByApprovalStatus(ApprovalStatus approvalStatus);

    List<Doctor> findByApprovalStatusAndNameContainingIgnoreCase(ApprovalStatus approvalStatus, String keyword);
}
