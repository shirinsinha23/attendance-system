package com.example.attendanceportal.dto;

public class StudentSubjectAttendanceDTO {

    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String subject;
    private Long totalClasses;
    private Long presentClasses;
    private Double percentage;

    public StudentSubjectAttendanceDTO(
            Long studentId,
            String studentName,
            String rollNumber,
            String subject,
            Long totalClasses,
            Long presentClasses
    ) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.subject = subject;
        this.totalClasses = totalClasses;
        this.presentClasses = presentClasses;

        // Calculate percentage safely
        if (totalClasses != null && totalClasses > 0) {
            this.percentage = (presentClasses * 100.0) / totalClasses;
        } else {
            this.percentage = 0.0;
        }
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

    public String getSubject() {
        return subject;
    }

    public Long getTotalClasses() {
        return totalClasses;
    }

    public Long getPresentClasses() {
        return presentClasses;
    }

    public Double getPercentage() {
        return percentage;
    }
}
