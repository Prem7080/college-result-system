package com.entc.resultportal.repository;

import com.entc.resultportal.model.ExcelSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExcelSheetRepository extends JpaRepository<ExcelSheet, Long> {
    Optional<ExcelSheet> findByYearAndSemester(String year, String semester);
    List<ExcelSheet> findAllByOrderByUploadedAtDesc();
}
