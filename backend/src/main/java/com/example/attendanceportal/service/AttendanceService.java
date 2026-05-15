package com.example.attendanceportal.service;

import com.example.attendanceportal.dto.StudentSubjectAttendanceDTO;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    List<StudentSubjectAttendanceDTO> getStudentSubjectAttendance();

    List<StudentSubjectAttendanceDTO> getStudentSubjectSummary(
            Long studentId,
            LocalDate fromDate,
            LocalDate toDate
    );
}
