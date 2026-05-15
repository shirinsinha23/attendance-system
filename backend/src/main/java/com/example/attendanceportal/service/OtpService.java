package com.example.attendanceportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    private final Map<String, OtpData> otpStorage = new HashMap<>();
    private final Random random = new Random();

    // Generate 6-digit OTP for students
    public String generateOtp(String email) {
        return generateOtp(email, 6);
    }

    // Generate OTP with custom length (for teachers - 4 digits)
    public String generateOtp(String email, int length) {
        // Generate OTP with specified length
        StringBuilder otpBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otpBuilder.append(random.nextInt(10));
        }
        String otp = otpBuilder.toString();

        // Store OTP with timestamp
        OtpData otpData = new OtpData();
        otpData.setOtp(otp);
        otpData.setTimestamp(System.currentTimeMillis());
        otpStorage.put(email, otpData);

        // Send email with OTP
        sendOtpEmail(email, otp, length);

        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        if (!otpStorage.containsKey(email)) return false;

        OtpData storedOtpData = otpStorage.get(email);
        String storedOtp = storedOtpData.getOtp();

        // Check if OTP is expired (5 minutes = 300000 milliseconds)
        long currentTime = System.currentTimeMillis();
        if (currentTime - storedOtpData.getTimestamp() > 300000) {
            otpStorage.remove(email);
            return false;
        }

        if (storedOtp.equals(otp)) {
            otpStorage.remove(email);
            return true;
        }

        return false;
    }

    private void sendOtpEmail(String toEmail, String otp, int length) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setFrom("shirinsinha23@gmail.com");
            message.setSubject("Smart Attendance System - OTP Verification");

            String userType = (length == 4) ? "Teacher" : "Student";

            String emailBody = String.format(
                    "Hello,\n\n" +
                            "Your %d-digit OTP for Smart Attendance System %s login is: %s\n\n" +
                            "This OTP is valid for 5 minutes.\n\n" +
                            "If you did not request this, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "Smart Attendance System Team",
                    length,
                    userType,
                    otp
            );

            message.setText(emailBody);
            mailSender.send(message);

            System.out.println("✅ OTP email sent to: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send OTP email: " + e.getMessage());
            System.err.println("But OTP is printed below for testing!");
        }
    }

    // Inner class to store OTP with timestamp
    private static class OtpData {
        private String otp;
        private long timestamp;

        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}