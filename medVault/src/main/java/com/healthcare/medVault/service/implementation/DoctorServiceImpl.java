package com.healthcare.medVault.service.implementation;

import com.healthcare.medVault.repository.*;
import com.healthcare.medVault.dto.DashboardResponse;
import com.healthcare.medVault.dto.DoctorResponse;
import com.healthcare.medVault.dto.MedicalRecordViewResponse;
import com.healthcare.medVault.dto.SlotRequest;
import com.healthcare.medVault.enums.AppointmentStatus;
import com.healthcare.medVault.enums.ApprovalStatus;
import com.healthcare.medVault.enums.SlotStatus;
import com.healthcare.medVault.model.*;
import com.healthcare.medVault.service.interfaces.DoctorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static java.util.Arrays.stream;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private DoctorSlotRepo doctorSlotRepo;

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MedicalRecordRepo medicalRecordRepo;

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private MedicalRecordAccessRepo medicalRecordAccessRepo;

    @Autowired
    private DoctorHistoryAccessRepo doctorHistoryAccessRepo;

    @Autowired
    private PatientHistoryRepo patientHistoryRepo;

    @Autowired
    private EmergencyRepo emergencyRepo;


    @Override
    public DashboardResponse getDashboard(HttpServletRequest request) {

        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        DashboardResponse response = new DashboardResponse();

        if (user.isFirstLogin()) {
            response.setFirstLogin(true);
            response.setMessage("First login detected. Please set a new password.");
        } else {
            response.setFirstLogin(false);
            response.setMessage("Welcome " + user.getEmail());
            Doctor doctor = doctorRepo.findByUserId(user.getId()).orElse(null);
            response.setDoctor(doctor);
        }

        return response;
    }

    @Override
    public Map<String, Object> setPassword(HttpServletRequest request, String newPassword) {

        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!user.isFirstLogin()) {
            throw new RuntimeException("Password already set");
        }

        user.setPassword(newPassword);
        user.setFirstLogin(false);
        userRepo.save(user);

        return Map.of("message", "Password set successfully! Please login again.");
    }

    @Override
    public DoctorResponse getProfile(HttpServletRequest request) {

        if (request.getUserPrincipal() == null) {
            throw new RuntimeException("Unauthenticated");
        }

        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Doctor doctor = doctorRepo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        DoctorResponse dto = new DoctorResponse();
        dto.setName(doctor.getName());
        dto.setDob(doctor.getDob());
        dto.setGender(doctor.getGender());
        dto.setPhone(doctor.getPhone());
        dto.setAddress(doctor.getAddress());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setConsultationFees(doctor.getConsultationFees());
        dto.setHospital(doctor.getHospital());
        dto.setExperience(doctor.getExperience());
        dto.setDoctorCertificateLink(doctor.getDoctorCertificateLink());
        dto.setGovernmentIdProofLink(doctor.getGovernmentIdProofLink());

        String picPath = doctor.getProfilePicLink();
        dto.setProfilePicLink(
                (picPath == null || picPath.isEmpty())
                        ? "/api/doctor/file/defaults/default_profile.png"
                        : "/api/doctor/file/" + doctor.getId() + "/profile_pic"
        );

        return dto;
    }

    @Override
    public String updateProfile(HttpServletRequest request,
                                DoctorResponse updatedDto,
                                MultipartFile doctorCertificate,
                                MultipartFile governmentId) {

        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Doctor doctor = doctorRepo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        saveprofile(updatedDto, doctor);

        try {
            if (doctorCertificate != null) {
                String doctorCertLink = saveFile(doctorCertificate, doctor.getId(), "doctor_certificate");
                doctor.setDoctorCertificateLink(doctorCertLink);
            }

            if (governmentId != null) {
                String govIdLink = saveFile(governmentId, doctor.getId(), "government_id");
                doctor.setGovernmentIdProofLink(govIdLink);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving files: " + e.getMessage());
        }

        if (doctor.getApprovalStatus() != ApprovalStatus.APPROVED) {
            doctor.setApprovalStatus(ApprovalStatus.PENDING);
        }

        doctorRepo.save(doctor);

        return "Doctor profile updated successfully.";
    }



    private void saveprofile(DoctorResponse updatedDto, Doctor doctor) {
        doctor.setName(updatedDto.getName() != null ? updatedDto.getName() : doctor.getName());
        doctor.setDob(updatedDto.getDob() != null ? updatedDto.getDob() : doctor.getDob());
        doctor.setGender(updatedDto.getGender() != null ? updatedDto.getGender() : doctor.getGender());
        doctor.setPhone(updatedDto.getPhone() != null ? updatedDto.getPhone() : doctor.getPhone());
        doctor.setAddress(updatedDto.getAddress() != null ? updatedDto.getAddress() : doctor.getAddress());
        doctor.setSpecialization(updatedDto.getSpecialization() != null ? updatedDto.getSpecialization() : doctor.getSpecialization());
        doctor.setConsultationFees(updatedDto.getConsultationFees() != null ? updatedDto.getConsultationFees() : doctor.getConsultationFees());
        doctor.setHospital(updatedDto.getHospital() != null ? updatedDto.getHospital() : doctor.getHospital());
        doctor.setExperience(updatedDto.getExperience() != 0 ? updatedDto.getExperience() : doctor.getExperience());
    }



    private String saveFile(MultipartFile file, Long doctorId, String type) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String uploadDir = "D:/medvault/uploads/doctors/" + doctorId;
        File dir = new File(uploadDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create upload directory: " + uploadDir);
        }

        String originalExtension = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf("."));
        String filename = type + "_" + System.currentTimeMillis() + originalExtension;

        File destFile = new File(dir, filename);
        file.transferTo(destFile);

        return destFile.getAbsolutePath();
    }


    @Override
    public Map<String, Object> checkProfileCompletion(HttpServletRequest request) {

        String email = request.getUserPrincipal().getName();
        Doctor doctor = doctorRepo.findByUserId(
                userRepo.findByEmailIgnoreCase(email).getId()
        ).orElse(null);

        if (doctor == null) {
            throw new RuntimeException("Doctor not found");
        }

        Map<String, Object> response = new HashMap<>();

        boolean isComplete = isIsComplete(doctor);

        if (!isComplete) {
            response.put("status", "INCOMPLETE");
            response.put("message", "Profile is incomplete. Please update all required fields.");
            return response;
        }

        switch (doctor.getApprovalStatus()) {
            case PENDING:
                response.put("status", "PENDING");
                response.put("message", "Your profile is under verification.");
                break;

            case REJECTED:
                response.put("status", "REJECTED");
                response.put("message", "Your profile has been rejected.");
                response.put("adminMessage", doctor.getAdminMessage());
                break;

            case APPROVED:
                response.put("status", "APPROVED");
                response.put("message", "Profile is complete and approved.");
                break;

            default:
                response.put("status", "UNKNOWN");
                response.put("message", "Unexpected status. Contact support.");
        }

        return response;
    }

    private boolean isIsComplete(Doctor doctor) {
        boolean isComplete =
                doctor.getName() != null &&
                        doctor.getDob() != null &&
                        doctor.getGender() != null &&
                        doctor.getPhone() != null &&
                        doctor.getAddress() != null &&
                        doctor.getSpecialization() != null &&
                        doctor.getHospital() != null &&
                        doctor.getExperience() > 0 &&
                        doctor.getDoctorCertificateLink() != null &&
                        doctor.getGovernmentIdProofLink() != null;
        return isComplete;
    }

    @Override
    public int createSlots(HttpServletRequest request, SlotRequest slotRequest) {
        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);

        if (user == null) throw new RuntimeException("User not found!");

        Doctor doctor = doctorRepo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found!"));

        LocalTime current = slotRequest.getStartTime();
        LocalTime end = slotRequest.getEndTime();
        int interval = slotRequest.getSlotIntervalMinutes();

        if (interval <= 0) throw new RuntimeException("Slot interval must be positive");

        if (!end.isAfter(current)) throw new RuntimeException("End time must be after start time");

        int createdCount = 0;

        while (current.isBefore(end)) {

            LocalTime next = current.plusMinutes(interval);

            Optional<DoctorSlot> existingSlot = doctorSlotRepo
                    .findByDoctorIdAndSlotDateAndSlotTime(doctor.getId(),
                            slotRequest.getSlotDate(), current);

            if (existingSlot.isPresent()) {
                DoctorSlot slot = existingSlot.get();
                if (slot.getStatus() == SlotStatus.INACTIVE) {
                    slot.setStatus(SlotStatus.ACTIVE);
                    doctorSlotRepo.save(slot);
                    createdCount++;
                }
            } else {
                DoctorSlot slot = new DoctorSlot();
                slot.setSlotDate(slotRequest.getSlotDate());
                slot.setSlotTime(current);
                slot.setDoctor(doctor);
                slot.setStatus(SlotStatus.ACTIVE);
                doctorSlotRepo.save(slot);
                createdCount++;
            }

            current = next;
        }

        return createdCount;
    }

    @Override
    public List<DoctorSlot> getActiveSlots(HttpServletRequest request, String dateStr) {
        String email = request.getUserPrincipal().getName();
        Doctor doctor = doctorRepo.findByUserId(
                userRepo.findByEmailIgnoreCase(email).getId()
        ).orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<DoctorSlot> slots;
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (dateStr != null) {
            LocalDate date = LocalDate.parse(dateStr);
            slots = doctorSlotRepo.findByDoctorIdAndSlotDateAndStatus(
                            doctor.getId(), date, SlotStatus.ACTIVE)
                    .stream()
                    .filter(slot -> !date.equals(today) || !slot.getSlotTime().isBefore(now))
                    .toList();
        } else {
            slots = doctorSlotRepo.findByDoctorIdAndStatus(doctor.getId(), SlotStatus.ACTIVE)
                    .stream()
                    .filter(slot -> slot.getSlotDate().isAfter(today) ||
                            (slot.getSlotDate().isEqual(today) &&
                                    !slot.getSlotTime().isBefore(now)))
                    .toList();
        }

        return slots;
    }

    @Override
    public List<Map<String, Object>> getBookedAppointments(HttpServletRequest request, String dateStr) {
        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);

        Doctor doctor = doctorRepo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<Appointment> appointments;

        if (dateStr != null && !dateStr.isEmpty()) {
            LocalDate date = LocalDate.parse(dateStr);
            appointments = appointmentRepo.findBySlot_Doctor_IdAndSlot_SlotDate(doctor.getId(), date);
        } else {
            appointments = appointmentRepo.findBySlot_Doctor_Id(doctor.getId());
        }

        return appointments.stream()
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("appointmentId", a.getId());
                    map.put("patientName", a.getPatient().getName());
                    map.put("patientId", a.getPatient().getId());
                    map.put("slotDate", a.getSlot().getSlotDate().toString());
                    map.put("slotTime", a.getSlot().getSlotTime().toString());
                    map.put("status", a.getStatus().toString());
                    map.put("reason", a.getReason());
                    map.put("rescheduled", a.isRescheduled());
                    map.put("rescheduleReason", a.getRescheduleReason());
                    return map;
                })
                .sorted((a, b) -> {
                    int cmp = ((String) a.get("slotDate")).compareTo((String) b.get("slotDate"));
                    return (cmp != 0) ? cmp :
                            ((String) a.get("slotTime")).compareTo((String) b.get("slotTime"));
                })
                .toList();
    }

    @Override
    public String updateAppointmentStatus(Long appointmentId, String statusStr) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        try {
            AppointmentStatus newStatus = AppointmentStatus.valueOf(statusStr.toUpperCase());
            appointment.setStatus(newStatus);
            appointmentRepo.save(appointment);
            updateAppoinmentStatusMail(appointment, newStatus);
            return "Appointment status updated to " + newStatus;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + statusStr);
        }
    }

    private void updateAppoinmentStatusMail(Appointment appointment, AppointmentStatus newStatus) {
        String email = appointment.getPatient().getUser().getEmail();
        String patientName = appointment.getPatient().getName();
        String doctorName = appointment.getSlot().getDoctor().getName();
        String date = appointment.getSlot().getSlotDate().toString();
        String time = appointment.getSlot().getSlotTime().toString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("MedVault - Appointment Status Update");
        message.setText(
                "Dear " + patientName + ",\n\n" +
                        "Your appointment with Dr. " + doctorName + " on " + date + " at " + time +
                        " has been updated.\n\n" +
                        "New Status: " + newStatus + "\n\n" +
                        (appointment.getReason() != null ? "Reason: " + appointment.getReason() + "\n\n" : "") +
                        "Thank you,\nMedVault Team"
        );

        mailSender.send(message);
    }

    @Override
    public String updateSlotStatus(HttpServletRequest request, Long slotId, String newStatus) {
        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);

        if (user == null) throw new RuntimeException("User not found!");

        Doctor doctor = doctorRepo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found!"));

        DoctorSlot slot = doctorSlotRepo.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (!slot.getDoctor().getId().equals(doctor.getId()))
            throw new RuntimeException("Forbidden: Slot does not belong to this doctor");

        try {
            SlotStatus statusEnum = SlotStatus.valueOf(newStatus.toUpperCase());
            slot.setStatus(statusEnum);
            doctorSlotRepo.save(slot);
            return "Slot status updated to " + newStatus;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + newStatus);
        }
    }

    @Override
    public void deleteSlot(HttpServletRequest request, Long slotId) {
        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);

        if (user == null) throw new RuntimeException("User not found!");

        Doctor doctor = doctorRepo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found!"));

        DoctorSlot slot = doctorSlotRepo.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (!slot.getDoctor().getId().equals(doctor.getId()))
            throw new RuntimeException("Forbidden");

        List<Appointment> bookedAppointments = appointmentRepo.findBySlot(slot);

        SimpleMailMessage emailMessage = new SimpleMailMessage();

        // delete all appointments
        for (Appointment appointment : bookedAppointments) {
                        emailMessage.setTo(appointment.getPatient().getUser().getEmail());
            emailMessage.setSubject("MedVault - Appointment Cancelled");
            emailMessage.setText(
                    "Dear " + appointment.getPatient().getName() + ",\n\n" +
                            "Your appointment with Dr. " + doctor.getName() + " on " + slot.getSlotDate()+
                            " has been cancelled because the slot was removed.\n\n" +
                            "We apologize for the inconvenience.\n\n" +
                            "Thank you,\nMedVault Team"
            );
            mailSender.send(emailMessage);
            appointmentRepo.delete(appointment);
        }

        doctorSlotRepo.delete(slot);
    }

    @Override
    public List<Map<String, Object>> getDoctorFeedback(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);

        Doctor doctor = doctorRepo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<Appointment> appointments =
                appointmentRepo.findBySlot_Doctor_IdAndStatus(
                        doctor.getId(), AppointmentStatus.COMPLETED);

        return appointments.stream()
                .filter(a -> a.getFeedback() != null)
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("appointmentId", a.getId());
                    map.put("patientName", a.getPatient().getName());
                    map.put("feedback", a.getFeedback());
                    map.put("rating", a.getRating());
                    return map;
                })
                .toList();
    }



    @Override
    public Map<String, Object> requestMedicalRecordForConfirmedPatient(
            HttpServletRequest request, Long patientId, int accessDays) {

        String email = request.getUserPrincipal().getName();
        Doctor doctor = doctorRepo.findByUserId(
                userRepo.findByEmailIgnoreCase(email).getId()
        ).orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<Appointment> confirmed = appointmentRepo
                .findBySlot_Doctor_IdAndStatus(doctor.getId(), AppointmentStatus.CONFIRMED)
                .stream()
                .filter(a -> a.getPatient().getId().equals(patientId))
                .toList();

        if (confirmed.isEmpty()) {
            throw new RuntimeException("Cannot request medical record. No confirmed appointment with this patient.");
        }

        List<MedicalRecord> patientRecords = medicalRecordRepo.findByPatientId(patientId);
        if (patientRecords.isEmpty()) {
            throw new RuntimeException("No medical records found for this patient.");
        }

        LocalDateTime now = LocalDateTime.now();
        List<MedicalRecordAccess> allAccesses =
                medicalRecordAccessRepo.findByDoctorAndMedicalRecord_PatientId(doctor, patientId);

        List<MedicalRecordAccess> toSave = new ArrayList<>();
        medicalrecorslist(accessDays, patientRecords, allAccesses, doctor, now, toSave);

        requestPatientHistoryAccess(request, patientId);

        if (!toSave.isEmpty()) {
            medicalRecordAccessRepo.saveAll(toSave);
        }

        return Map.of("message",
                "Access requests created or renewed for " + toSave.size() + " records.");
    }

    private void medicalrecorslist(int accessDays, List<MedicalRecord> patientRecords, List<MedicalRecordAccess> allAccesses, Doctor doctor, LocalDateTime now, List<MedicalRecordAccess> toSave) {
        for (MedicalRecord record : patientRecords) {
            // Find existing access for this record, if any
            MedicalRecordAccess existing = allAccesses.stream()
                    .filter(a -> a.getMedicalRecord().getId().equals(record.getId()))
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                // No access exists → create new pending request
                MedicalRecordAccess newAccess = new MedicalRecordAccess();
                newAccess.setMedicalRecord(record);
                newAccess.setDoctor(doctor);
                newAccess.setStatus(ApprovalStatus.PENDING);
                newAccess.setExpiry(now.plusDays(accessDays));
                toSave.add(newAccess);
                continue;
            }

            //  If previously rejected → allow re-request by setting to PENDING + new expiry
            if (existing.getStatus() == ApprovalStatus.REJECTED) {
                existing.setStatus(ApprovalStatus.PENDING);
                existing.setExpiry(now.plusDays(accessDays));
                toSave.add(existing);
                continue;
            }

            //  If expired → renew it (set to PENDING)
            if (existing.getExpiry() == null || existing.getExpiry().isBefore(now)) {
                existing.setStatus(ApprovalStatus.PENDING);
                existing.setExpiry(now.plusDays(accessDays));
                toSave.add(existing);
                continue;
            }

            //  Otherwise (still active APPROVED/PENDING) → skip
        }
    }


    @Override
    public List<Map<String, Object>> getApprovedMedicalRecords(
            HttpServletRequest request, Long patientId) {

        String email = request.getUserPrincipal().getName();
        Doctor doctor = doctorRepo.findByUserId(
                userRepo.findByEmailIgnoreCase(email).getId()
        ).orElseThrow(() -> new RuntimeException("Doctor not found"));

        LocalDateTime now = LocalDateTime.now();

        List<MedicalRecordAccess> approved = medicalRecordAccessRepo
                .findByMedicalRecord_PatientIdAndDoctorIdAndStatusAndExpiryAfter(
                        patientId, doctor.getId(), ApprovalStatus.APPROVED, now
                );

        return approved.stream().map(access -> {
            MedicalRecord r = access.getMedicalRecord();
            return Map.<String, Object>of(
                    "recordId", r.getId(),
                    "name", r.getName(),
                    "fileLink", r.getFileLink(),
                    "expiry", access.getExpiry().toString()
            );
        }).toList();
    }

    @Override
    public MedicalRecordViewResponse viewApprovedRecord(
            HttpServletRequest request, Long recordId) {

        String email = request.getUserPrincipal().getName();
        Doctor doctor = doctorRepo.findByUserId(
                userRepo.findByEmailIgnoreCase(email).getId()
        ).orElseThrow(() -> new RuntimeException("Doctor not found"));

        MedicalRecord record = medicalRecordRepo.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        LocalDateTime now = LocalDateTime.now();

        MedicalRecordAccess access = medicalRecordAccessRepo
                .findByMedicalRecord_IdAndDoctorIdAndStatusAndExpiryAfter(
                        recordId, doctor.getId(), ApprovalStatus.APPROVED, now
                ).orElse(null);

        if (access == null) {
            throw new RuntimeException("Access denied or expired.");
        }

        File file = new File(record.getFileLink());
        if (!file.exists()) {
            throw new RuntimeException("File not found on server");
        }

        Resource resource = new FileSystemResource(file);
        return new MedicalRecordViewResponse(resource, file.getName());
    }

    @Override
    public Map<String, Object> requestPatientHistoryAccess(
            HttpServletRequest request, Long patientId) {

        String email = request.getUserPrincipal().getName();
        Doctor doctor = doctorRepo.findByUserId(
                userRepo.findByEmailIgnoreCase(email).getId()
        ).orElseThrow(() -> new RuntimeException("Doctor not found"));

        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Optional<DoctorHistoryAccess> existingOpt =
                doctorHistoryAccessRepo.findByDoctorIdAndPatientId(doctor.getId(), patientId);

        DoctorHistoryAccess access;

        if (existingOpt.isPresent()) {
            access = existingOpt.get();

            if (access.getStatus() == ApprovalStatus.REJECTED ||
                    (access.getExpiry() != null && access.getExpiry().isBefore(LocalDateTime.now()))) {

                access.setStatus(ApprovalStatus.PENDING);
                access.setRequestedAt(LocalDateTime.now());
                access.setExpiry(null);
                doctorHistoryAccessRepo.save(access);

                return Map.of("message", "Access re-request sent successfully.");
            }

            return Map.of("message", "Existing request is already pending or approved.");
        }

        access = new DoctorHistoryAccess();
        access.setDoctor(doctor);
        access.setPatient(patient);
        access.setStatus(ApprovalStatus.PENDING);
        access.setRequestedAt(LocalDateTime.now());
        doctorHistoryAccessRepo.save(access);

        return Map.of("message", "Access request sent successfully.");
    }


    @Override
    public List<PatientHistory> getApprovedPatientHistories(HttpServletRequest request, Long patientId) {
        String email = request.getUserPrincipal().getName();
        Doctor doctor = doctorRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Optional<DoctorHistoryAccess> anyAccess =
                doctorHistoryAccessRepo.findByDoctorIdAndPatientIdAndStatus(
                        doctor.getId(), patientId, ApprovalStatus.APPROVED);

        if (anyAccess.isEmpty()) {
            throw new RuntimeException("You don’t have approved access to this patient.");
        }

        DoctorHistoryAccess activeAccess =
                doctorHistoryAccessRepo.findByDoctorIdAndPatientIdAndStatusAndExpiryAfter(
                        doctor.getId(), patientId, ApprovalStatus.APPROVED, LocalDateTime.now())
                        .orElseThrow(() -> new RuntimeException("No active access found"));

        if (activeAccess == null) {
            throw new RuntimeException("Your previous access has expired. You can request access again.");
        }

        return patientHistoryRepo.findByPatientId(patientId);
    }


    @Override
    public List<DoctorHistoryAccess> getDoctorPendingHistoryRequests(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        Doctor doctor = doctorRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return doctorHistoryAccessRepo.findByDoctorIdAndStatus(doctor.getId(), ApprovalStatus.PENDING);
    }

    @Override
    public List<Map<String, Object>> getEmergencyAlerts(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        Doctor doctor = doctorRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<EmergencyRequest> alerts = emergencyRepo.findByStatusTrueAndDoctorIsNullOrderByCreatedAtDesc();

        return alerts.stream().map(alert -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", alert.getId());
            map.put("patientName", alert.getPatient().getName());
            map.put("problem", alert.getProblem());
            map.put("intensity", alert.getIntensity().toString());
            map.put("message", alert.getMessage());
            map.put("location", alert.getLocation());
            map.put("createdAt", alert.getCreatedAt());
            return map;
        }).toList();
    }

    @Override
    public String acceptEmergency(HttpServletRequest request, Long emergencyId) {
        String email = request.getUserPrincipal().getName();
        Doctor doctor = doctorRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        EmergencyRequest emergency = emergencyRepo.findById(emergencyId)
                .orElseThrow(() -> new RuntimeException("Emergency not found"));

        if (emergency.getDoctor() != null) {
            throw new RuntimeException("This emergency has already been accepted by another doctor");
        }

        emergency.setDoctor(doctor);
        emergency.setStatus(false);
        emergencyRepo.save(emergency);

        try {
            if (emergency.getPatient() != null && emergency.getPatient().getUser() != null
                    && emergency.getPatient().getUser().getEmail() != null) {

                SimpleMailMessage emailMessage = new SimpleMailMessage();
                emailMessage.setTo(emergency.getPatient().getUser().getEmail());
                emailMessage.setSubject("MedVault - Emergency Accepted by Dr. " + doctor.getName());
                emailMessage.setText(
                        "Dear " + emergency.getPatient().getName() + ",\n\n" +
                                "Your emergency request has been accepted by Dr. " + doctor.getName() + ".\n" +
                                "Problem: " + emergency.getProblem() + "\n" +
                                "Intensity: " + emergency.getIntensity() + "\n" +
                                (emergency.getMessage() != null ? "Message: " + emergency.getMessage() + "\n" : "") +
                                (emergency.getLocation() != null ? "Location: " + emergency.getLocation() + "\n" : "") +
                                "\nThe doctor will contact you shortly.\n\n" +
                                "Thank you,\nMedVault Team"
                );

                mailSender.send(emailMessage);
            }
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }

        return "Emergency accepted successfully and patient notified!";
    }

    @Override
    public List<Map<String, Object>> getAcceptedEmergencies(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        Doctor doctor = doctorRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<EmergencyRequest> acceptedAlerts =
                emergencyRepo.findByDoctorOrderByCreatedAtDesc(doctor);

        return acceptedAlerts.stream().map(alert -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", alert.getId());
            map.put("patientName", alert.getPatient().getName());
            map.put("problem", alert.getProblem());
            map.put("intensity", alert.getIntensity().toString());
            map.put("message", alert.getMessage());
            map.put("location", alert.getLocation());
            map.put("createdAt", alert.getCreatedAt());
            return map;
        }).toList();
    }




}
