package com.example.attendanceportal.controller;

import com.example.attendanceportal.dto.AttendanceReportDTO;
import com.example.attendanceportal.dto.StudentAttendanceSummaryDTO;
import com.example.attendanceportal.dto.StudentSubjectAttendanceDTO;
import com.example.attendanceportal.entity.Attendance;
import com.example.attendanceportal.entity.Student;
import com.example.attendanceportal.repository.AttendanceRepository;
import com.example.attendanceportal.repository.StudentRepository;
import com.example.attendanceportal.service.AttendanceService;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "http://localhost:3000")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AttendanceService attendanceService;

    // ===================== TEST ENDPOINT =====================
    @GetMapping("/test-token")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> testToken(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Token is working perfectly!");
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities().toString());
        response.put("isAuthenticated", authentication.isAuthenticated());

        System.out.println("🎯 Test endpoint accessed by: " + authentication.getName());
        System.out.println("🎯 Authorities: " + authentication.getAuthorities());

        return ResponseEntity.ok(response);
    }

    // ===================== INIT ATTENDANCE =====================
    @PostMapping("/init-today")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> initAttendanceForToday(
            @RequestParam(defaultValue = "General") String subject) {

        LocalDate today = LocalDate.now();
        List<Student> students = studentRepository.findAll();

        for (Student s : students) {
            boolean exists = attendanceRepository
                    .findByStudentAndDateAndSubject(s, today, subject)
                    .isPresent();

            if (!exists) {
                attendanceRepository.save(
                        new Attendance(s, today, subject, false)
                );
            }
        }
        return ResponseEntity.ok("Initialized attendance. Subject=" + subject);
    }

    // ===================== MARK ATTENDANCE =====================
    @PostMapping("/mark")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> markAttendance(
            @RequestParam Long studentId,
            @RequestParam boolean present,
            @RequestParam(defaultValue = "General") String subject,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        LocalDate attendanceDate = (date != null) ? date : LocalDate.now();

        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) return ResponseEntity.notFound().build();

        Attendance attendance = attendanceRepository
                .findByStudentAndDateAndSubject(student, attendanceDate, subject)
                .orElseGet(() -> new Attendance(student, attendanceDate, subject, present));

        attendance.setPresent(present);
        attendance.setSubject(subject);

        attendanceRepository.save(attendance);

        return ResponseEntity.ok(attendance);
    }

    // ===================== SUMMARY =====================
    @GetMapping("/summary")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public Map<String, Object> getSummary(@RequestParam String subject) {

        LocalDate today = LocalDate.now();

        long totalStudents = studentRepository.count();

        long present = attendanceRepository
                .countByDateAndSubjectAndPresent(today, subject, true);

        long absent = totalStudents - present;

        Map<String, Object> map = new HashMap<>();
        map.put("totalStudents", totalStudents);
        map.put("todayPresent", present);
        map.put("todayAbsent", absent);

        return map;
    }

    // ===================== GET ALL SUBJECTS (NEW) =====================
    @GetMapping("/subjects")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<String>> getAllSubjects() {
        List<String> subjects = attendanceRepository.findAllSubjects();
        if (subjects.isEmpty()) {
            // Return default subjects if no attendance records
            subjects = Arrays.asList("JAVA", "Python", "DBMS", "DSA", "OS", "CN");
        }
        return ResponseEntity.ok(subjects);
    }

    // ===================== GET TODAY'S ATTENDANCE (NEW) =====================
    @GetMapping("/today")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTodayAttendance(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        LocalDate today = (date != null) ? date : LocalDate.now();
        long totalStudents = studentRepository.count();
        long present = attendanceRepository.countByDateAndPresent(today, true);

        Map<String, Object> response = new HashMap<>();
        response.put("total", totalStudents);
        response.put("present", present);
        response.put("absent", totalStudents - present);
        response.put("date", today.toString());

        return ResponseEntity.ok(response);
    }

    // ===================== GET ATTENDANCE FOR SPECIFIC DATE AND SUBJECT (NEW) =====================
    @GetMapping("/by-date-subject")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAttendanceByDateAndSubject(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam String subject) {

        List<Attendance> attendances = attendanceRepository.findByDateAndSubject(date, subject);

        List<Map<String, Object>> response = new ArrayList<>();
        for (Attendance a : attendances) {
            Map<String, Object> record = new HashMap<>();
            record.put("studentId", a.getStudent().getId());
            record.put("studentName", a.getStudent().getName());
            record.put("rollNumber", a.getStudent().getRollNumber());
            record.put("present", a.isPresent());
            response.add(record);
        }

        return ResponseEntity.ok(response);
    }

    // ===================== ADMIN SUMMARY =====================
    @GetMapping("/student-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public List<StudentAttendanceSummaryDTO> getStudentSummary() {
        return attendanceRepository.fetchStudentAttendanceSummary();
    }

    // ===================== ADMIN SUBJECT SUMMARY =====================
    @GetMapping("/subject-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public List<StudentSubjectAttendanceDTO> getStudentSubjectAttendance() {
        return attendanceService.getStudentSubjectAttendance();
    }

    // ===================== REPORT =====================
    @GetMapping("/report")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public List<AttendanceReportDTO> getReport() {
        return attendanceRepository.fetchAttendanceReport();
    }

    // ===================== REPORT BY DATE (STUDENT-SPECIFIC) =====================
    @GetMapping("/report/by-date")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> getReportByDate(
            Authentication authentication,
            @RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            @RequestParam(value = "subject", required = false) String subject) {

        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        System.out.println("=== Report Request ===");
        System.out.println("Username: " + username);
        System.out.println("Role: " + role);
        System.out.println("From: " + fromDate + ", To: " + toDate);
        System.out.println("Subject: " + subject);

        List<AttendanceReportDTO> reports;

        if ("ROLE_STUDENT".equals(role)) {
            // Student: Get only their own attendance
            Student student = studentRepository.findByRollNumber(username)
                    .orElseThrow(() -> new RuntimeException("Student not found: " + username));

            System.out.println("Fetching reports for student ID: " + student.getId());

            if (subject != null && !subject.trim().isEmpty()) {
                reports = attendanceRepository.fetchStudentAttendanceReportByDateRangeAndSubject(
                        student.getId(), fromDate, toDate, subject);
            } else {
                reports = attendanceRepository.fetchStudentAttendanceReportByDateRange(
                        student.getId(), fromDate, toDate);
            }
        } else {
            // Teacher or Admin: Get all attendance
            if (subject != null && !subject.trim().isEmpty()) {
                reports = attendanceRepository.fetchAttendanceReportByDateRangeAndSubject(fromDate, toDate, subject);
            } else {
                reports = attendanceRepository.fetchAttendanceReportByDateRange(fromDate, toDate);
            }
        }

        System.out.println("Found " + reports.size() + " records");

        return ResponseEntity.ok(reports);
    }

    // ===================== EXPORT CSV =====================
    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportToCsv(
            @RequestParam("fromDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam("toDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            @RequestParam(value = "subject", required = false) String subject) {

        List<AttendanceReportDTO> data =
                (subject != null && !subject.trim().isEmpty())
                        ? attendanceRepository.fetchAttendanceReportByDateRangeAndSubject(fromDate, toDate, subject)
                        : attendanceRepository.fetchAttendanceReportByDateRange(fromDate, toDate);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

        writer.println("Student Name,Roll Number,Date,Subject,Status");

        for (AttendanceReportDTO r : data) {
            writer.printf("%s,%s,%s,%s,%s%n",
                    r.getStudentName(),
                    r.getRollNumber(),
                    r.getDate(),
                    r.getSubject(),
                    Boolean.TRUE.equals(r.getPresent()) ? "Present" : "Absent");
        }

        writer.flush();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=attendance_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(out.toByteArray());
    }

    // ===================== EXPORT EXCEL =====================
    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam("fromDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam("toDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            @RequestParam(value = "subject", required = false) String subject) throws Exception {

        List<AttendanceReportDTO> data =
                (subject != null && !subject.trim().isEmpty())
                        ? attendanceRepository.fetchAttendanceReportByDateRangeAndSubject(fromDate, toDate, subject)
                        : attendanceRepository.fetchAttendanceReportByDateRange(fromDate, toDate);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Report");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Student Name");
        header.createCell(1).setCellValue("Roll Number");
        header.createCell(2).setCellValue("Date");
        header.createCell(3).setCellValue("Subject");
        header.createCell(4).setCellValue("Status");

        int rowIdx = 1;
        for (AttendanceReportDTO r : data) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(r.getStudentName());
            row.createCell(1).setCellValue(r.getRollNumber());
            row.createCell(2).setCellValue(r.getDate().toString());
            row.createCell(3).setCellValue(r.getSubject());
            row.createCell(4).setCellValue(
                    Boolean.TRUE.equals(r.getPresent()) ? "Present" : "Absent"
            );
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=attendance_report.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(out.toByteArray());
    }

    // ===================== STUDENT MY SUMMARY =====================
    @GetMapping("/student/my-summary")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyAttendanceSummary(Authentication authentication) {

        String rollNumber = authentication.getName();
        System.out.println("📊 Getting summary for student: " + rollNumber);

        Student student = studentRepository.findByRollNumber(rollNumber)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<Attendance> list = attendanceRepository.findByStudent(student);

        int total = list.size();
        int present = (int) list.stream().filter(Attendance::isPresent).count();
        int absent = total - present;

        double percentage = total == 0 ? 0 : (present * 100.0) / total;

        Map<String, Object> response = new HashMap<>();
        response.put("name", student.getName());
        response.put("rollNumber", student.getRollNumber());
        response.put("totalClasses", total);
        response.put("present", present);
        response.put("absent", absent);
        response.put("percentage", percentage);

        return ResponseEntity.ok(response);
    }

    // ===================== STUDENT SUBJECT SUMMARY =====================
    @GetMapping("/student/my-subject-summary")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMySubjectAttendance(Authentication authentication) {

        String rollNumber = authentication.getName();
        List<StudentSubjectAttendanceDTO> data =
                attendanceRepository.fetchMySubjectAttendance(rollNumber);

        return ResponseEntity.ok(data);
    }

    // ===================== STUDENT FULL ATTENDANCE =====================
    @GetMapping("/student/my-attendance")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyFullAttendance(Authentication authentication) {

        String rollNumber = authentication.getName();

        Student student = studentRepository.findByRollNumber(rollNumber)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<Attendance> list = attendanceRepository.findByStudent(student);

        List<Map<String, Object>> response = new ArrayList<>();

        for (Attendance a : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", a.getDate());
            map.put("subject", a.getSubject());
            map.put("status", a.isPresent() ? "Present" : "Absent");
            response.add(map);
        }

        return ResponseEntity.ok(response);
    }
}