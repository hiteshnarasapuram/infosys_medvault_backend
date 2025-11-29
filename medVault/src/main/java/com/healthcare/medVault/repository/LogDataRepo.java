package com.healthcare.medVault.repository;

import com.healthcare.medVault.model.LogData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogDataRepo extends JpaRepository<LogData, Long> {
    List<LogData> findAllByOrderByTimestampDesc();
}
