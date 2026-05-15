package com.example.attendanceportal.controller;

import com.example.attendanceportal.entity.Student;
import com.example.attendanceportal.service.StudentService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // ================= CREATE STUDENT (TEACHER & ADMIN) =================
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public Student saveStudent(@RequestBody Student student) {
        student.setEmail(null); // optional
        return studentService.createStudent(student);
    }

    // ================= GET ALL STUDENTS =================
    @GetMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    // ================= GET ALL STUDENTS (Alias for Teacher) =================
    @GetMapping("/all")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public List<Student> getAllStudentsAlias() {
        return studentService.getAllStudents();
    }

    // ================= GET STUDENT BY ID =================
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Optional<Student> student = studentService.getStudentById(id);
        return student.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ================= GET STUDENT BY ROLL NUMBER =================
    @GetMapping("/roll/{rollNumber}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<Student> getStudentByRollNumber(@PathVariable String rollNumber) {
        Optional<Student> student = studentService.findByRollNumber(rollNumber);
        return student.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ================= UPDATE STUDENT =================
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Student> updateStudent(
            @PathVariable Long id,
            @RequestBody Student studentDetails) {

        Optional<Student> optionalStudent = studentService.getStudentById(id);

        if (optionalStudent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = optionalStudent.get();

        student.setName(studentDetails.getName());
        student.setSurname(studentDetails.getSurname());
        student.setDepartment(studentDetails.getDepartment());
        student.setSpecialization(studentDetails.getSpecialization());
        student.setEmail(studentDetails.getEmail());
        student.setRollNumber(studentDetails.getRollNumber());

        return ResponseEntity.ok(studentService.saveStudent(student));
    }

    // ================= DELETE STUDENT =================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudentById(id);
        return ResponseEntity.noContent().build();
    }

    // ================= FIX OLD STUDENTS =================
    @PostMapping("/fix-old-students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> fixOldStudents() {
        studentService.fixOldStudents();
        return ResponseEntity.ok("Old students fixed!");
    }

    // ================= 🎯 ATTENDANCE SUMMARY =================
    @GetMapping("/attendance-summary")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getAttendanceSummary() {
        return ResponseEntity.ok(
                Map.of(
                        "totalClasses", 40,
                        "present", 32,
                        "absent", 8,
                        "percentage", 80
                )
        );
    }

    // =========================================================
    // 🔐 OTP LOGIN SYSTEM
    // =========================================================

    // ================= SEND OTP =================
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {

        String identifier = request.get("identifier"); // rollNumber OR systemId

        Optional<Student> optionalStudent =
                studentService.findByRollNumberOrSystemId(identifier);

        if (optionalStudent.isEmpty()) {
            return ResponseEntity.badRequest().body("Student not found");
        }

        Student student = optionalStudent.get();

        // Generate 6-digit OTP
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        student.setOtp(otp);
        studentService.saveStudent(student);

        // TEMP: print OTP in console
        System.out.println("OTP for " + student.getEmail() + " is: " + otp);

        return ResponseEntity.ok(
                Map.of("message", "OTP sent successfully")
        );
    }

    // ================= VERIFY OTP =================
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {

        String identifier = request.get("identifier");
        String otp = request.get("otp");

        Optional<Student> optionalStudent =
                studentService.findByRollNumberOrSystemId(identifier);

        if (optionalStudent.isEmpty()) {
            return ResponseEntity.badRequest().body("Student not found");
        }

        Student student = optionalStudent.get();

        if (!otp.equals(student.getOtp())) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        // Clear OTP after success
        student.setOtp(null);
        studentService.saveStudent(student);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful",
                        "studentId", student.getId(),
                        "name", student.getName(),
                        "rollNumber", student.getRollNumber()
                )
        );
    }
}