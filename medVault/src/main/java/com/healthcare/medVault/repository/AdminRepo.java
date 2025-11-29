package com.healthcare.medVault.repository;

import com.healthcare.medVault.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepo extends JpaRepository<Admin, Long> {
}
