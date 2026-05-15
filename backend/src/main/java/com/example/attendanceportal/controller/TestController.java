package com.example.attendanceportal.controller;

import com.example.attendanceportal.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/mail")
    public String testMail() {
        emailService.sendOtp("yourgmail@gmail.com", "123456");
        return "Mail Sent Successfully!";
    }
}