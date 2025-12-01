package com.healthcare.medVault.service.implementation;

import com.healthcare.medVault.repository.*;
import com.healthcare.medVault.dto.*;
import com.healthcare.medVault.enums.AppointmentStatus;
import com.healthcare.medVault.enums.ApprovalStatus;
import com.healthcare.medVault.enums.Intensity;
import com.healthcare.medVault.enums.SlotStatus;
import com.healthcare.medVault.model.*;
import com.healthcare.medVault.service.interfaces.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private DoctorSlotRepo doctorSlotRepo;

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private MedicalRecordRepo medicalRecordRepo;

    @Autowired
    private MedicalRecordAccessRepo medicalRecordAccessRepo;

    @Autowired
    private PatientHistoryRepo patientHistoryRepo;

    @Autowired
    private DoctorHistoryAccessRepo  doctorHistoryAccessRepo;

    @Autowired
    private EmergencyRepo emergencyRepo;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public DashboardResponse getDashboard(HttpServletRequest request) {

        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);

        if (user == null) {
            throw new RuntimeException("User not found!");
        }

        DashboardResponse response = new DashboardResponse();

        if (user.isFirstLogin()) {
            response.setFirstLogin(true);
            response.setMessage("First login detected. Please set a new password.");
        } else {
            response.setFirstLogin(false);
            response.setMessage("Welcome " + user.getEmail());
            Patient patient = patientRepo.findByUserId(user.getId());
            response.setPatient(patient);
        }

        return response;
    }


    @Override
    public PatientResponse getProfile(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);

        if (user == null) throw new RuntimeException("User not found!");

        Patient patient = patientRepo.findByUserId(user.getId());
        if (patient == null) throw new RuntimeException("Patient profile not found!");

        PatientResponse dto = new PatientResponse();
        dto.setName(patient.getName());
        dto.setDob(patient.getDob());
        dto.setGender(patient.getGender());
        dto.setPhone(patient.getPhone());
        dto.setAddress(patient.getAddress());
        dto.setGovernmentIdProofLink(patient.getGovernmentIdProofLink());
        dto.setEmergencyContactPhone(patient.getEmergencyContactPhone());

        return dto;
    }

    @Override
    public String updateProfile(String email,
                                PatientResponse updatedDto,
                                MultipartFile governmentId) {

        User user = userRepo.findByEmailIgnoreCase(email);
        if (user == null) {
            throw new RuntimeException("User not found!");
        }

        Patient patient = patientRepo.findByUserId(user.getId());
        if (patient == null) {
            throw new RuntimeException("Patient profile not found!");
        }

        // Update patient details
        savePatientProfile(updatedDto, patient);

        try {
            if (governmentId != null && !governmentId.isEmpty()) {
                String govIdLink = saveFile(governmentId, patient.getId(), "government_id");
                patient.setGovernmentIdProofLink(govIdLink);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving file: " + e.getMessage());
        }

        patient.setApprovalStatus(ApprovalStatus.APPROVED);
        patientRepo.save(patient);

        return "Patient profile updated successfully!";
    }


    private void savePatientProfile(PatientResponse updatedDto, Patient patient) {
        patient.setName(updatedDto.getName() != null ? updatedDto.getName() : patient.getName());
        patient.setDob(updatedDto.getDob() != null ? updatedDto.getDob() : patient.getDob());
        patient.setGender(updatedDto.getGender() != null ? updatedDto.getGender() : patient.getGender());
        patient.setPhone(updatedDto.getPhone() != null ? updatedDto.getPhone() : patient.getPhone());
        patient.setAddress(updatedDto.getAddress() != null ? updatedDto.getAddress() : patient.getAddress());
        patient.setEmergencyContactPhone(updatedDto.getEmergencyContactPhone() != null ? updatedDto.getEmergencyContactPhone() : patient.getEmergencyContactPhone());
    }

    private String saveFile(MultipartFile file, Long patientId, String type) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String uploadDir = "D:/medvault/uploads/patients/" + patientId;
        File dir = new File(uploadDir);
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("Could not create directory");

        String filename = type + "_" + System.currentTimeMillis() + ".pdf";
        File destFile = new File(dir, filename);
        destFile.getParentFile().mkdirs();
        file.transferTo(destFile);

        return destFile.getAbsolutePath();
    }

    @Override
    public boolean checkProfileCompletion(String email) {

        User user = userRepo.findByEmailIgnoreCase(email);
        if (user == null) {
            throw new RuntimeException("User not found!");
        }

        Patient patient = patientRepo.findByUserId(user.getId());
        if (patient == null) {
            throw new RuntimeException("Patient not found!");
        }

        return patient.getName() != null &&
                patient.getDob() != null &&
                patient.getGender() != null &&
                patient.getPhone() != null &&
                patient.getAddress() != null &&
                patient.getGovernmentIdProofLink() != null;
    }


    @Override
    public void setPassword(HttpServletRequest request, String password) {

        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);

        if (user == null) {
            throw new RuntimeException("User not found!");
        }

        if (!user.isFirstLogin()) {
            throw new IllegalStateException("Password already set.");
        }

        user.setPassword(password);
        user.setFirstLogin(false);

        userRepo.save(user);
    }


    @Override
    public List<Map<String, Object>> searchDoctors(String keyword) {

        List<Doctor> doctors;

        if (keyword != null && !keyword.isEmpty()) {
            doctors = doctorRepo.findByApprovalStatusAndNameContainingIgnoreCase(
                    ApprovalStatus.APPROVED,
                    keyword
            );
        } else {
            doctors = doctorRepo.findByApprovalStatus(ApprovalStatus.APPROVED);
        }

        return doctors.stream().map(doc -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", doc.getId());
            map.put("name", doc.getName());
            map.put("specialization", doc.getSpecialization());
            map.put("hospital", doc.getHospital());

            // Completed appointments for rating calculation
            List<Appointment> completedAppointments = appointmentRepo
                    .findBySlot_DoctorIdAndStatus(doc.getId(), AppointmentStatus.COMPLETED);

            List<Integer> ratings = completedAppointments.stream()
                    .map(Appointment::getRating)
                    .filter(Objects::nonNull)
                    .toList();

            double avgRating = ratings.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);

            map.put("averageRating", avgRating);
            map.put("ratingCount", ratings.size());

            return map;
        }).toList();
    }




    @Override
    public List<SlotResponse> getDoctorSlots(Long doctorId, String date) {
        LocalDate slotDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();

        List<DoctorSlot> slots = doctorSlotRepo
                .findByDoctorIdAndSlotDateGreaterThanEqualAndStatusOrderBySlotDateAscSlotTimeAsc(
                        doctorId, slotDate, SlotStatus.ACTIVE
                );

        return slots.stream()
                .map(slot -> new SlotResponse(
                        slot.getId(),
                        slot.getSlotDate().toString(),
                        slot.getSlotTime().toString()
                ))
                .toList();
    }


    @Override
    public String bookAppointment(HttpServletRequest request, Long slotId, String reason) {

        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);
        Patient patient = patientRepo.findByUserId(user.getId());

        DoctorSlot slot = doctorSlotRepo.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        // Check if patient already has an appointment on the same date
        boolean alreadyBooked = appointmentRepo.existsByPatientIdAndSlot_SlotDateAndStatusIn(
                patient.getId(),
                slot.getSlotDate(),
                Arrays.asList(
                        AppointmentStatus.PENDING,
                        AppointmentStatus.CONFIRMED,
                        AppointmentStatus.COMPLETED
                )
        );

        if (alreadyBooked) {
            throw new RuntimeException("You already have an appointment on this date!");
        }

        Appointment appointment = new Appointment();
        appointment.setSlot(slot);
        appointment.setPatient(patient);
        appointment.setReason(reason);
        appointment.setStatus(AppointmentStatus.PENDING);

        appointmentRepo.save(appointment);

        return "Appointment booked successfully!";
    }



    @Override
    public List<Map<String, Object>> getAppointments(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);
        Patient patient = patientRepo.findByUserId(user.getId());

        List<Appointment> appointments = appointmentRepo.findByPatientId(patient.getId());

        appointments.sort(Comparator.comparing((Appointment appt) -> appt.getSlot().getSlotDate())
                .thenComparing(appt -> appt.getSlot().getSlotTime())
                .reversed());

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Map<String, Object>> appointmentResponses = appointments.stream()
//                .filter(appt -> {
//                    LocalDate slotDate = appt.getSlot().getSlotDate();
//                    LocalTime slotTime = appt.getSlot().getSlotTime();
//
//                    // Include if date is after today OR (same day but time >= now)
//                    return slotDate.isAfter(today) ||
//                            (slotDate.isEqual(today) && !slotTime.isBefore(now));
//                })
                .map(appt -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("appointmentId", appt.getId());
                    map.put("doctorId", appt.getSlot().getDoctor().getId());
                    map.put("doctorName", appt.getSlot().getDoctor().getName());
                    map.put("date", appt.getSlot().getSlotDate().toString());
                    map.put("time", appt.getSlot().getSlotTime().toString());
                    map.put("status", appt.getStatus().toString());
                    map.put("reasonForBooking", appt.getReason());
                    return map;
                })
                .collect(Collectors.toList());

        return appointmentResponses;
    }


    @Override
    public String submitFeedback(HttpServletRequest request,
                                 Long appointmentId,
                                 String feedback,
                                 Integer rating) {

        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);
        Patient patient = patientRepo.findByUserId(user.getId());

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("You cannot give feedback for this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Feedback can only be given after the appointment is completed.");
        }

        appointment.setFeedback(feedback);
        if (rating != null) {
            appointment.setRating(rating);
        }
        appointmentRepo.save(appointment);

        return "Feedback submitted successfully!";
    }

    @Override
    public String uploadRecord(HttpServletRequest request, MultipartFile file, String recordName) {

        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);
        Patient patient = patientRepo.findByUserId(user.getId());

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        try {
            String uploadDir = "D:/medvault/uploads/medical_records/" + patient.getId();
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File destFile = new File(dir, filename);
            file.transferTo(destFile);

            MedicalRecord record = new MedicalRecord();
            record.setName(recordName);
            record.setFileLink(destFile.getAbsolutePath());
            record.setPatient(patient);
            medicalRecordRepo.save(record);

            return "Medical record uploaded successfully!";
        } catch (IOException e) {
            throw new RuntimeException("Error saving file: " + e.getMessage());
        }
    }



    @Override
    public List<Map<String, Object>> getRecords(HttpServletRequest request) {

        String email = request.getUserPrincipal().getName();
        Patient patient = patientRepo.findByUserId(
                userRepo.findByEmailIgnoreCase(email).getId()
        );

        List<MedicalRecord> records = medicalRecordRepo.findByPatientId(patient.getId());

        return records.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("name", r.getName());
            map.put("fileLink", r.getFileLink());
            return map;
        }).collect(Collectors.toList());
    }



    @Override
    public String deleteRecord(HttpServletRequest request, Long recordId) {

        String email = request.getUserPrincipal().getName();
        Patient patient = patientRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId());

        MedicalRecord record = medicalRecordRepo.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        if (!record.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Cannot delete this record");
        }

        List<MedicalRecordAccess> accessList = medicalRecordAccessRepo.findByMedicalRecord(record);
        if (!accessList.isEmpty()) {
            medicalRecordAccessRepo.deleteAll(accessList);
        }

        File file = new File(record.getFileLink());
        if (file.exists()) file.delete();

        medicalRecordRepo.delete(record);

        return "Record deleted successfully";
    }


    @Override
    public FileSystemResource viewRecord(HttpServletRequest request, Long recordId) {

        String email = request.getUserPrincipal().getName();
        Patient patient = patientRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId());

        MedicalRecord record = medicalRecordRepo.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        if (!record.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("You don't have access to this record");
        }

        File file = new File(record.getFileLink());
        if (!file.exists()) {
            throw new RuntimeException("File not found on server");
        }

        return new FileSystemResource(file);
    }

    @Override
    public List<Map<String, Object>> getPendingRecordAccess(HttpServletRequest request) {

        String email = request.getUserPrincipal().getName();
        Patient patient = patientRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId());

        if (patient == null) {
            throw new RuntimeException("Patient not found!");
        }

        List<MedicalRecordAccess> pending = medicalRecordAccessRepo
                .findByMedicalRecord_Patient_IdAndStatus(patient.getId(), ApprovalStatus.PENDING);

        return pending.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("accessId", a.getId());
            map.put("recordId", a.getMedicalRecord().getId());
            map.put("recordName", a.getMedicalRecord().getName());
            map.put("doctorId", a.getDoctor().getId());
            map.put("doctorName", a.getDoctor().getName());
            map.put("expiry", a.getExpiry() != null ? a.getExpiry().toString() : null);
            map.put("status", a.getStatus().toString());
            return map;
        }).toList();
    }


    @Override
    public String approveRecordAccess(HttpServletRequest request, Long accessId) {

        String email = request.getUserPrincipal().getName();
        Patient patient = patientRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId());

        if (patient == null) {
            throw new RuntimeException("Patient not found!");
        }

        MedicalRecordAccess access = medicalRecordAccessRepo.findById(accessId)
                .orElseThrow(() -> new RuntimeException("Access request not found"));

        if (!access.getMedicalRecord().getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Cannot approve this request");
        }

        if (access.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("Request is not pending");
        }

        access.setStatus(ApprovalStatus.APPROVED);
        access.setExpiry(LocalDateTime.now().plusHours(24));
        medicalRecordAccessRepo.save(access);

        return "Access approved successfully. Expiry set to 24 hours.";
    }

    @Override
    public String rejectRecordAccess(HttpServletRequest request, Long accessId) {

        String email = request.getUserPrincipal().getName();
        Patient patient = patientRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId());

        if (patient == null) {
            throw new RuntimeException("Patient not found!");
        }

        MedicalRecordAccess access = medicalRecordAccessRepo.findById(accessId)
                .orElseThrow(() -> new RuntimeException("Access request not found"));

        if (!access.getMedicalRecord().getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Cannot reject this request");
        }

        if (access.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("Request is not pending");
        }

        access.setStatus(ApprovalStatus.REJECTED);
        access.setExpiry(null);
        medicalRecordAccessRepo.save(access);

        return "Access request rejected successfully.";
    }


    @Override
    public String rescheduleAppointment(
            HttpServletRequest request, Long appointmentId, Long newSlotId, String reason) {

        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);
        Patient patient = patientRepo.findByUserId(user.getId());

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("You cannot reschedule this appointment.");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED ||
                appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("This appointment cannot be rescheduled.");
        }

        DoctorSlot currentSlot = appointment.getSlot();
        if (currentSlot.getId().equals(newSlotId)) {
            throw new RuntimeException("You selected the same slot. Choose another slot.");
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(
                currentSlot.getSlotDate(), currentSlot.getSlotTime());

        if (Duration.between(LocalDateTime.now(), appointmentDateTime).toHours() < 24) {
            throw new RuntimeException("Rescheduling is allowed only before 24 hours.");
        }

        DoctorSlot newSlot = doctorSlotRepo.findById(newSlotId)
                .orElseThrow(() -> new RuntimeException("New slot not found"));

        if (newSlot.getStatus() != SlotStatus.ACTIVE) {
            throw new RuntimeException("Selected slot is not available.");
        }

        appointment.setSlot(newSlot);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setRescheduled(true);
        appointment.setRescheduleReason(reason);

        appointmentRepo.save(appointment);

        return "Appointment rescheduled successfully!";
    }


    @Override
    public String cancelAppointment(HttpServletRequest request, Long appointmentId, String reason) {
        String email = request.getUserPrincipal().getName();
        User user = userRepo.findByEmailIgnoreCase(email);
        Patient patient = patientRepo.findByUserId(user.getId());

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("You cannot cancel this appointment.");
        }

        if (appointment.getStatus() == AppointmentStatus.CONFIRMED ||
                appointment.getStatus() == AppointmentStatus.COMPLETED ||
                appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("This appointment cannot be cancelled.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setRescheduleReason(reason);

        appointmentRepo.save(appointment);

        return "Appointment cancelled successfully!";
    }

    @Override
    public PatientHistoryResponseDTO addHistory(HttpServletRequest request, PatientHistoryRequestDTO dto) {
        String email = request.getUserPrincipal().getName();
        Patient patient = patientRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId());

        PatientHistory history = new PatientHistory();
        history.setPatient(patient);
        history.setProblem(dto.getProblem());
        history.setDate(dto.getDate());
        history.setIntensity(dto.getIntensity());
        history.setNotes(dto.getNotes());

        patientHistoryRepo.save(history);

        return new PatientHistoryResponseDTO(
                history.getId(),
                history.getProblem(),
                history.getDate(),
                history.getIntensity(),
                history.getNotes()
        );
    }

    @Override
    public List<PatientHistoryResponseDTO> getHistory(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        Patient patient = patientRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId());

        return patientHistoryRepo
                .findByPatientIdOrderByDateDesc(patient.getId())
                .stream()
                .map(h -> new PatientHistoryResponseDTO(
                        h.getId(),
                        h.getProblem(),
                        h.getDate(),
                        h.getIntensity(),
                        h.getNotes()
                ))
                .toList();
    }

    @Override
    public PatientHistoryResponseDTO updateHistory(
            HttpServletRequest request, Long historyId, PatientHistoryRequestDTO updatedDto) {

        String email = request.getUserPrincipal().getName();
        Patient patient = patientRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId());

        PatientHistory history = patientHistoryRepo.findById(historyId)
                .orElseThrow(() -> new RuntimeException("History entry not found"));

        if (!history.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Cannot update this entry");
        }

        history.setProblem(updatedDto.getProblem() != null ? updatedDto.getProblem() : history.getProblem());
        history.setDate(updatedDto.getDate() != null ? updatedDto.getDate() : history.getDate());
        history.setIntensity(updatedDto.getIntensity() != null ? updatedDto.getIntensity() : history.getIntensity());
        history.setNotes(updatedDto.getNotes() != null ? updatedDto.getNotes() : history.getNotes());

        patientHistoryRepo.save(history);

        return new PatientHistoryResponseDTO(
                history.getId(),
                history.getProblem(),
                history.getDate(),
                history.getIntensity(),
                history.getNotes()
        );
    }

    @Override
    public String deleteHistory(HttpServletRequest request, Long historyId) {
        String email = request.getUserPrincipal().getName();
        Patient patient = patientRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId());

        PatientHistory history = patientHistoryRepo.findById(historyId)
                .orElseThrow(() -> new RuntimeException("History entry not found"));

        if (!history.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Cannot delete this entry");
        }

        patientHistoryRepo.delete(history);

        return "History deleted successfully!";
    }


    @Override
    public List<Map<String, Object>> getPendingDoctorRequests(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        Patient patient = patientRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId());

        List<DoctorHistoryAccess> requests =
                doctorHistoryAccessRepo.findByPatientIdAndStatus(patient.getId(), ApprovalStatus.PENDING);

        return requests.stream().map(req -> {
            Map<String, Object> map = new HashMap<>();
            map.put("requestId", req.getId());
            map.put("doctorId", req.getDoctor().getId());
            map.put("doctorName", req.getDoctor().getName());
            map.put("doctorEmail", req.getDoctor().getUser().getEmail());
            map.put("status", req.getStatus().toString());
            return map;
        }).toList();
    }

    @Override
    public String approveHistoryRequest(HttpServletRequest request, Long requestId, int accessHours) {

        String email = request.getUserPrincipal().getName();
        Patient patient = patientRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId());

        DoctorHistoryAccess accessRequest = doctorHistoryAccessRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!accessRequest.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("This request is not for you");
        }

        accessRequest.setStatus(ApprovalStatus.APPROVED);
        accessRequest.setExpiry(LocalDateTime.now().plusHours(accessHours));
        doctorHistoryAccessRepo.save(accessRequest);

        return "Request approved for 24 hours";
    }

    @Override
    public String rejectHistoryRequest(HttpServletRequest request, Long requestId) {

        String email = request.getUserPrincipal().getName();
        Patient patient = patientRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId());

        DoctorHistoryAccess accessRequest = doctorHistoryAccessRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!accessRequest.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("This request is not for you");
        }

        accessRequest.setStatus(ApprovalStatus.REJECTED);
        doctorHistoryAccessRepo.save(accessRequest);

        return "Request rejected successfully";
    }


    @Override
    public String sendEmergency(HttpServletRequest request,
                                String problem,
                                Intensity intensity,
                                String message,
                                String location) {

        String email = request.getUserPrincipal().getName();
        Patient patient = patientRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId());

        if (patient == null) {
            throw new RuntimeException("Patient not found!");
        }

        EmergencyRequest emergency = new EmergencyRequest();
        emergency.setPatient(patient);
        emergency.setProblem(problem);
        emergency.setIntensity(intensity);
        emergency.setMessage(message);
        emergency.setLocation(location);
        emergency.setCreatedAt(LocalDateTime.now());
        emergency.setStatus(true);
        emergency.setDoctor(null);

        emergencyRepo.save(emergency);
        sendEmergencyMail(problem, intensity, message, location, patient);



        return "Emergency request sent successfully!";
    }


    private void sendEmergencyMail(String problem, Intensity intensity, String message, String location, Patient patient) {
        SimpleMailMessage emailMessage = new SimpleMailMessage();

        List<Doctor> doctors = doctorRepo.findByApprovalStatus(ApprovalStatus.APPROVED);
        for (Doctor doc : doctors) {
            try {
                emailMessage.setTo(doc.getEmail());
                emailMessage.setSubject("MedVault - Emergency Alert from " + patient.getName());
                emailMessage.setText(
                        "Dear Dr. " + doc.getName() + ",\n\n" +
                                "An emergency has been raised by patient: " + patient.getName() + ".\n" +
                                "Problem: " + problem + "\n" +
                                "Intensity: " + intensity + "\n" +
                                (message != null ? "Message: " + message + "\n" : "") +
                                (location != null ? "Location: " + location + "\n" : "") +
                                "\nPlease respond immediately if you can assist.\n\n" +
                                "Thank you,\nMedVault Team"
                );
                mailSender.send(emailMessage);
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }


    @Override
    public List<Map<String, Object>> getPatientEmergencies(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        Patient patient = patientRepo.findByUserId(userRepo.findByEmailIgnoreCase(email).getId());
        if (patient == null) {
            throw new RuntimeException("Patient not found!");
        }

        List<EmergencyRequest> emergencies = emergencyRepo.findByPatientId(patient.getId());

        return emergencies.stream().map(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("problem", e.getProblem());
            map.put("intensity", e.getIntensity());
            map.put("message", e.getMessage());
            map.put("location", e.getLocation());
            map.put("createdAt", e.getCreatedAt());
            map.put("status", e.getStatus());
            map.put("doctorName", e.getDoctor() != null ? e.getDoctor().getName() : "Not yet accepted");
            return map;
        }).toList();
    }







}
