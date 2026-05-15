package com.example.attendanceportal.controller;

import com.example.attendanceportal.entity.Student;
import com.example.attendanceportal.entity.Teacher;
import com.example.attendanceportal.entity.User;
import com.example.attendanceportal.repository.StudentRepository;
import com.example.attendanceportal.repository.TeacherRepository;
import com.example.attendanceportal.repository.UserRepository;
import com.example.attendanceportal.security.JwtUtil;
import com.example.attendanceportal.security.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;  // ✅ ADDED - For JWT token generation

    // ================= ADMIN LOGIN (with JWT Token) =================
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Admin not found"));
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid password"));
        }

        if (user.getRole() != Role.ROLE_ADMIN) {
            return ResponseEntity.badRequest().body(Map.of("message", "Not authorized"));
        }

        // ✅ Generate JWT Token for Admin
        String token = jwtUtil.generateToken(
                user.getUsername(),
                "ROLE_ADMIN"
        );

        System.out.println("✅ Admin token generated for: " + user.getUsername());

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "token", token,
                "username", user.getUsername(),
                "role", user.getRole().toString()
        ));
    }

    // ================= GET ALL STUDENTS =================
    @GetMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // ================= GET STUDENT BY ID =================
    @GetMapping("/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Optional<Student> student = studentRepository.findById(id);
        return student.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ================= CREATE STUDENT =================
    @PostMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    public Student createStudent(@RequestBody Student student) {
        return studentRepository.save(student);
    }

    // ================= UPDATE STUDENT =================
    @PutMapping("/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student studentDetails) {
        Optional<Student> optionalStudent = studentRepository.findById(id);

        if (optionalStudent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = optionalStudent.get();
        student.setName(studentDetails.getName());
        student.setEmail(studentDetails.getEmail());
        student.setRollNumber(studentDetails.getRollNumber());
        student.setDepartment(studentDetails.getDepartment());
        student.setSpecialization(studentDetails.getSpecialization());

        return ResponseEntity.ok(studentRepository.save(student));
    }

    // ================= DELETE STUDENT =================
    @DeleteMapping("/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ================= GET ALL TEACHERS =================
    @GetMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    // ================= GET TEACHER BY ID =================
    @GetMapping("/teachers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Teacher> getTeacherById(@PathVariable Long id) {
        Optional<Teacher> teacher = teacherRepository.findById(id);
        return teacher.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ================= CREATE TEACHER =================
    @PostMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    public Teacher createTeacher(@RequestBody Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    // ================= UPDATE TEACHER =================
    @PutMapping("/teachers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Teacher> updateTeacher(@PathVariable Long id, @RequestBody Teacher teacherDetails) {
        Optional<Teacher> optionalTeacher = teacherRepository.findById(id);

        if (optionalTeacher.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Teacher teacher = optionalTeacher.get();
        teacher.setName(teacherDetails.getName());
        teacher.setEmail(teacherDetails.getEmail());
        teacher.setDepartment(teacherDetails.getDepartment());
        teacher.setDesignation(teacherDetails.getDesignation());
        teacher.setPhoneNumber(teacherDetails.getPhoneNumber());

        return ResponseEntity.ok(teacherRepository.save(teacher));
    }

    // ================= DELETE TEACHER =================
    @DeleteMapping("/teachers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        teacherRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ================= DASHBOARD STATS =================
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", studentRepository.count());
        stats.put("totalTeachers", teacherRepository.count());
        stats.put("totalAdmins", userRepository.count());

        return ResponseEntity.ok(stats);
    }
}