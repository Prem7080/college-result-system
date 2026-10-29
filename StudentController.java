package com.entc.resultportal.controller;

import com.entc.resultportal.dto.Dto;
import com.entc.resultportal.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private ExcelService excelService;

    /**
     * POST /api/student/result
     * Public — no token required
     */
    @PostMapping("/result")
    public ResponseEntity<?> getResult(@RequestBody Dto.ResultRequest req) {
        try {
            Dto.StudentResult result = excelService.getStudentResult(req);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new Dto.MessageResponse(e.getMessage(), false));
        }
    }
}
