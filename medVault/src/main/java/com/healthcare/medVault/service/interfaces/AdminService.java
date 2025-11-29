package com.healthcare.medVault.service.interfaces;

import com.healthcare.medVault.dto.AppointmentDTO;
import com.healthcare.medVault.model.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.net.MalformedURLException;
import java.util.List;

public interface AdminService {


    // Add Patient
    Patient addPatient(Patient patient);

    // Add Doctor
    Doctor addDoctor(Doctor doctor);

    // Update User
    User updateUser(Long userId, User updatedUser);

    // Update Patient
    Patient updatePatient(Long patientId, Patient updatedPatient);

    // Update Doctor
    Doctor updateDoctor(Long doctorId, Doctor updatedDoctor);

    // Get all Doctors
    List<Doctor> getAllDoctors();

    // Get all Patients
    List<Patient> getAllPatients();

    // Delete Doctor
    void deleteDoctor(Long doctorId);

    // Delete Patient
    void deletePatient(Long patientId);

    // Search Patients
    List<Patient> searchPatientsByName(String name);

    // Search Doctors
    List<Doctor> searchDoctorsByName(String name);

    // Pending Registrations
    List<PendingRegistration> getAllPendingRegistrations();

    // Approve Registration
    void approveRegistration(Long id);

    // Reject Registration
    void rejectRegistration(Long id);

    // Get Logs
    List<LogData> getLogData();

    // Get Pending Doctors
    List<Doctor> getPendingDoctors();

    // Approve Doctor
    Doctor approveDoctor(Long id, String messageText);

    // Reject Doctor
    Doctor rejectDoctor(Long id, String messageText);

    ResponseEntity<Resource> viewDoctorDocument(String authHeader, Long doctorId, String type) throws MalformedURLException;

    // Get All Appointments
    List<AppointmentDTO> getAllAppointments();
}
