package com.entc.resultportal.controller;

import com.entc.resultportal.dto.Dto;
import com.entc.resultportal.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class ExcelController {

    @Autowired
    private ExcelService excelService;

    /**
     * GET /api/admin/test — test endpoint (no auth required)
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(new Dto.MessageResponse("Backend is working", true));
    }

    /**
     * POST /api/admin/test-upload — debug multipart parsing (no auth required)
     */
    @PostMapping("/test-upload")
    public ResponseEntity<?> testUpload(
            @RequestParam String year,
            @RequestParam String semester,
            @RequestParam MultipartFile file) {
        try {
            System.out.println("=== TEST UPLOAD ===");
            System.out.println("Year: " + year);
            System.out.println("Semester: " + semester);
            System.out.println("File: " + file.getOriginalFilename() + " | Size: " + file.getSize());
            return ResponseEntity.ok(new Dto.MessageResponse(
                "Test received: " + year + " " + semester + " " + file.getOriginalFilename(), true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new Dto.MessageResponse("Test failed: " + e.getMessage(), false));
        }
    }

    /**
     * GET /api/admin/sheets  — list all uploaded sheets
     */
    @GetMapping("/sheets")
    public ResponseEntity<List<Dto.SheetInfo>> getSheets() {
        return ResponseEntity.ok(excelService.getAllSheets());
    }

    /**
     * POST /api/admin/sheets/upload  — upload a new Excel sheet
     */
    @PostMapping("/sheets/upload")
    public ResponseEntity<?> upload(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) MultipartFile file) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // Method Authentication param is often null on multipart; read from SecurityContext instead
            boolean anonymous = auth == null || auth instanceof AnonymousAuthenticationToken;
            String teacherUsername = anonymous ? null : auth.getName();

            System.out.println("=== UPLOAD REQUEST ===");
            System.out.println("Year: " + year + " | Semester: " + semester);
            System.out.println("File: " + (file != null ? file.getOriginalFilename() : "null"));
            System.out.println("Auth: " + (teacherUsername != null ? teacherUsername : "null"));

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new Dto.MessageResponse("File is empty or missing", false));
            }
            if (year == null || year.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new Dto.MessageResponse("Year is required", false));
            }
            if (semester == null || semester.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new Dto.MessageResponse("Semester is required", false));
            }
            if (teacherUsername == null || teacherUsername.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(new Dto.MessageResponse("Authentication required", false));
            }

            Dto.MessageResponse resp = excelService.uploadSheet(year, semester, file, teacherUsername);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            System.out.println("=== UPLOAD ERROR ===");
            e.printStackTrace();
            String error = e.getMessage() != null ? e.getMessage() : e.toString();
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                error = error + " | " + e.getCause().getMessage();
            }
            return ResponseEntity.badRequest()
                    .body(new Dto.MessageResponse("Upload failed: " + error, false));
        }
    }

    /**
     * DELETE /api/admin/sheets/{id}  — delete a sheet
     */
    @DeleteMapping("/sheets/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            Dto.MessageResponse resp = excelService.deleteSheet(id);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new Dto.MessageResponse(e.getMessage(), false));
        }
    }
}
