package com.example.attendanceportal.dto;

public class OtpRequest {

    // 🔥 This replaces email
    private String identifier; // rollNumber OR systemId

    private String otp;

    public OtpRequest() {
    }

    public OtpRequest(String identifier, String otp) {
        this.identifier = identifier;
        this.otp = otp;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}