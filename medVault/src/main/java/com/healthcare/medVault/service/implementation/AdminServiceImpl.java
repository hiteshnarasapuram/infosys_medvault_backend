package com.healthcare.medVault.service.implementation;

import com.healthcare.medVault.repository.*;
import com.healthcare.medVault.dto.AppointmentDTO;
import com.healthcare.medVault.enums.ApprovalStatus;
import com.healthcare.medVault.enums.Role;
import com.healthcare.medVault.model.*;
import com.healthcare.medVault.service.interfaces.AdminService;
import com.healthcare.medVault.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private PendingRegistrationRepo pendingRepo;

    @Autowired
    private AuditService auditService;

    @Autowired
    private LogDataRepo logDataRepo;

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private JavaMailSender mailSender;

    // Add Patient
    @Override
    public Patient addPatient(Patient patient) {
        User user = new User();
        user.setEmail(patient.getUser().getEmail());
        user.setPassword(patient.getUser().getPassword());
        user.setRole(Role.PATIENT);
        user.setFirstLogin(true);
        userRepo.save(user);

        patient.setUser(user);
        Patient savedPatient = patientRepo.save(patient);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Your MedVault Account Created");
            message.setText("Hello " + patient.getName() + ",\n\n" +
                    "Your account has been created.\n" +
                    "Email: " + user.getEmail() + "\n" +
                    "Password: " + user.getPassword() + "\n\n" +
                    "Please login and change your password.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }

        auditService.logAction(savedPatient.getName(), user.getEmail(), "Patient added");
        return savedPatient;
    }

    // Add Doctor
    @Override
    public Doctor addDoctor(Doctor doctor) {
        User user = new User();
        user.setEmail(doctor.getUser().getEmail());
        user.setPassword(doctor.getUser().getPassword());
        user.setRole(Role.DOCTOR);
        user.setFirstLogin(true);
        userRepo.save(user);

        doctor.setUser(user);
        Doctor savedDoctor = doctorRepo.save(doctor);

                try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Your MedVault Account Created");
            message.setText("Hello " + doctor.getName() + ",\n\n" +
                    "Your account has been created.\n" +
                    "Email: " + user.getEmail() + "\n" +
                    "Password: " + user.getPassword() + "\n\n" +
                    "Please login and change your password.");
            mailSender.send(message);
        } catch (Exception e) {
             System.err.println("Failed to send email: " + e.getMessage());
             e.printStackTrace();
        }

        auditService.logAction(savedDoctor.getName(), user.getEmail(), "Doctor added");
        return savedDoctor;
    }

    // Update User
    @Override
    public User updateUser(Long userId, User updatedUser) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updatedUser.getEmail() != null) user.setEmail(updatedUser.getEmail());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank())
            user.setPassword(updatedUser.getPassword());
        if (updatedUser.getRole() != null) user.setRole(updatedUser.getRole());
        user.setFirstLogin(updatedUser.isFirstLogin());

        return userRepo.save(user);
    }

    // Update Patient
    @Override
    public Patient updatePatient(Long patientId, Patient updatedPatient) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (updatedPatient.getName() != null) patient.setName(updatedPatient.getName());
        if (updatedPatient.getDob() != null) patient.setDob(updatedPatient.getDob());
        if (updatedPatient.getGender() != null) patient.setGender(updatedPatient.getGender());
        if (updatedPatient.getPhone() != null) patient.setPhone(updatedPatient.getPhone());

        if (updatedPatient.getUser() != null) {
            if (updatedPatient.getUser().getEmail() != null)
                patient.getUser().setEmail(updatedPatient.getUser().getEmail());

            if (updatedPatient.getUser().getPassword() != null &&
                    !updatedPatient.getUser().getPassword().isBlank())
                patient.getUser().setPassword(updatedPatient.getUser().getPassword());
        }

        Patient savedPatient = patientRepo.save(patient);

                try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(patient.getUser().getEmail());
            message.setSubject("Your account has been updated");
            message.setText("Hello " + patient.getName() + ",\n\nYour account details have been updated successfully.");
            mailSender.send(message);
        } catch (Exception e) {
               System.err.println("Failed to send email: " + e.getMessage());
               e.printStackTrace();
        }
        auditService.logAction(savedPatient.getName(), savedPatient.getUser().getEmail(), "Patient updated");
        return savedPatient;
    }

    // Update Doctor
    @Override
    public Doctor updateDoctor(Long doctorId, Doctor updatedDoctor) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (updatedDoctor.getName() != null) doctor.setName(updatedDoctor.getName());
        if (updatedDoctor.getDob() != null) doctor.setDob(updatedDoctor.getDob());
        if (updatedDoctor.getGender() != null) doctor.setGender(updatedDoctor.getGender());
        if (updatedDoctor.getPhone() != null) doctor.setPhone(updatedDoctor.getPhone());
        if (updatedDoctor.getSpecialization() != null) doctor.setSpecialization(updatedDoctor.getSpecialization());

        if (updatedDoctor.getUser() != null) {
            if (updatedDoctor.getUser().getEmail() != null)
                doctor.getUser().setEmail(updatedDoctor.getUser().getEmail());

            if (updatedDoctor.getUser().getPassword() != null &&
                    !updatedDoctor.getUser().getPassword().isBlank())
                doctor.getUser().setPassword(updatedDoctor.getUser().getPassword());
        }

        Doctor savedDoctor = doctorRepo.save(doctor);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(doctor.getUser().getEmail());
            message.setSubject("Your account has been updated");
            message.setText("Hello " + doctor.getName() + ",\n\nYour account details have been updated successfully.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
        auditService.logAction(savedDoctor.getName(), savedDoctor.getUser().getEmail(), "Doctor updated");
        return savedDoctor;
    }

    // Get all Doctors
    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepo.findAll();
    }

    // Get all Patients
    @Override
    public List<Patient> getAllPatients() {
        return patientRepo.findAll();
    }

    // Delete Doctor
    @Override
    public void deleteDoctor(Long doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        userRepo.delete(doctor.getUser());
        doctorRepo.delete(doctor);
    }

    // Delete Patient
    @Override
    public void deletePatient(Long patientId) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        userRepo.delete(patient.getUser());
        patientRepo.delete(patient);
    }

    // Search Patients
    @Override
    public List<Patient> searchPatientsByName(String name) {
        return patientRepo.findByNameContainingIgnoreCase(name);
    }

    // Search Doctors
    @Override
    public List<Doctor> searchDoctorsByName(String name) {
        return doctorRepo.findByNameContainingIgnoreCase(name);
    }

    // Pending Registrations
    @Override
    public List<PendingRegistration> getAllPendingRegistrations() {
        return pendingRepo.findAll();
    }

    // Approve Registration
    @Override
    public void approveRegistration(Long id) {
        PendingRegistration pending = pendingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration request not found"));

        User user = new User();
        user.setEmail(pending.getEmail());
        user.setPassword(pending.getPassword());
        user.setRole(Role.valueOf(pending.getRole().toUpperCase()));
        user.setFirstLogin(true);
        userRepo.save(user);

        if (pending.getRole().equalsIgnoreCase("PATIENT")) {
            Patient patient = new Patient();
            patient.setName(pending.getName());
            patient.setDob(pending.getDob());
            patient.setGender(pending.getGender());
            patient.setPhone(pending.getPhone());
            patient.setUser(user);
            patientRepo.save(patient);
        } else if (pending.getRole().equalsIgnoreCase("DOCTOR")) {
            Doctor doctor = new Doctor();
            doctor.setName(pending.getName());
            doctor.setDob(pending.getDob());
            doctor.setGender(pending.getGender());
            doctor.setPhone(pending.getPhone());
            doctor.setSpecialization(pending.getSpecialization());
            doctor.setUser(user);
            doctorRepo.save(doctor);
        }

        pendingRepo.delete(pending);
    }

    // Reject Registration
    @Override
    public void rejectRegistration(Long id) {
        PendingRegistration pending = pendingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration request not found"));
        pendingRepo.delete(pending);
    }

    // Get Logs
    @Override
    public List<LogData> getLogData() {
        return logDataRepo.findAllByOrderByTimestampDesc();
    }

    // Get Pending Doctors
    @Override
    public List<Doctor> getPendingDoctors() {
        return doctorRepo.findByApprovalStatus(ApprovalStatus.PENDING);
    }

    // Approve Doctor
    @Override
    public Doctor approveDoctor(Long id, String messageText) {
        Doctor doctor = doctorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctor.setApprovalStatus(ApprovalStatus.APPROVED);
        doctor.setAdminMessage("APPROVED: " + messageText);
        return doctorRepo.save(doctor);
    }

    // Reject Doctor
    @Override
    public Doctor rejectDoctor(Long id, String messageText) {
        Doctor doctor = doctorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctor.setApprovalStatus(ApprovalStatus.REJECTED);
        doctor.setAdminMessage("REJECTED: " + messageText);
        return doctorRepo.save(doctor);
    }

    @Override
    public ResponseEntity<Resource> viewDoctorDocument(String authHeader, Long doctorId, String type) throws MalformedURLException {
        Doctor doctor = doctorRepo.findById(doctorId).orElse(null);
        if (doctor == null) {
            return ResponseEntity.notFound().build();
        }

        String filePath = null;
        if ("doctor_certificate".equals(type)) {
            filePath = doctor.getDoctorCertificateLink();
        } else if ("government_id".equals(type)) {
            filePath = doctor.getGovernmentIdProofLink();
        }

        if (filePath == null || filePath.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(file.toURI());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }


    // Get All Appointments
    @Override
    public List<AppointmentDTO> getAllAppointments() {
        List<Appointment> appointments = appointmentRepo.findAll();
        return appointments.stream()
                .map(appt -> new AppointmentDTO(
                        appt.getId(),
                        appt.getSlot().getSlotDate().toString(),
                        appt.getStatus()
                ))
                .toList();
    }
}

