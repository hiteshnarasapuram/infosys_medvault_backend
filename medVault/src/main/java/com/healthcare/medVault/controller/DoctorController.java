package com.healthcare.medVault.controller;

import com.healthcare.medVault.dto.*;
import com.healthcare.medVault.service.interfaces.DoctorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
@CrossOrigin
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(HttpServletRequest request) {
        DashboardResponse response = doctorService.getDashboard(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/set-password")
    public ResponseEntity<?> setPassword(HttpServletRequest request,
                                         @RequestBody PasswordRequest req) {
        Map<String, Object> result = doctorService.setPassword(request, req.getNewPassword());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        DoctorResponse profile = doctorService.getProfile(request);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            HttpServletRequest request,
            @ModelAttribute DoctorResponse updatedDto,
            @RequestParam(value = "doctorCertificate", required = false) MultipartFile doctorCertificate,
            @RequestParam(value = "governmentId", required = false) MultipartFile governmentId) {

        String message = doctorService.updateProfile(request, updatedDto, doctorCertificate, governmentId);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/check-profile-completion")
    public ResponseEntity<?> checkProfileCompletion(HttpServletRequest request) {
        Map<String, Object> result = doctorService.checkProfileCompletion(request);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/create-slots")
    public ResponseEntity<?> createSlots(HttpServletRequest request,
                                         @RequestBody SlotRequest slotRequest) {
        int result = doctorService.createSlots(request, slotRequest);
        return ResponseEntity.ok(result + " slots created/activated successfully!");
    }

    @GetMapping("/slots")
    public ResponseEntity<?> getActiveSlots(HttpServletRequest request,
                                            @RequestParam(required = false) String date) {
        return ResponseEntity.ok(doctorService.getActiveSlots(request, date));
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getBookedAppointments(HttpServletRequest request,
                                                   @RequestParam(required = false) String date) {
        return ResponseEntity.ok(doctorService.getBookedAppointments(request, date));
    }

    @PutMapping("/appointments/{appointmentId}/status")
    public ResponseEntity<?> updateAppointmentStatus(@PathVariable Long appointmentId,
                                                     @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        return ResponseEntity.ok(doctorService.updateAppointmentStatus(appointmentId, status));
    }

    @PutMapping("/slots/{slotId}/status")
    public ResponseEntity<?> updateSlotStatus(HttpServletRequest request,
                                              @PathVariable Long slotId,
                                              @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        return ResponseEntity.ok(doctorService.updateSlotStatus(request, slotId, status));
    }

    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<?> deleteSlot(HttpServletRequest request,
                                        @PathVariable Long slotId) {
        doctorService.deleteSlot(request, slotId);
        return ResponseEntity.ok("Slot deleted successfully");
    }

    @GetMapping("/appointments/feedback")
    public ResponseEntity<?> getDoctorFeedback(HttpServletRequest request) {
        return ResponseEntity.ok(doctorService.getDoctorFeedback(request));
    }


    @PostMapping("/patients/{patientId}/medical-records/request")
    public ResponseEntity<?> requestMedicalRecord(
            HttpServletRequest request,
            @PathVariable Long patientId,
            @RequestParam int accessDays) {

        Map<String, Object> response = doctorService
                .requestMedicalRecordForConfirmedPatient(request, patientId, accessDays);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/patients/{patientId}/medical-records")
    public ResponseEntity<?> getPatientMedicalRecords(
            HttpServletRequest request,
            @PathVariable Long patientId) {

        List<Map<String, Object>> records =
                doctorService.getApprovedMedicalRecords(request, patientId);

        return ResponseEntity.ok(records);
    }

    @GetMapping("/medical-records/{recordId}/view")
    public ResponseEntity<?> viewMedicalRecord(
            HttpServletRequest request,
            @PathVariable Long recordId) {

        MedicalRecordViewResponse response =
                doctorService.viewApprovedRecord(request, recordId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + response.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(response.getResource());
    }

    @PostMapping("/patients/{patientId}/history/request")
    public ResponseEntity<?> requestPatientHistory(
            HttpServletRequest request,
            @PathVariable Long patientId) {

        Map<String, Object> response =
                doctorService.requestPatientHistoryAccess(request, patientId);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/patients/{patientId}/histories")
    public ResponseEntity<?> getApprovedPatientHistories(
            HttpServletRequest request,
            @PathVariable Long patientId) {

        try {
            return ResponseEntity.ok(doctorService.getApprovedPatientHistories(request, patientId));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        }
    }


    @GetMapping("/history/requests/pending")
    public ResponseEntity<?> getPendingHistoryRequests(HttpServletRequest request) {
        return ResponseEntity.ok(doctorService.getDoctorPendingHistoryRequests(request));
    }


    @GetMapping("/emergencies")
    public ResponseEntity<?> getEmergencyAlerts(HttpServletRequest request) {
        return ResponseEntity.ok(doctorService.getEmergencyAlerts(request));
    }


    @PostMapping("/emergency/accept/{id}")
    public ResponseEntity<?> acceptEmergency(
            HttpServletRequest request,
            @PathVariable("id") Long emergencyId) {

        try {
            return ResponseEntity.ok(Map.of("message",
                    doctorService.acceptEmergency(request, emergencyId)));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }


    @GetMapping("/emergencies/accepted")
    public ResponseEntity<?> getAcceptedEmergencies(HttpServletRequest request) {
        return ResponseEntity.ok(doctorService.getAcceptedEmergencies(request));
    }















}











