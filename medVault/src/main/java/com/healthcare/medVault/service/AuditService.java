package com.healthcare.medVault.service;

import com.healthcare.medVault.repository.LogDataRepo;
import com.healthcare.medVault.model.LogData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    @Autowired
    private LogDataRepo logDataRepo;

    public void logAction(String Username, String targetEmail, String action) {
        LogData log = new LogData(Username, targetEmail, action);
        logDataRepo.save(log);
    }
}
