package com.healthcare.medVault.repository;


import com.healthcare.medVault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User,Long> {
    User findByEmailIgnoreCase(String email);
    boolean existsByEmail(String email);
}
