package com.healthcare.medVault.repository;

import com.healthcare.medVault.enums.AppointmentStatus;
import com.healthcare.medVault.model.Appointment;
import com.healthcare.medVault.model.DoctorSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface AppointmentRepo extends JpaRepository<Appointment, Long> {

    List<Appointment> findBySlot_Doctor_Id(Long doctorId);
    List<Appointment> findBySlot_Doctor_IdAndSlot_SlotDate(Long doctorId, LocalDate date);
    boolean existsBySlotId(Long slotId);

    List<Appointment> findBySlot_Doctor_IdAndStatus(Long id, AppointmentStatus appointmentStatus);

    List<Appointment> findByPatientIdAndStatus(Long id, AppointmentStatus appointmentStatus);

    boolean existsByPatientIdAndSlot_SlotDateAndStatusIn(Long id, LocalDate slotDate, List<AppointmentStatus> list);

    List<Appointment> findByPatientIdOrderBySlot_SlotDateDescSlot_SlotTimeDesc(Long id);

    List<Appointment> findByPatientId(Long id);

    List<Appointment> findBySlot(DoctorSlot slot);

    List<Appointment> findBySlot_DoctorIdAndStatus(Long id, AppointmentStatus appointmentStatus);

}

