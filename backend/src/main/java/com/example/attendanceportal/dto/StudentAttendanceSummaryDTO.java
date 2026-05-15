package com.example.attendanceportal.dto;

public class StudentAttendanceSummaryDTO {
    private Long studentId;
    private String name;
    private String rollNumber;
    private long totalDays;
    private long presentDays;

    public StudentAttendanceSummaryDTO(Long studentId, String name, String rollNumber, long totalDays, long presentDays) {
        this.studentId = studentId;
        this.name = name;
        this.rollNumber = rollNumber;
        this.totalDays = totalDays;
        this.presentDays = presentDays;
    }

    public Long getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getRollNumber() { return rollNumber; }
    public long getTotalDays() { return totalDays; }
    public long getPresentDays() { return presentDays; }

    public double getAttendancePercentage() {
        if (totalDays == 0) return 0.0;
        return (presentDays * 100.0) / totalDays;
    }
}