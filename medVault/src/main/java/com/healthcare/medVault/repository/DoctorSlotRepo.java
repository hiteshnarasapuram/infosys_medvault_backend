package com.healthcare.medVault.repository;

import com.healthcare.medVault.enums.SlotStatus;
import com.healthcare.medVault.model.DoctorSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface DoctorSlotRepo extends JpaRepository<DoctorSlot, Long> {
    List<DoctorSlot> findByDoctorId(Long doctorId);
    Optional<DoctorSlot> findByDoctorIdAndSlotDateAndSlotTime(Long doctorId, LocalDate slotDate, LocalTime slotTime);

    List<DoctorSlot> findByDoctorIdAndStatus(Long id, SlotStatus slotStatus);

    List<DoctorSlot> findByDoctorIdAndSlotDateAndStatus(Long id, LocalDate date, SlotStatus slotStatus);

    List<DoctorSlot> findByDoctorIdAndSlotDateGreaterThanEqualAndStatusOrderBySlotDateAscSlotTimeAsc(Long doctorId, LocalDate slotDate, SlotStatus slotStatus);
}