package com.example.attendanceportal.security;

import com.example.attendanceportal.entity.Student;
import com.example.attendanceportal.entity.Teacher;
import com.example.attendanceportal.entity.User;
import com.example.attendanceportal.repository.StudentRepository;
import com.example.attendanceportal.repository.TeacherRepository;
import com.example.attendanceportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // First, try to find in User table (for Admin)
        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null) {
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword() != null ? user.getPassword() : "",
                    user.isEnabled(),
                    true,
                    true,
                    true,
                    Collections.singletonList(
                            new SimpleGrantedAuthority(user.getRole().name())
                    )
            );
        }

        // Then, try Student table (for Student login)
        Student student = studentRepository.findByRollNumber(username).orElse(null);

        if (student != null) {
            return new org.springframework.security.core.userdetails.User(
                    student.getRollNumber(),
                    "",
                    true,
                    true,
                    true,
                    true,
                    Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_STUDENT")
                    )
            );
        }

        // Finally, try Teacher table (for Teacher login)
        Teacher teacher = teacherRepository.findByEmail(username).orElse(null);

        if (teacher != null) {
            return new org.springframework.security.core.userdetails.User(
                    teacher.getEmail(),
                    "",
                    teacher.isEnabled(),
                    true,
                    true,
                    true,
                    Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_TEACHER")
                    )
            );
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}