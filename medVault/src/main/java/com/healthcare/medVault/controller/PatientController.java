package com.healthcare.medVault.controller;

import com.healthcare.medVault.dto.*;
import com.healthcare.medVault.enums.Intensity;
import com.healthcare.medVault.model.PatientHistory;
import com.healthcare.medVault.service.implementation.PatientServiceImpl;
import com.healthcare.medVault.service.interfaces.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@CrossOrigin
public class PatientController {

    @Autowired
    private PatientService patientService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpServletRequest request) {
        DashboardResponse response = patientService.getDashboard(request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            PatientResponse profile = patientService.getProfile(request);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }

    }


    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(HttpServletRequest request,
                                           @ModelAttribute PatientResponse updatedDto,
                                           @RequestPart(required = false) MultipartFile governmentId) {

        try {
            String email = request.getUserPrincipal().getName();
            String message = patientService.updateProfile(email, updatedDto, governmentId);
            return ResponseEntity.ok(message);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/check-profile")
    public ResponseEntity<?> checkProfileCompletion(HttpServletRequest request) {

        try {
            String email = request.getUserPrincipal().getName();
            boolean isComplete = patientService.checkProfileCompletion(email);

            return ResponseEntity.ok(isComplete ? "Profile is complete" : "Profile is incomplete");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    @PostMapping("/set-password")
    public ResponseEntity<?> setPassword(HttpServletRequest request, @RequestBody Map<String, String> body) {
        try {
            patientService.setPassword(request, body.get("password"));
            return ResponseEntity.ok(Map.of("message", "Password set successfully! Please login again."));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }


    @GetMapping("/search-doctors")
    public ResponseEntity<?> searchDoctors(@RequestParam(required = false) String keyword) {
        List<Map<String, Object>> doctors = patientService.searchDoctors(keyword);
        return ResponseEntity.ok(doctors);
    }


    @GetMapping("/doctor/{doctorId}/slots")
    public ResponseEntity<List<SlotResponse>> getDoctorSlots(@PathVariable Long doctorId,
                                                             @RequestParam(required = false) String date) {
        List<SlotResponse> slots = patientService.getDoctorSlots(doctorId, date);
        return ResponseEntity.ok(slots);
    }


    @PostMapping("/book-appointment")
    public ResponseEntity<?> bookAppointment(HttpServletRequest request,
                                             @RequestParam Long slotId,
                                             @RequestParam String reason) {
        try {
            String message = patientService.bookAppointment(request, slotId, reason);
            return ResponseEntity.ok(Map.of("message", message));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }


    @GetMapping("/appointments")
    public ResponseEntity<?> getAppointments(HttpServletRequest request) {
        List<Map<String, Object>> appointments = patientService.getAppointments(request);
        return ResponseEntity.ok(appointments);
    }



    @PostMapping("/appointments/{appointmentId}/feedback")
    public ResponseEntity<?> submitFeedback(HttpServletRequest request,
                                            @PathVariable Long appointmentId,
                                            @RequestParam String feedback,
                                            @RequestParam(required = false) Integer rating) {

        try {
            String message = patientService.submitFeedback(request, appointmentId, feedback, rating);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/records")
    public ResponseEntity<?> uploadRecord(HttpServletRequest request,
                                          @RequestParam("file") MultipartFile file,
                                          @RequestParam("name") String name) {

        try {
            String message = patientService.uploadRecord(request, file, name);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/records")
    public ResponseEntity<?> getRecords(HttpServletRequest request) {
        try {
            List<Map<String, Object>> records = patientService.getRecords(request);
            return ResponseEntity.ok(records);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/records/{recordId}")
    public ResponseEntity<?> deleteRecord(HttpServletRequest request, @PathVariable Long recordId) {
        try {
            String msg = patientService.deleteRecord(request, recordId);
            return ResponseEntity.ok(msg);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/records/{recordId}/view")
    public ResponseEntity<?> viewRecord(HttpServletRequest request,
                                        @PathVariable Long recordId) {
        try {
            FileSystemResource resource = patientService.viewRecord(request, recordId);
            File file = resource.getFile();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/records/pending-access")
    public ResponseEntity<?> getPendingAccess(HttpServletRequest request) {
        try {
            List<Map<String,Object>> data = patientService.getPendingRecordAccess(request);
            return ResponseEntity.ok(data);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/records/access/{accessId}/approve")
    public ResponseEntity<?> approveAccess(HttpServletRequest request, @PathVariable Long accessId) {
        try {
            String msg = patientService.approveRecordAccess(request, accessId);
            return ResponseEntity.ok(msg);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/records/access/{accessId}/reject")
    public ResponseEntity<?> rejectAccess(HttpServletRequest request, @PathVariable Long accessId) {
        try {
            String msg = patientService.rejectRecordAccess(request, accessId);
            return ResponseEntity.ok(msg);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PutMapping("/appointments/{appointmentId}/reschedule")
    public ResponseEntity<?> rescheduleAppointment(
            HttpServletRequest request,
            @PathVariable Long appointmentId,
            @RequestParam Long newSlotId,
            @RequestParam String reason) {
        try {
            String msg = patientService.rescheduleAppointment(request, appointmentId, newSlotId, reason);
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/appointments/{appointmentId}/cancel")
    public ResponseEntity<?> cancelAppointment(
            HttpServletRequest request,
            @PathVariable Long appointmentId,
            @RequestParam String reason) {
        try {
            String msg = patientService.cancelAppointment(request, appointmentId, reason);
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/history")
    public ResponseEntity<?> addHistory(HttpServletRequest request,
                                        @RequestBody PatientHistoryRequestDTO dto) {
        try {
            PatientHistoryResponseDTO response = patientService.addHistory(request, dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(HttpServletRequest request) {
        try {
            List<PatientHistoryResponseDTO> response = patientService.getHistory(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/history/{historyId}")
    public ResponseEntity<?> updateHistory(HttpServletRequest request,
                                           @PathVariable Long historyId,
                                           @RequestBody PatientHistoryRequestDTO updatedDto) {
        try {
            PatientHistoryResponseDTO response = patientService.updateHistory(request, historyId, updatedDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/history/{historyId}")
    public ResponseEntity<?> deleteHistory(HttpServletRequest request,
                                           @PathVariable Long historyId) {
        try {
            String msg = patientService.deleteHistory(request, historyId);
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/history/requests")
    public ResponseEntity<?> getPendingHistoryRequests(HttpServletRequest request) {
        return ResponseEntity.ok(patientService.getPendingDoctorRequests(request));
    }

    @PostMapping("/history/requests/{requestId}/approve")
    public ResponseEntity<?> approveHistoryRequest(
            HttpServletRequest request,
            @PathVariable Long requestId) {
        int accessHours = 24;

        try {
            String result = patientService.approveHistoryRequest(request, requestId, accessHours);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/history/requests/{requestId}/reject")
    public ResponseEntity<?> rejectHistoryRequest(
            HttpServletRequest request,
            @PathVariable Long requestId) {

        try {
            String result = patientService.rejectHistoryRequest(request, requestId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/emergency")
    public ResponseEntity<?> sendEmergency(
            HttpServletRequest request,
            @RequestBody EmergencyRequestDTO dto) {
        try {
            String msg = patientService.sendEmergency(
                    request,
                    dto.getProblem(), dto.getIntensity(), dto.getMessage(), dto.getLocation()
            );
            return ResponseEntity.ok(msg);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/emergencies")
    public ResponseEntity<?> getPatientEmergencies(HttpServletRequest request) {
        return ResponseEntity.ok(patientService.getPatientEmergencies(request));
    }











}
