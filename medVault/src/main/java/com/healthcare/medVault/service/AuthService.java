package com.healthcare.medVault.service;

import com.healthcare.medVault.repository.UserRepo;
import com.healthcare.medVault.dto.LoginRequest;
import com.healthcare.medVault.dto.LoginResponse;
import com.healthcare.medVault.jwt.JwtUtil;
import com.healthcare.medVault.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JavaMailSender mailSender;

    private Map<String, String> otpStorage = new HashMap<>();

    public LoginResponse login(LoginRequest request) {
        User user = userRepo.findByEmailIgnoreCase(request.getEmail());

        if (user == null || !user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        System.out.println("token: " + token);

        return new LoginResponse(token, user.getRole().name());
    }


    public String generateOtp(String email) {
        User user = userRepo.findByEmailIgnoreCase(email);
        if (user == null) {
            throw new RuntimeException("User with this email does not exist!");
        }

        String otp = String.valueOf(100000 + new Random().nextInt(900000)); // 6-digit OTP
//        String otp = String.valueOf(new Random().nextInt(100000,999999));
        otpStorage.put(email, otp);


        System.out.println("Sending OTP to email: " + email + " | OTP = " + otp);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom("MedVault <no-reply@medvault.com>");
        message.setSubject("MedVault - OTP Verification");
        message.setText(
                "Hello,\n\n" +
                        "Your One-Time Password (OTP) for password reset is:\n\n" +
                        otp + "\n\n" +
                        "This OTP is valid for 10 minutes.\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "Regards,\n" +
                        "MedVault Support Team"
        );
        mailSender.send(message);

        return "OTP sent to your email!";
    }

    // VERIFY OTP
    public boolean verifyOtp(String email, String otp) {
        return otpStorage.containsKey(email) && otpStorage.get(email).equals(otp);
    }

    // RESET PASSWORD
    public String resetPassword(String email, String otp, String newPassword) {
        if (!verifyOtp(email, otp)) {
            return "Invalid OTP!";
        }

        User user = userRepo.findByEmailIgnoreCase(email);
        if (user == null) {
            return "User not found!";
        }

        user.setPassword(newPassword);
        userRepo.save(user);

        otpStorage.remove(email);
        return "Password reset successfully!";
    }
}
