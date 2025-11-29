package com.healthcare.medVault.repository;

import com.healthcare.medVault.enums.ApprovalStatus;
import com.healthcare.medVault.model.DoctorHistoryAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface DoctorHistoryAccessRepo extends JpaRepository<DoctorHistoryAccess, Long> {


    List<DoctorHistoryAccess> findByPatientIdAndStatus(Long id, ApprovalStatus approvalStatus);

    List<DoctorHistoryAccess> findByDoctorIdAndStatus(Long id, ApprovalStatus approvalStatus);

    Optional<DoctorHistoryAccess> findByDoctorIdAndPatientId(Long id, Long patientId);

    Optional<DoctorHistoryAccess> findByDoctorIdAndPatientIdAndStatusAndExpiryAfter(
            Long doctorId, Long patientId, ApprovalStatus status, LocalDateTime now
    );

    Optional<DoctorHistoryAccess> findByDoctorIdAndPatientIdAndStatus(Long id, Long patientId, ApprovalStatus approvalStatus);
}
