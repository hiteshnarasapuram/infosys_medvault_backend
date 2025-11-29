package com.healthcare.medVault.service.interfaces;

import com.healthcare.medVault.dto.*;
import com.healthcare.medVault.enums.Intensity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface PatientService {

    DashboardResponse getDashboard(HttpServletRequest request);

    PatientResponse getProfile(HttpServletRequest request);

    String updateProfile(String email,
                         PatientResponse updatedDto,
                         MultipartFile governmentId);

    boolean checkProfileCompletion(String email);

    void setPassword(HttpServletRequest request, String newPassword);

    List<Map<String, Object>> searchDoctors(String keyword);

    List<SlotResponse> getDoctorSlots(Long doctorId, String date);

    String bookAppointment(HttpServletRequest request, Long slotId, String reason);

    List<Map<String, Object>> getAppointments(HttpServletRequest request);

    String submitFeedback(HttpServletRequest request, Long appointmentId, String feedback, Integer rating);

    String uploadRecord(HttpServletRequest request, MultipartFile file, String recordName);


    List<Map<String, Object>> getRecords(HttpServletRequest request);

    String deleteRecord(HttpServletRequest request, Long recordId);

    FileSystemResource viewRecord(HttpServletRequest request, Long recordId);

    List<Map<String, Object>> getPendingRecordAccess(HttpServletRequest request);

    String approveRecordAccess(HttpServletRequest request, Long accessId);

    String rejectRecordAccess(HttpServletRequest request, Long accessId);


    String rescheduleAppointment(
            HttpServletRequest request, Long appointmentId, Long newSlotId, String reason);


    String cancelAppointment(HttpServletRequest request, Long appointmentId, String reason);

    PatientHistoryResponseDTO addHistory(HttpServletRequest request, PatientHistoryRequestDTO dto);

    List<PatientHistoryResponseDTO> getHistory(HttpServletRequest request);

    PatientHistoryResponseDTO updateHistory(
            HttpServletRequest request, Long historyId, PatientHistoryRequestDTO updatedDto);

    String deleteHistory(HttpServletRequest request, Long historyId);

    List<Map<String, Object>> getPendingDoctorRequests(HttpServletRequest request);

    String approveHistoryRequest(HttpServletRequest request, Long requestId, int accessHours);

    String rejectHistoryRequest(HttpServletRequest request, Long requestId);

    String sendEmergency(HttpServletRequest request,
                         String problem,
                         Intensity intensity,
                         String message,
                         String location);

    List<Map<String, Object>> getPatientEmergencies(HttpServletRequest request);
}
