package com.example.attendanceportal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String employeeId;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String designation;

    @Column(nullable = false)
    private String phoneNumber;

    private String otp;

    private LocalDateTime otpExpiry;

    @Column(nullable = false)
    private boolean enabled = true;

    // Default constructor
    public Teacher() {}

    // Constructor with required fields
    public Teacher(String email, String name, String employeeId,
                   String department, String designation, String phoneNumber) {
        this.email = email;
        this.name = name;
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
        this.phoneNumber = phoneNumber;
    }

    // Getters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getEmployeeId() { return employeeId; }
    public String getDepartment() { return department; }
    public String getDesignation() { return designation; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getOtp() { return otp; }
    public LocalDateTime getOtpExpiry() { return otpExpiry; }
    public boolean isEnabled() { return enabled; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public void setDepartment(String department) { this.department = department; }
    public void setDesignation(String designation) { this.designation = designation; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setOtp(String otp) { this.otp = otp; }
    public void setOtpExpiry(LocalDateTime otpExpiry) { this.otpExpiry = otpExpiry; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
