package com.example.attendanceportal.config;

import com.example.attendanceportal.entity.User;
import com.example.attendanceportal.repository.UserRepository;
import com.example.attendanceportal.security.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // Create TEACHER user if not exists
        if (userRepository.findByUsername("teacher@gmail.com").isEmpty()) {

            User teacher = new User();
            teacher.setUsername("teacher@gmail.com");
            teacher.setPassword(passwordEncoder.encode("123456"));
            teacher.setRole(Role.ROLE_TEACHER);
            teacher.setEnabled(true);

            userRepository.save(teacher);

            System.out.println("✅ Default teacher user created!");
        }

        // Create ADMIN user if not exists
        if (userRepository.findByUsername("admin@gmail.com").isEmpty()) {

            User admin = new User();
            admin.setUsername("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ROLE_ADMIN);
            admin.setEnabled(true);

            userRepository.save(admin);

            System.out.println("✅ Default admin user created!");
        }
    }
}
