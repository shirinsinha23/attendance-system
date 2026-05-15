package com.example.attendanceportal.service;

import com.example.attendanceportal.entity.Student;

import java.util.List;
import java.util.Optional;

public interface StudentService {

    Student createStudent(Student student);

    Optional<Student> getStudentById(Long id);

    List<Student> getAllStudents();

    Student saveStudent(Student student);

    void deleteStudentById(Long id);

    void fixOldStudents();

    // Find by roll number or system ID (for OTP login)
    Optional<Student> findByRollNumberOrSystemId(String identifier);

    // 🔥 ADD THIS METHOD - Find by roll number only
    Optional<Student> findByRollNumber(String rollNumber);
}