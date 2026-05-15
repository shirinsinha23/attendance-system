package com.example.attendanceportal.controller;

import com.example.attendanceportal.dto.StudentSubjectAttendanceDTO;
import com.example.attendanceportal.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance/chart")
@CrossOrigin(origins = "http://localhost:3000")
public class AttendanceChartController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * Bar graph data:
     * Student → Subject → Total classes → Present classes
     */
    @GetMapping("/student-subject")
    public List<StudentSubjectAttendanceDTO> getStudentSubjectAttendance() {
        return attendanceService.getStudentSubjectAttendance();
    }
}
