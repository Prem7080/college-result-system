package com.entc.resultportal.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.List;

// ── Auth DTOs ──────────────────────────────────────────────────
public class Dto {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class LoginResponse {
        private String token;
        private String username;
        private String fullName;
        private String message;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
        private String confirmPassword;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class MessageResponse {
        private String message;
        private boolean success;
    }

    // ── Student / Result DTOs ──────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ResultRequest {
        private String prn;
        private String motherName;
        private String year;
        private String semester;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SubjectResult {
        private String subjectName;
        private String marksObtained;
        private int maxMarks;
        private int passingMarks;
        private boolean passed;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StudentResult {
        private String studentName;
        private String prn;
        private String rollNo;
        private String motherName;
        private String year;
        private String semester;
        private String resultDeclaredOn;
        private List<SubjectResult> subjects;
        private boolean overallPass;
    }

    // ── Excel Sheet DTO ────────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SheetInfo {
        private Long id;
        private String year;
        private String semester;
        private String fileName;
        private String uploadedBy;
        private String uploadedAt;
    }
}
