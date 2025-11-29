package com.healthcare.medVault.service.interfaces;

import com.healthcare.medVault.dto.DashboardResponse;
import com.healthcare.medVault.dto.DoctorResponse;
import com.healthcare.medVault.dto.MedicalRecordViewResponse;
import com.healthcare.medVault.dto.SlotRequest;
import com.healthcare.medVault.model.DoctorHistoryAccess;
import com.healthcare.medVault.model.DoctorSlot;
import com.healthcare.medVault.model.PatientHistory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface DoctorService {


    DashboardResponse getDashboard(HttpServletRequest request);

    Map<String, Object> setPassword(HttpServletRequest request, String newPassword);

    DoctorResponse getProfile(HttpServletRequest request);

    String updateProfile(HttpServletRequest request,
                         DoctorResponse updatedDto,
                         MultipartFile doctorCertificate,
                         MultipartFile governmentId);

    Map<String, Object> checkProfileCompletion(HttpServletRequest request);


    int createSlots(HttpServletRequest request, SlotRequest slotRequest);

    List<DoctorSlot> getActiveSlots(HttpServletRequest request, String dateStr);

    List<Map<String, Object>> getBookedAppointments(HttpServletRequest request, String dateStr);

    String updateAppointmentStatus(Long appointmentId, String statusStr);

    String updateSlotStatus(HttpServletRequest request, Long slotId, String newStatus);

    void deleteSlot(HttpServletRequest request, Long slotId);

    List<Map<String, Object>> getDoctorFeedback(HttpServletRequest request);


    Map<String, Object> requestMedicalRecordForConfirmedPatient(
            HttpServletRequest request, Long patientId, int accessDays);

    List<Map<String, Object>> getApprovedMedicalRecords(
            HttpServletRequest request, Long patientId);

    MedicalRecordViewResponse viewApprovedRecord(
            HttpServletRequest request, Long recordId);

    Map<String, Object> requestPatientHistoryAccess(
            HttpServletRequest request, Long patientId);


    List<PatientHistory> getApprovedPatientHistories(HttpServletRequest request, Long patientId);

    List<DoctorHistoryAccess> getDoctorPendingHistoryRequests(HttpServletRequest request);

    List<Map<String, Object>> getEmergencyAlerts(HttpServletRequest request);

    String acceptEmergency(HttpServletRequest request, Long emergencyId);

    List<Map<String, Object>> getAcceptedEmergencies(HttpServletRequest request);
}
