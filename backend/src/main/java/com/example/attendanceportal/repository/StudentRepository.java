package com.example.attendanceportal.repository;

import com.example.attendanceportal.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // ✅ LOGIN (OTP)
    Optional<Student> findByRollNumberOrSystemId(String rollNumber, String systemId);

    // ✅ FIND BY ROLL
    Optional<Student> findByRollNumber(String rollNumber);

    // ✅ 🔥 ADD THIS (FIX ERROR)
    Optional<Student> findBySystemId(String systemId);

    // ✅ SYSTEM ID GENERATOR
    Optional<Student> findTopBySystemIdStartingWithOrderBySystemIdDesc(String yearPrefix);

    // ✅ ROLL NUMBER GENERATOR
    Optional<Student> findTopByDepartmentAndSpecializationOrderByRollNumberDesc(
            String department,
            String specialization
    );
}