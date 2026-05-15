package com.example.attendanceportal.repository;

import com.example.attendanceportal.dto.AttendanceReportDTO;
import com.example.attendanceportal.dto.StudentAttendanceSummaryDTO;
import com.example.attendanceportal.dto.StudentSubjectAttendanceDTO;
import com.example.attendanceportal.entity.Attendance;
import com.example.attendanceportal.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // ================= BASIC =================
    Optional<Attendance> findByStudentAndDate(Student student, LocalDate date);

    Optional<Attendance> findByStudentAndDateAndSubject(
            Student student,
            LocalDate date,
            String subject
    );

    List<Attendance> findByStudent(Student student);

    List<Attendance> findByDate(LocalDate date);

    long countByDateAndSubjectAndPresent(
            LocalDate date,
            String subject,
            boolean present
    );

    // ================= DELETE =================
    @Transactional
    @Modifying
    @Query("DELETE FROM Attendance a WHERE a.student.id = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);

    // ================= REPORT (ALL) =================
    @Query("""
        SELECT new com.example.attendanceportal.dto.AttendanceReportDTO(
            a.id,
            s.id,
            s.name,
            s.rollNumber,
            a.date,
            a.subject,
            a.present
        )
        FROM Attendance a
        JOIN a.student s
        ORDER BY a.date DESC
    """)
    List<AttendanceReportDTO> fetchAttendanceReport();

    // ================= REPORT (DATE RANGE) =================
    @Query("""
        SELECT new com.example.attendanceportal.dto.AttendanceReportDTO(
            a.id,
            s.id,
            s.name,
            s.rollNumber,
            a.date,
            a.subject,
            a.present
        )
        FROM Attendance a
        JOIN a.student s
        WHERE a.date BETWEEN :fromDate AND :toDate
        ORDER BY a.date DESC
    """)
    List<AttendanceReportDTO> fetchAttendanceReportByDateRange(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    // ================= DATE + SUBJECT =================
    @Query("""
        SELECT new com.example.attendanceportal.dto.AttendanceReportDTO(
            a.id,
            s.id,
            s.name,
            s.rollNumber,
            a.date,
            a.subject,
            a.present
        )
        FROM Attendance a
        JOIN a.student s
        WHERE a.date BETWEEN :fromDate AND :toDate
          AND a.subject = :subject
        ORDER BY a.date DESC
    """)
    List<AttendanceReportDTO> fetchAttendanceReportByDateRangeAndSubject(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("subject") String subject
    );

    // ================= GET ALL DISTINCT SUBJECTS =================
    @Query("SELECT DISTINCT a.subject FROM Attendance a ORDER BY a.subject")
    List<String> findAllSubjects();

    // ================= GET SUBJECTS WITH ATTENDANCE RECORDS =================
    @Query("SELECT DISTINCT a.subject FROM Attendance a WHERE a.subject IS NOT NULL")
    List<String> getDistinctSubjects();

    // ================= TODAY'S ATTENDANCE SUMMARY =================
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.date = :date AND a.present = :present")
    long countByDateAndPresent(
            @Param("date") LocalDate date,
            @Param("present") boolean present
    );

    // ================= STUDENT-SPECIFIC REPORT BY DATE RANGE =================
    @Query("""
        SELECT new com.example.attendanceportal.dto.AttendanceReportDTO(
            a.id,
            s.id,
            s.name,
            s.rollNumber,
            a.date,
            a.subject,
            a.present
        )
        FROM Attendance a
        JOIN a.student s
        WHERE s.id = :studentId 
          AND a.date BETWEEN :startDate AND :endDate
        ORDER BY a.date DESC
    """)
    List<AttendanceReportDTO> fetchStudentAttendanceReportByDateRange(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ================= STUDENT-SPECIFIC REPORT BY DATE RANGE AND SUBJECT =================
    @Query("""
        SELECT new com.example.attendanceportal.dto.AttendanceReportDTO(
            a.id,
            s.id,
            s.name,
            s.rollNumber,
            a.date,
            a.subject,
            a.present
        )
        FROM Attendance a
        JOIN a.student s
        WHERE s.id = :studentId 
          AND a.date BETWEEN :startDate AND :endDate
          AND a.subject = :subject
        ORDER BY a.date DESC
    """)
    List<AttendanceReportDTO> fetchStudentAttendanceReportByDateRangeAndSubject(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("subject") String subject);

    // ================= STUDENT SUMMARY =================
    @Query("""
        SELECT new com.example.attendanceportal.dto.StudentAttendanceSummaryDTO(
            s.id,
            s.name,
            s.rollNumber,
            COUNT(a.id),
            SUM(CASE WHEN a.present = true THEN 1 ELSE 0 END)
        )
        FROM Student s
        LEFT JOIN s.attendances a
        GROUP BY s.id, s.name, s.rollNumber
    """)
    List<StudentAttendanceSummaryDTO> fetchStudentAttendanceSummary();

    // ================= SUBJECT-WISE =================
    @Query("""
        SELECT new com.example.attendanceportal.dto.StudentSubjectAttendanceDTO(
            s.id,
            s.name,
            s.rollNumber,
            a.subject,
            COUNT(a.id),
            SUM(CASE WHEN a.present = true THEN 1 ELSE 0 END)
        )
        FROM Attendance a
        JOIN a.student s
        GROUP BY s.id, s.name, s.rollNumber, a.subject
        ORDER BY s.rollNumber, a.subject
    """)
    List<StudentSubjectAttendanceDTO> fetchStudentSubjectAttendance();

    // ================= MY SUBJECT ATTENDANCE =================
    @Query("""
        SELECT new com.example.attendanceportal.dto.StudentSubjectAttendanceDTO(
            s.id,
            s.name,
            s.rollNumber,
            a.subject,
            COUNT(a.id),
            SUM(CASE WHEN a.present = true THEN 1 ELSE 0 END)
        )
        FROM Attendance a
        JOIN a.student s
        WHERE s.rollNumber = :rollNumber
        GROUP BY s.id, s.name, s.rollNumber, a.subject
        ORDER BY a.subject
    """)
    List<StudentSubjectAttendanceDTO> fetchMySubjectAttendance(
            @Param("rollNumber") String rollNumber
    );

    // ================= GET ATTENDANCE FOR A SPECIFIC DATE AND SUBJECT =================
    @Query("""
        SELECT a FROM Attendance a 
        WHERE a.date = :date AND a.subject = :subject
    """)
    List<Attendance> findByDateAndSubject(
            @Param("date") LocalDate date,
            @Param("subject") String subject
    );
}