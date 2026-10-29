package com.entc.resultportal.config;

import com.entc.resultportal.model.Teacher;
import com.entc.resultportal.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private TeacherRepository teacherRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (!teacherRepo.existsByUsername("admin")) {
            Teacher admin = Teacher.builder()
                    .username("admin")
                    .password(encoder.encode("Admin@1234"))
                    .email("admin@entc.edu")
                    .fullName("Admin Teacher")
                    .active(true)
                    .build();
            teacherRepo.save(admin);
            System.out.println("✅ Default admin created — username: admin | password: Admin@1234");
        }
    }
}
