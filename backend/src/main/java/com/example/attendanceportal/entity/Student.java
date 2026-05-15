package com.example.attendanceportal.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String systemId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(unique = true, nullable = false)
    private String rollNumber;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String specialization;

    @Column(unique = true, nullable = false)
    private String email;

    // ⭐ NEW FIELD FOR OTP LOGIN
    private String otp;

    // Attendance Mapping
    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Attendance> attendances;

    public Student() {}

    public Student(String systemId, String name, String surname,
                   String rollNumber, String department,
                   String specialization, String email) {
        this.systemId = systemId;
        this.name = name;
        this.surname = surname;
        this.rollNumber = rollNumber;
        this.department = department;
        this.specialization = specialization;
        this.email = email;
    }

    // ===== GETTERS AND SETTERS =====

    public Long getId() {
        return id;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // ⭐ OTP getter/setter
    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public List<Attendance> getAttendances() {
        return attendances;
    }

    public void setAttendances(List<Attendance> attendances) {
        this.attendances = attendances;
    }
}