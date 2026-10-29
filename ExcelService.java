package com.entc.resultportal.service;

import com.entc.resultportal.dto.Dto;
import com.entc.resultportal.model.ExcelSheet;
import com.entc.resultportal.model.Teacher;
import com.entc.resultportal.repository.ExcelSheetRepository;
import com.entc.resultportal.repository.TeacherRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.result.passing-marks:12}")
    private int passingMarks;

    @Autowired
    private ExcelSheetRepository sheetRepo;

    @Autowired
    private TeacherRepository teacherRepo;

    // ── Upload ─────────────────────────────────────────────────
    @Transactional(rollbackFor = Exception.class)
    public Dto.MessageResponse uploadSheet(String year, String semester,
                                           MultipartFile file, String teacherUsername) throws IOException {
        System.out.println("=== UPLOAD START ===");
        System.out.println("Year: " + year + " | Semester: " + semester);
        System.out.println("File: " + file.getOriginalFilename() + " | Size: " + file.getSize());
        System.out.println("Teacher: " + teacherUsername);
        
        try {
            // Validate inputs
            if (file == null || file.isEmpty()) {
                throw new IOException("File is empty");
            }
            if (year == null || year.isBlank()) {
                throw new IOException("Year is required");
            }
            if (semester == null || semester.isBlank()) {
                throw new IOException("Semester is required");
            }
            
            // Delete existing sheet for same year+semester
            sheetRepo.findByYearAndSemester(year, semester).ifPresent(existing -> {
                try { Files.deleteIfExists(Path.of(existing.getFilePath())); } catch (IOException ignored) {}
                sheetRepo.delete(existing);
            });

            // Create upload directory
            Path dir = Path.of(uploadDir).toAbsolutePath().normalize();
            System.out.println("Upload dir: " + dir);
            Files.createDirectories(dir);

            // Save file safely
            String origName = file.getOriginalFilename();
            if (origName == null || origName.isBlank()) origName = "upload.xlsx";
            String safeLeaf = origName.replace('\\', '_').replace('/', '_');
            String stored = year + "_" + semester + "_" + System.currentTimeMillis() + "_" + safeLeaf;
            Path dest = dir.resolve(stored).normalize();
            
            System.out.println("Saving to: " + dest);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("File saved: " + Files.size(dest) + " bytes");

            // Try to detect header row, but don't fail if Excel parsing fails
            int headerRow = 5;
            try {
                headerRow = detectHeaderRow(dest.toFile());
                System.out.println("Detected header at row: " + headerRow);
            } catch (Exception e) {
                System.out.println("Could not detect header, using default: " + e.getMessage());
            }

            // Get teacher
            Teacher teacher = teacherRepo.findByUsername(teacherUsername).orElse(null);
            System.out.println("Teacher: " + (teacher != null ? teacher.getFullName() : "null"));

            // Create and save record
            ExcelSheet sheet = ExcelSheet.builder()
                    .year(year)
                    .semester(semester)
                    .originalFileName(origName)
                    .storedFileName(stored)
                    .filePath(dest.toString())
                    .uploadedBy(teacher)
                    .headerRowIndex(headerRow)
                    .dataStartRowIndex(headerRow + 2)
                    .build();
            sheet = sheetRepo.save(sheet);
            System.out.println("=== UPLOAD SUCCESS ID=" + sheet.getId() + " ===");
            return new Dto.MessageResponse("Sheet uploaded successfully for " + year + " " + semester, true);
        } catch (Exception e) {
            System.out.println("=== UPLOAD FAILED ===");
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : "Upload error";
            throw new IOException("Upload failed: " + msg, e);
        }
    }

    // ── Delete ─────────────────────────────────────────────────
    public Dto.MessageResponse deleteSheet(Long id) {
        ExcelSheet sheet = sheetRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Sheet not found"));
        try { Files.deleteIfExists(Path.of(sheet.getFilePath())); } catch (IOException ignored) {}
        sheetRepo.delete(sheet);
        return new Dto.MessageResponse("Sheet deleted successfully", true);
    }

    // ── List all ───────────────────────────────────────────────
    public List<Dto.SheetInfo> getAllSheets() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        return sheetRepo.findAllByOrderByUploadedAtDesc().stream().map(s -> Dto.SheetInfo.builder()
                .id(s.getId())
                .year(s.getYear())
                .semester(s.getSemester())
                .fileName(s.getOriginalFileName())
                .uploadedBy(s.getUploadedBy() != null ? s.getUploadedBy().getFullName() : "—")
                .uploadedAt(s.getUploadedAt() != null ? s.getUploadedAt().format(fmt) : "—")
                .build()
        ).collect(Collectors.toList());
    }

    // ── Find student result ────────────────────────────────────
    public Dto.StudentResult getStudentResult(Dto.ResultRequest req) throws IOException {
        ExcelSheet sheet = sheetRepo.findByYearAndSemester(req.getYear(), req.getSemester())
                .orElseThrow(() -> new RuntimeException(
                        "No result sheet found for " + req.getYear() + " " + req.getSemester() +
                        ". Please check back later."));

        return parseAndFindStudent(sheet, req.getPrn().trim(), req.getMotherName().trim());
    }

    // ── Private: Excel parsing ─────────────────────────────────
    private int detectHeaderRow(File file) throws IOException {
        try (Workbook wb = WorkbookFactory.create(file)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 0; i <= Math.min(10, sheet.getLastRowNum()); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                for (Cell cell : row) {
                    String val = getCellString(cell).toUpperCase();
                    if (val.contains("PRN") || val.contains("ROLL") || val.contains("STUDENT NAME")) {
                        return i;
                    }
                }
            }
        }
        return 5; // default
    }

    private Dto.StudentResult parseAndFindStudent(ExcelSheet sheetMeta, String prn, String motherName)
            throws IOException {

        File file = new File(sheetMeta.getFilePath());
        if (!file.exists()) throw new RuntimeException("Result file not found on server.");

        try (Workbook wb = WorkbookFactory.create(file)) {
            Sheet sheet = wb.getSheetAt(0);
            int headerIdx = sheetMeta.getHeaderRowIndex();
            int dataIdx   = sheetMeta.getDataStartRowIndex();

            Row headerRow = sheet.getRow(headerIdx);
            if (headerRow == null) throw new RuntimeException("Invalid Excel format.");

            // Map column index → header name
            Map<Integer, String> colMap = new LinkedHashMap<>();
            for (Cell cell : headerRow) {
                String h = getCellString(cell).trim();
                if (!h.isEmpty()) colMap.put(cell.getColumnIndex(), h);
            }

            // Identify key columns
            int prnCol = -1, rollCol = -1, nameCol = -1, motherCol = -1;
            Set<String> nonSubject = Set.of("roll", "prn", "name", "mother", "seat",
                    "total", "percentage", "result", "sr", "no", "class", "div", "division");

            for (Map.Entry<Integer, String> e : colMap.entrySet()) {
                String h = e.getValue().toLowerCase();
                if ((h.contains("prn") || h.contains("prn no")) && prnCol < 0) prnCol = e.getKey();
                else if ((h.contains("roll") || h.contains("roll no")) && rollCol < 0) rollCol = e.getKey();
                else if (h.contains("student name") && nameCol < 0) nameCol = e.getKey();
                else if (h.contains("mother") && motherCol < 0) motherCol = e.getKey();
            }

            // Max marks row (one row after header)
            Row maxRow = sheet.getRow(headerIdx + 1);

            // Find subject columns
            List<Integer> subjectCols = new ArrayList<>();
            for (Map.Entry<Integer, String> e : colMap.entrySet()) {
                int ci = e.getKey();
                if (ci == prnCol || ci == rollCol || ci == nameCol || ci == motherCol) continue;
                String h = e.getValue().toLowerCase();
                boolean skip = nonSubject.stream().anyMatch(ns ->
                        h.equals(ns) || h.startsWith(ns + " ") || h.endsWith(" " + ns));
                if (!skip) subjectCols.add(ci);
            }

            // Search for student
            for (int r = dataIdx; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String rowPrn    = prnCol >= 0 ? getCellString(row.getCell(prnCol)).trim() : "";
                String rowMother = motherCol >= 0 ? getCellString(row.getCell(motherCol)).trim() : "";

                if (!rowPrn.equalsIgnoreCase(prn)) continue;
                if (!rowMother.equalsIgnoreCase(motherName)) {
                    throw new RuntimeException("PRN found but Mother's Name does not match. Please check and try again.");
                }

                // Build subject results
                List<Dto.SubjectResult> subjects = new ArrayList<>();
                boolean overallPass = true;
                for (int ci : subjectCols) {
                    String subName = colMap.get(ci);
                    Cell dataCell = row.getCell(ci);
                    String marksStr = getCellString(dataCell).trim();

                    int maxM = 30;
                    if (maxRow != null) {
                        String maxStr = getCellString(maxRow.getCell(ci)).trim();
                        try { maxM = (int) Double.parseDouble(maxStr); } catch (Exception ignored) {}
                    }

                    boolean passed;
                    if (marksStr.equalsIgnoreCase("AB") || marksStr.isEmpty()) {
                        passed = false;
                        marksStr = marksStr.isEmpty() ? "—" : "AB";
                    } else {
                        try {
                            double m = Double.parseDouble(marksStr);
                            passed = m >= passingMarks;
                            marksStr = String.valueOf((int) m);
                        } catch (NumberFormatException e) {
                            passed = false;
                        }
                    }
                    if (!passed) overallPass = false;

                    subjects.add(Dto.SubjectResult.builder()
                            .subjectName(subName)
                            .marksObtained(marksStr)
                            .maxMarks(maxM)
                            .passingMarks(passingMarks)
                            .passed(passed)
                            .build());
                }

                String studentName = nameCol >= 0 ? getCellString(row.getCell(nameCol)).trim() : "—";
                String rollNo      = rollCol >= 0 ? getCellString(row.getCell(rollCol)).trim() : "—";
                String uploadDate  = sheetMeta.getUploadedAt() != null
                        ? sheetMeta.getUploadedAt().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
                        : "—";

                return Dto.StudentResult.builder()
                        .studentName(studentName)
                        .prn(prn.toUpperCase())
                        .rollNo(rollNo)
                        .motherName(motherName)
                        .year(sheetMeta.getYear())
                        .semester(sheetMeta.getSemester())
                        .resultDeclaredOn(uploadDate)
                        .subjects(subjects)
                        .overallPass(overallPass)
                        .build();
            }

            throw new RuntimeException("Student with PRN " + prn + " not found in the result sheet.");
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) yield cell.getLocalDateTimeCellValue().toString();
                double d = cell.getNumericCellValue();
                yield d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield String.valueOf(cell.getNumericCellValue()); }
                catch (Exception e) { yield cell.getStringCellValue(); }
            }
            default -> "";
        };
    }
}
