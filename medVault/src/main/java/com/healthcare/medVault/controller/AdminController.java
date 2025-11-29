package com.healthcare.medVault.controller;

import com.healthcare.medVault.dto.AppointmentDTO;
import com.healthcare.medVault.model.*;
import com.healthcare.medVault.service.implementation.AdminServiceImpl;
import com.healthcare.medVault.service.interfaces.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/add/patient")
    public ResponseEntity<?> addPatient(@RequestBody Patient patient) {
        Patient saved = adminService.addPatient(patient);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/add/doctor")
    public ResponseEntity<?> addDoctor(@RequestBody Doctor doctor) {
        Doctor saved = adminService.addDoctor(doctor);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/update/user/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody User updatedUser) {
        User user = adminService.updateUser(userId, updatedUser);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update/patient/{patientId}")
    public ResponseEntity<?> updatePatient(@PathVariable Long patientId, @RequestBody Patient updatedPatient) {
        Patient patient = adminService.updatePatient(patientId, updatedPatient);
        return ResponseEntity.ok(patient);
    }

    @PutMapping("/update/doctor/{doctorId}")
    public ResponseEntity<?> updateDoctor(@PathVariable Long doctorId, @RequestBody Doctor updatedDoctor) {
        Doctor doctor = adminService.updateDoctor(doctorId, updatedDoctor);
        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        List<Doctor> doctors = adminService.getAllDoctors();
        return doctors.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(doctors);
    }

    @GetMapping("/patients")
    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = adminService.getAllPatients();
        return patients.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(patients);
    }

    @DeleteMapping("/delete/doctor/{doctorId}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long doctorId) {
        adminService.deleteDoctor(doctorId);
        return ResponseEntity.ok("Doctor deleted successfully");
    }

    @DeleteMapping("/delete/patient/{patientId}")
    public ResponseEntity<?> deletePatient(@PathVariable Long patientId) {
        adminService.deletePatient(patientId);
        return ResponseEntity.ok("Patient deleted successfully");
    }

    @GetMapping("/search/patients")
    public ResponseEntity<List<Patient>> searchPatients(@RequestParam String name) {
        List<Patient> patients = adminService.searchPatientsByName(name);
        return patients.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(patients);
    }

    @GetMapping("/search/doctors")
    public ResponseEntity<List<Doctor>> searchDoctors(@RequestParam String name) {
        List<Doctor> doctors = adminService.searchDoctorsByName(name);
        return doctors.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(doctors);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PendingRegistration>> getAllPendingRegistrations() {
        List<PendingRegistration> pending = adminService.getAllPendingRegistrations();
        return pending.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(pending);
    }

    @PostMapping("/register/approve/{id}")
    public ResponseEntity<?> approveRegistration(@PathVariable Long id) {
        adminService.approveRegistration(id);
        return ResponseEntity.ok("Registration approved successfully");
    }

    @DeleteMapping("/register/reject/{id}")
    public ResponseEntity<?> rejectRegistration(@PathVariable Long id) {
        adminService.rejectRegistration(id);
        return ResponseEntity.ok("Registration rejected successfully");
    }

    @GetMapping("/logs")
    public ResponseEntity<List<LogData>> getAllLogs() {
        List<LogData> logs = adminService.getLogData();
        return logs.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(logs);
    }

    @GetMapping("/doctors/pending")
    public ResponseEntity<List<Doctor>> getPendingDoctors() {
        List<Doctor> pending = adminService.getPendingDoctors();
        return pending.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(pending);
    }

    @PostMapping("/doctors/approve/{id}")
    public ResponseEntity<?> approveDoctor(@PathVariable Long id, @RequestParam String message) {
        Doctor doctor = adminService.approveDoctor(id, message);
        return ResponseEntity.ok(doctor);
    }

    @PostMapping("/doctors/reject/{id}")
    public ResponseEntity<?> rejectDoctor(@PathVariable Long id, @RequestParam String message) {
        Doctor doctor = adminService.rejectDoctor(id, message);
        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/doctors/certificate/view/{doctorId}/{type}")
    public ResponseEntity<Resource> viewDoctorDocument(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long doctorId,
            @PathVariable String type) throws MalformedURLException {
        return adminService.viewDoctorDocument(authHeader, doctorId, type);
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        List<AppointmentDTO> appointments = adminService.getAllAppointments();
        return appointments.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(appointments);
    }
}
