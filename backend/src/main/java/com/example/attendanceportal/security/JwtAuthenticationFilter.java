package com.example.attendanceportal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Log the request path
        String path = request.getRequestURI();
        System.out.println("🔍 Request path: " + path);

        // Skip authentication for OPTIONS requests (CORS preflight)
        if (request.getMethod().equals("OPTIONS")) {
            System.out.println("⏭️ Skipping OPTIONS request");
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        // Log auth header presence
        if (authHeader != null) {
            System.out.println("📝 Auth header present, length: " + authHeader.length());
            System.out.println("📝 Auth header starts with Bearer: " + authHeader.startsWith("Bearer "));
        } else {
            System.out.println("❌ No Authorization header found");
        }

        String username = null;
        String token = null;
        String role = null;

        try {
            // ✅ STEP 1: Extract token from header
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7).trim();
                System.out.println("🔑 Token extracted, length: " + token.length());

                // Check if token has dots (valid JWT format)
                int dotCount = token.length() - token.replace(".", "").length();
                System.out.println("🔑 Token dot count: " + dotCount);

                // ✅ STEP 2: Extract username and role from token
                if (token != null && !token.isEmpty() && token.contains(".")) {
                    try {
                        username = jwtUtil.extractUsername(token);
                        role = jwtUtil.extractRole(token);

                        System.out.println("✅ JWT extracted - Username: " + username + ", Role: " + role);

                        if (username == null || username.isEmpty()) {
                            System.out.println("⚠️ Username is null or empty!");
                        }
                        if (role == null || role.isEmpty()) {
                            System.out.println("⚠️ Role is null or empty!");
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Error extracting from token: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("⚠️ Token doesn't contain dots - not a valid JWT format");
                }
            }

            // ✅ STEP 3: Set authentication in context
            if (username != null &&
                    role != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                System.out.println("🔐 Attempting to validate token for user: " + username);

                // Validate token
                boolean isValid = jwtUtil.validateToken(token, username);
                System.out.println("Token valid: " + isValid);

                if (isValid) {

                    // Format role properly (ensure it has ROLE_ prefix)
                    String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    System.out.println("Setting authentication with role: " + formattedRole);

                    // Create authentication token with the role
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    Collections.singletonList(
                                            new SimpleGrantedAuthority(formattedRole)
                                    )
                            );

                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authenticationToken);

                    System.out.println("✅ Authentication set for user: " + username + " with role: " + formattedRole);
                } else {
                    System.out.println("❌ Token validation failed for user: " + username);
                }
            } else {
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    System.out.println("⚠️ Could not extract username/role from token or auth already set");
                    System.out.println("   - Username: " + username);
                    System.out.println("   - Role: " + role);
                    System.out.println("   - Auth already set: " + (SecurityContextHolder.getContext().getAuthentication() != null));
                }
            }

        } catch (Exception e) {
            System.out.println("❌ JWT Authentication Error: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}