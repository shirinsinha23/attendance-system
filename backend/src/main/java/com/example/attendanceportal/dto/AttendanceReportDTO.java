package com.example.attendanceportal.dto;

import java.time.LocalDate;

public class AttendanceReportDTO {

    private Long attendanceId;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private LocalDate date;
    private String subject;
    private Boolean present;

    // Constructor must EXACTLY match JPQL query order & types
    public AttendanceReportDTO(
            Long attendanceId,
            Long studentId,
            String studentName,
            String rollNumber,
            LocalDate date,
            String subject,
            Boolean present
    ) {
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.date = date;
        this.subject = subject;
        this.present = present;
    }

    public Long getAttendanceId() {
        return attendanceId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getSubject() {
        return subject;
    }

    public Boolean getPresent() {
        return present;
    }
}