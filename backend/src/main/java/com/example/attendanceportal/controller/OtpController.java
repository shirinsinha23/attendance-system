package com.example.attendanceportal.controller;

import com.example.attendanceportal.entity.Student;
import com.example.attendanceportal.entity.Teacher;
import com.example.attendanceportal.repository.StudentRepository;
import com.example.attendanceportal.repository.TeacherRepository;
import com.example.attendanceportal.security.JwtUtil;
import com.example.attendanceportal.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@CrossOrigin(origins = "http://localhost:3000")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // ================= SEND OTP (Student - 6 digits) =================
    @PostMapping("/student/send")
    public ResponseEntity<?> sendStudentOtp(@RequestBody Map<String, String> request) {

        String input = request.get("email"); // rollNumber OR systemId

        Student student = studentRepository
                .findByRollNumberOrSystemId(input, input)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Generate 6-digit OTP for students
        String otp = otpService.generateOtp(student.getEmail());

        System.out.println("📧 Student OTP for " + student.getEmail() + " = " + otp);

        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP sent successfully");
        response.put("email", student.getEmail());

        return ResponseEntity.ok(response);
    }

    // ================= VERIFY OTP (Student) =================
    @PostMapping("/student/verify")
    public ResponseEntity<?> verifyStudentOtp(@RequestBody Map<String, String> request) {

        String input = request.get("email"); // rollNumber/systemId
        String otp = request.get("otp");

        Student student = studentRepository
                .findByRollNumberOrSystemId(input, input)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        boolean isValid = otpService.verifyOtp(student.getEmail(), otp);

        if (!isValid) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid OTP");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // GENERATE JWT TOKEN
        String token = jwtUtil.generateToken(
                student.getRollNumber(),
                "ROLE_STUDENT"
        );

        System.out.println("✅ Token generated for student: " + student.getRollNumber());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", "ROLE_STUDENT");
        response.put("name", student.getName());
        response.put("rollNumber", student.getRollNumber());

        return ResponseEntity.ok(response);
    }

    // ================= SEND OTP (Teacher - 4 digits) =================
    @PostMapping("/teacher/send")
    public ResponseEntity<?> sendTeacherOtp(@RequestBody Map<String, String> request) {

        String input = request.get("email"); // email OR employeeId

        Teacher teacher = teacherRepository
                .findByEmailOrEmployeeId(input, input)
                .orElseThrow(() -> new RuntimeException("Teacher not found with email/ID: " + input));

        // Generate 4-digit OTP for teachers
        String otp = otpService.generateOtp(teacher.getEmail(), 4);

        System.out.println("📧 Teacher OTP for " + teacher.getEmail() + " = " + otp);

        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP sent successfully");
        response.put("email", teacher.getEmail());

        return ResponseEntity.ok(response);
    }

    // ================= VERIFY OTP (Teacher) =================
    @PostMapping("/teacher/verify")
    public ResponseEntity<?> verifyTeacherOtp(@RequestBody Map<String, String> request) {

        String input = request.get("email"); // email or employeeId
        String otp = request.get("otp");

        Teacher teacher = teacherRepository
                .findByEmailOrEmployeeId(input, input)
                .orElseThrow(() -> new RuntimeException("Teacher not found with email/ID: " + input));

        boolean isValid = otpService.verifyOtp(teacher.getEmail(), otp);

        if (!isValid) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid OTP");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // GENERATE JWT TOKEN
        String token = jwtUtil.generateToken(
                teacher.getEmail(),
                "ROLE_TEACHER"
        );

        System.out.println("✅ Token generated for teacher: " + teacher.getEmail());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", "ROLE_TEACHER");
        response.put("name", teacher.getName());
        response.put("employeeId", teacher.getEmployeeId());
        response.put("email", teacher.getEmail());

        return ResponseEntity.ok(response);
    }
}