package com.healthcare.medVault.repository;

import com.healthcare.medVault.enums.ApprovalStatus;
import com.healthcare.medVault.model.Doctor;
import com.healthcare.medVault.model.MedicalRecord;
import com.healthcare.medVault.model.MedicalRecordAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MedicalRecordAccessRepo extends JpaRepository<MedicalRecordAccess, Long> {

    List<MedicalRecordAccess> findByMedicalRecord_PatientIdAndDoctorIdAndStatusAndExpiryAfter(Long patientId, Long id, ApprovalStatus approvalStatus, LocalDateTime now);

    List<MedicalRecordAccess> findByMedicalRecord_Patient_IdAndStatus(Long id, ApprovalStatus approvalStatus);

    Optional<MedicalRecordAccess> findByMedicalRecord_IdAndDoctorIdAndStatusAndExpiryAfter(
            Long recordId,
            Long doctorId,
            ApprovalStatus status,
            LocalDateTime expiry
    );

    List<MedicalRecordAccess> findByMedicalRecord(MedicalRecord record);

    boolean existsByMedicalRecordAndDoctor(MedicalRecord record, Doctor doctor);

    boolean existsByMedicalRecordAndDoctorAndStatusInAndExpiryAfter(MedicalRecord record, Doctor doctor, List<ApprovalStatus> approved, LocalDateTime now);

    boolean existsByMedicalRecordAndDoctorAndStatusIn(MedicalRecord record, Doctor doctor, List<ApprovalStatus> approved);

    List<MedicalRecordAccess> findByDoctorAndMedicalRecord_PatientIdAndStatusIn(Doctor doctor, Long patientId, List<ApprovalStatus> approved);

    List<MedicalRecordAccess> findByDoctorAndMedicalRecord_PatientId(Doctor doctor, Long patientId);
}