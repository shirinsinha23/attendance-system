package com.example.attendanceportal.repository;

import com.example.attendanceportal.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByEmail(String email);

    Optional<Teacher> findByEmployeeId(String employeeId);

    Optional<Teacher> findByEmailOrEmployeeId(String email, String employeeId);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);
}