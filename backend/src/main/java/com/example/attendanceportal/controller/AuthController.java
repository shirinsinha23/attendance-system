package com.example.attendanceportal.controller;

import com.example.attendanceportal.security.JwtUtil;
import com.example.attendanceportal.repository.StudentRepository;
import com.example.attendanceportal.entity.Student;

import jakarta.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StudentRepository studentRepository;

    // 🔐 EXISTING LOGIN API (ADMIN / TEACHER)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        System.out.println("🔥 LOGIN ATTEMPT: " + request.getUsername());

        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String role = userDetails.getAuthorities()
                    .iterator()
                    .next()
                    .getAuthority();

            String token = jwtUtil.generateToken(userDetails.getUsername(), role);

            return ResponseEntity.ok(
                    Map.of(
                            "token", token,
                            "username", userDetails.getUsername(),
                            "role", role
                    )
            );

        } catch (BadCredentialsException e) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }

    // 📩 SEND OTP TO STUDENT
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {

        String rollNumber = request.get("rollNumber");

        Optional<Student> studentOpt = studentRepository.findByRollNumber(rollNumber);

        if (studentOpt.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Student not found"));
        }

        Student student = studentOpt.get();

        // Generate 6 digit OTP
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        student.setOtp(otp);

        studentRepository.save(student);

        // For now printing OTP in console
        System.out.println("📩 OTP for " + rollNumber + " : " + otp);

        return ResponseEntity.ok(
                Map.of("message", "OTP sent successfully")
        );
    }

    // ✅ VERIFY OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {

        String rollNumber = request.get("rollNumber");
        String otp = request.get("otp");

        Optional<Student> studentOpt = studentRepository.findByRollNumber(rollNumber);

        if (studentOpt.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Student not found"));
        }

        Student student = studentOpt.get();

        if (!otp.equals(student.getOtp())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Invalid OTP"));
        }

        // ✅ Generate JWT
        String token = jwtUtil.generateToken(rollNumber, "ROLE_STUDENT");

        // ✅ OPTIONAL: clear OTP after login (best practice)
        student.setOtp(null);
        studentRepository.save(student);

        // ✅ RETURN FULL STUDENT DATA
        return ResponseEntity.ok(
                Map.of(
                        "token", token,
                        "role", "ROLE_STUDENT",
                        "student", Map.of(
                                "name", student.getName(),
                                "email", student.getEmail(),
                                "rollNumber", student.getRollNumber()
                        )
                )
        );
    }
    // 🔹 LOGIN DTO
    public static class LoginRequest {

        @NotBlank
        private String username;

        @NotBlank
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}