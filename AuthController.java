package com.entc.resultportal.controller;

import com.entc.resultportal.dto.Dto;
import com.entc.resultportal.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Dto.LoginRequest req) {
        try {
            Dto.LoginResponse resp = authService.login(req);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(new Dto.MessageResponse(e.getMessage(), false));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Dto.ChangePasswordRequest req) {
        try {
            String username = currentUsername();
            Dto.MessageResponse resp = authService.changePassword(username, req);
            return ResponseEntity.ok(resp);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401)
                    .body(new Dto.MessageResponse(e.getMessage(), false));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new Dto.MessageResponse(e.getMessage(), false));
        }
    }

    private static String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("Not authenticated");
        }
        return auth.getName();
    }
}
