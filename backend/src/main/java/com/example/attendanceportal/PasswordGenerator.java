package com.example.attendanceportal;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("Admin Hash: " + encoder.encode("admin123"));
        System.out.println("Teacher Hash: " + encoder.encode("teacher123"));
        System.out.println("Student Hash: " + encoder.encode("student123"));
    }
}