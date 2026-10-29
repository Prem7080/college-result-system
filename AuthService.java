package com.entc.resultportal.service;

import com.entc.resultportal.config.JwtUtil;
import com.entc.resultportal.dto.Dto;
import com.entc.resultportal.model.Teacher;
import com.entc.resultportal.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private TeacherRepository teacherRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Dto.LoginResponse login(Dto.LoginRequest req) {
        Teacher teacher = teacherRepo.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!teacher.getActive()) {
            throw new RuntimeException("Account is disabled. Contact administrator.");
        }

        if (!encoder.matches(req.getPassword(), teacher.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(teacher.getUsername());
        return new Dto.LoginResponse(token, teacher.getUsername(), teacher.getFullName(),
                "Login successful");
    }

    public Dto.MessageResponse changePassword(String username, Dto.ChangePasswordRequest req) {
        Teacher teacher = teacherRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        if (!encoder.matches(req.getCurrentPassword(), teacher.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        if (req.getNewPassword().length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters");
        }

        if (encoder.matches(req.getNewPassword(), teacher.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        teacher.setPassword(encoder.encode(req.getNewPassword()));
        teacherRepo.save(teacher);

        return new Dto.MessageResponse("Password changed successfully", true);
    }
}
