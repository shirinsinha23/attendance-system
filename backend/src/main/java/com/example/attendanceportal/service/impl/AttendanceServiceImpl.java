package com.example.attendanceportal.service.impl;

import com.example.attendanceportal.dto.StudentSubjectAttendanceDTO;
import com.example.attendanceportal.repository.AttendanceRepository;
import com.example.attendanceportal.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    // ================= ALL STUDENTS SUBJECT SUMMARY =================
    @Override
    public List<StudentSubjectAttendanceDTO> getStudentSubjectAttendance() {
        return attendanceRepository.fetchStudentSubjectAttendance();
    }

    // ================= STUDENT SUBJECT SUMMARY =================
    @Override
    public List<StudentSubjectAttendanceDTO> getStudentSubjectSummary(
            Long studentId,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        // ✅ If no date filter → return all student subject attendance
        if (fromDate == null || toDate == null) {
            return attendanceRepository.fetchStudentSubjectAttendance();
        }

        // ⚠️ Since your repository does NOT have studentId + date method,
        // we fallback to date filter and then filter by studentId manually

        return attendanceRepository.fetchAttendanceReportByDateRange(fromDate, toDate)
                .stream()
                .filter(dto -> dto.getStudentId().equals(studentId))
                .map(dto -> new StudentSubjectAttendanceDTO(
                        dto.getStudentId(),
                        dto.getStudentName(),
                        dto.getRollNumber(),
                        dto.getSubject(),
                        1L,
                        dto.getPresent() != null && dto.getPresent() ? 1L : 0L
                ))
                .toList();
    }
}