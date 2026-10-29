package com.entc.resultportal.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "excel_sheets",
    uniqueConstraints = @UniqueConstraint(columnNames = {"year", "semester"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year", nullable = false, length = 10)
    private String year;   // FE, SE, TE, BE

    @Column(name = "semester", nullable = false, length = 10)
    private String semester; // Sem1 … Sem8

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false)
    private String storedFileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher uploadedBy;

    @Builder.Default
    @Column(name = "header_row_index")
    private int headerRowIndex = 5;

    @Builder.Default
    @Column(name = "data_start_row_index")
    private int dataStartRowIndex = 7;

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) uploadedAt = LocalDateTime.now();
    }
}
