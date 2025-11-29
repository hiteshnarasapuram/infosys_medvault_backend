package com.healthcare.medVault.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
public class LogData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String Username;
    private String email;
    private String action;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public LogData() {
        this.timestamp = LocalDateTime.now();
    }

    public LogData(String username, String email, String action) {
        this.Username = username;
        this.email = email;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }


}
