package com.corecompass.report.controller;

import com.corecompass.report.dto.*;
import com.corecompass.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * GET /api/v1/reports/weekly
     * Returns paginated weekly report summaries for the authenticated user.
     */
    @GetMapping("/weekly")
    public ResponseEntity<Page<ReportSummaryDTO>> listWeekly(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(
            reportService.listReports(userId, PageRequest.of(page, size)));
    }

    /**
     * GET /api/v1/reports/weekly/latest
     * Returns the most recent report, or 204 if none exist yet.
     */
    @GetMapping("/weekly/latest")
    public ResponseEntity<WeeklyReportDTO> getLatest(
            @RequestHeader("X-User-Id") UUID userId) {
        WeeklyReportDTO dto = reportService.getLatestReport(userId);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/reports/weekly/{id}
     * Returns full report by ID, scoped to the requesting user.
     */
    @GetMapping("/weekly/{id}")
    public ResponseEntity<WeeklyReportDTO> getReport(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(reportService.getReport(userId, id));
    }

    /**
     * POST /api/v1/reports/generate
     * Triggers report generation for the requesting user (on-demand).
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateMyReport(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody(required = false) GenerateReportRequest req) {
        String weekStart = req != null ? req.getWeekStart() : null;
        reportService.generateReportForUser(userId, weekStart);
        return ResponseEntity.accepted().body("Report generation started");
    }

    /**
     * POST /api/v1/reports/admin/generate-all
     * Admin-only: triggers generation for all active users.
     * Gateway should restrict this to ADMIN role via X-User-Role header.
     */
    @PostMapping("/admin/generate-all")
    public ResponseEntity<String> adminGenerateAll(
            @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Admin access required");
        }
        reportService.generateForAllUsers();
        return ResponseEntity.accepted().body("Full report generation triggered");
    }
}
