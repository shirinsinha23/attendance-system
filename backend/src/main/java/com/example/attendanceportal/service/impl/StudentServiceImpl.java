package com.example.attendanceportal.service.impl;

import com.example.attendanceportal.entity.Student;
import com.example.attendanceportal.repository.StudentRepository;
import com.example.attendanceportal.service.StudentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.*;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository studentRepository;

    // ================= CREATE STUDENT =================
    @Override
    public Student createStudent(Student student) {

        normalizeDepartmentAndSpecialization(student);

        student.setSystemId(generateSystemId());

        student.setRollNumber(generateRollNumber(
                student.getDepartment(),
                student.getSpecialization()
        ));

        student.setEmail(generateEmail(student));

        return studentRepository.save(student);
    }

    // ================= GET BY ID =================
    @Override
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    // ================= GET ALL =================
    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // ================= UPDATE =================
    @Override
    public Student saveStudent(Student student) {

        Optional<Student> oldOpt = studentRepository.findById(student.getId());
        Student old = oldOpt.orElse(null);

        normalizeDepartmentAndSpecialization(student);

        if (student.getSystemId() == null || student.getSystemId().isEmpty()) {
            student.setSystemId(generateSystemId());
        }

        if (old != null) {
            boolean changed =
                    !safe(old.getDepartment()).equalsIgnoreCase(safe(student.getDepartment())) ||
                            !safe(old.getSpecialization()).equalsIgnoreCase(safe(student.getSpecialization()));

            if (changed) {
                student.setRollNumber(generateRollNumber(
                        student.getDepartment(),
                        student.getSpecialization()
                ));
            }
        }

        student.setEmail(generateEmail(student));

        return studentRepository.save(student);
    }

    // ================= DELETE =================
    @Override
    public void deleteStudentById(Long id) {
        studentRepository.deleteById(id);
    }

    // ================= FIX OLD STUDENTS =================
    @Override
    public void fixOldStudents() {

        List<Student> students = studentRepository.findAll();

        String yearPrefix = String.valueOf(Year.now().getValue());

        Optional<Student> lastStudent =
                studentRepository.findTopBySystemIdStartingWithOrderBySystemIdDesc(yearPrefix);

        long nextSystemIdNum = lastStudent
                .map(s -> Long.parseLong(s.getSystemId()))
                .orElse(Long.parseLong(yearPrefix + "000000"));

        Map<String, Integer> lastRollNumMap = new HashMap<>();

        for (Student student : students) {

            boolean updated = false;

            normalizeDepartmentAndSpecialization(student);

            if (student.getSystemId() == null || student.getSystemId().isEmpty()) {
                nextSystemIdNum++;
                student.setSystemId(String.valueOf(nextSystemIdNum));
                updated = true;
            }

            if (student.getRollNumber() == null || student.getRollNumber().isEmpty()) {

                String department = safe(student.getDepartment());
                String specialization = safe(student.getSpecialization());

                String key = department + "-" + specialization;

                if (!lastRollNumMap.containsKey(key)) {

                    Optional<Student> lastRollStudent =
                            studentRepository.findTopByDepartmentAndSpecializationOrderByRollNumberDesc(
                                    department, specialization
                            );

                    int lastNum = 0;

                    if (lastRollStudent.isPresent() && lastRollStudent.get().getRollNumber() != null) {
                        String[] parts = lastRollStudent.get().getRollNumber().split("-");
                        lastNum = Integer.parseInt(parts[2]);
                    }

                    lastRollNumMap.put(key, lastNum);
                }

                int nextRoll = lastRollNumMap.get(key) + 1;
                lastRollNumMap.put(key, nextRoll);

                student.setRollNumber(
                        department + "-" + specialization + "-" + String.format("%03d", nextRoll)
                );

                updated = true;
            }

            if (student.getEmail() == null || student.getEmail().isEmpty()) {
                student.setEmail(generateEmail(student));
                updated = true;
            }

            if (updated) {
                studentRepository.save(student);
            }
        }
    }

    // =========================================================
    // 🔐 OTP LOGIN SUPPORT
    // =========================================================

    @Override
    public Optional<Student> findByRollNumberOrSystemId(String identifier) {

        Optional<Student> student = studentRepository.findByRollNumber(identifier);

        if (student.isEmpty()) {
            student = studentRepository.findBySystemId(identifier);
        }

        return student;
    }

    // 🔥 ADD THIS METHOD - Find by roll number only
    @Override
    public Optional<Student> findByRollNumber(String rollNumber) {
        return studentRepository.findByRollNumber(rollNumber);
    }

    // ================= SYSTEM ID =================
    private String generateSystemId() {

        String yearPrefix = String.valueOf(Year.now().getValue());

        Optional<Student> lastStudent =
                studentRepository.findTopBySystemIdStartingWithOrderBySystemIdDesc(yearPrefix);

        if (lastStudent.isPresent()) {
            long next = Long.parseLong(lastStudent.get().getSystemId()) + 1;
            return String.valueOf(next);
        }

        return yearPrefix + "000001";
    }

    // ================= ROLL NUMBER =================
    private String generateRollNumber(String department, String specialization) {

        department = safe(department);
        specialization = safe(specialization);

        Optional<Student> lastStudent =
                studentRepository.findTopByDepartmentAndSpecializationOrderByRollNumberDesc(
                        department, specialization
                );

        int next = 1;

        if (lastStudent.isPresent() && lastStudent.get().getRollNumber() != null) {
            String[] parts = lastStudent.get().getRollNumber().split("-");
            next = Integer.parseInt(parts[2]) + 1;
        }

        return department + "-" + specialization + "-" + String.format("%03d", next);
    }

    // ================= EMAIL =================
    private String generateEmail(Student student) {

        String firstName = student.getName() != null
                ? student.getName().trim().toLowerCase()
                : "student";

        firstName = firstName.replaceAll("\\s+", "");

        String sys = student.getSystemId() != null ? student.getSystemId() : "000";
        String last3 = sys.length() >= 3 ? sys.substring(sys.length() - 3) : sys;

        return firstName + last3 + "@gmail.com";
    }

    // ================= NORMALIZE =================
    private void normalizeDepartmentAndSpecialization(Student student) {

        String dept = safe(student.getDepartment());
        String spec = safe(student.getSpecialization());

        if ("AIML".equalsIgnoreCase(dept)) {
            student.setDepartment("CSE");
            student.setSpecialization("AI & ML");
            return;
        }

        if ("CSE".equalsIgnoreCase(dept)) {
            student.setDepartment("CSE");

            if (spec.isEmpty()) {
                student.setSpecialization("Core");
            }
        } else {
            student.setSpecialization("");
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}