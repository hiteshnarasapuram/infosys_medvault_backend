package com.healthcare.medVault.controller;

import com.healthcare.medVault.repository.PendingRegistrationRepo;
import com.healthcare.medVault.repository.UserRepo;
import com.healthcare.medVault.model.PendingRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/register")
@CrossOrigin
public class RegistrationController {

    @Autowired
    private PendingRegistrationRepo pendingRepo;

    @Autowired
    private UserRepo userRepo;

    @PostMapping("/patient")
    public ResponseEntity<Map<String, String>> registerPatient(@RequestBody PendingRegistration request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists in users!"));
        }

        if (pendingRepo.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists in pending list!"));
        }

        request.setRole("PATIENT");
        pendingRepo.save(request);

        return ResponseEntity.ok(Map.of("message", "Patient registration request submitted. Wait for admin approval."));
    }


    @PostMapping("/doctor")
    public ResponseEntity<Map<String, String>> registerDoctor(@RequestBody PendingRegistration request) {

        if (userRepo.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists in users!"));
        }

        if (pendingRepo.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists in pending list!"));
        }

        request.setRole("DOCTOR");
        pendingRepo.save(request);

        return ResponseEntity.ok(Map.of("message", "Doctor registration request submitted. Wait for admin approval."));
    }

}

