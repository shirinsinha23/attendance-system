package com.example.attendanceportal.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET =
            "mySuperSecretKeyForJwtAuthenticationMySuperSecretKey";

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    private static final long EXPIRATION_TIME =
            1000 * 60 * 60 * 24; // 24 hours

    // ✅ Generate Token
    public String generateToken(String username, String role) {
        System.out.println("🎫 Generating token for username: " + username + ", role: " + role);

        String token = Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + EXPIRATION_TIME)
                )
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        System.out.println("🎫 Token generated successfully, length: " + token.length());
        return token;
    }

    // ✅ Extract Username
    public String extractUsername(String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (Exception e) {
            System.out.println("❌ Error extracting username: " + e.getMessage());
            return null;
        }
    }

    // ✅ Extract Role
    public String extractRole(String token) {
        try {
            String role = extractAllClaims(token).get("role", String.class);
            System.out.println("🔑 Extracted role from token: " + role);
            return role;
        } catch (Exception e) {
            System.out.println("❌ Error extracting role: " + e.getMessage());
            return null;
        }
    }

    // ✅ Validate Token (UPDATED - no UserDetails needed)
    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            boolean isValid = (extractedUsername.equals(username) && !isTokenExpired(token));
            System.out.println("🔐 Token validation for " + username + ": " + isValid);
            return isValid;
        } catch (Exception e) {
            System.out.println("❌ Token validation error: " + e.getMessage());
            return false;
        }
    }

    // ✅ Check Expiration
    private Boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        boolean isExpired = expiration.before(new Date());
        System.out.println("📅 Token expires: " + expiration + ", isExpired: " + isExpired);
        return isExpired;
    }

    // ✅ Extract Claims
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}