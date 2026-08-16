package com.xinchuang.diagnosis.ai.controller;

import com.xinchuang.diagnosis.ai.dto.AiPageQuery;
import com.xinchuang.diagnosis.ai.dto.ResultHandleRequest;
import com.xinchuang.diagnosis.ai.entity.DefectReport;
import com.xinchuang.diagnosis.ai.entity.FaultReport;
import com.xinchuang.diagnosis.ai.entity.OptimizationReport;
import com.xinchuang.diagnosis.ai.service.AiManagementService;
import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.common.api.PageResponse;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiReportController {
    private final AiManagementService aiManagementService;

    public AiReportController(AiManagementService aiManagementService) {
        this.aiManagementService = aiManagementService;
    }

    @GetMapping("/fault-reports")
    public ApiResponse<PageResponse<FaultReport>> faultPage(@Valid @ModelAttribute AiPageQuery query) {
        return ApiResponse.ok(aiManagementService.pageFaultReports(query));
    }

    @GetMapping("/fault-reports/{id}")
    public ApiResponse<FaultReport> faultDetail(@PathVariable Long id) {
        return ApiResponse.ok(aiManagementService.faultReportDetail(id));
    }

    @PostMapping("/fault-reports")
    public ApiResponse<FaultReport> createFault(@RequestBody FaultReport request) {
        return ApiResponse.ok(aiManagementService.createFaultReport(request));
    }

    @PutMapping("/fault-reports/{id}")
    public ApiResponse<FaultReport> updateFault(@PathVariable Long id, @RequestBody FaultReport request) {
        return ApiResponse.ok(aiManagementService.updateFaultReport(id, request));
    }

    @PatchMapping("/fault-reports/{id}/handle")
    public ApiResponse<Void> handleFault(@PathVariable Long id, @Valid @RequestBody ResultHandleRequest request) {
        aiManagementService.handleFaultReport(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/fault-reports/{id}")
    public ApiResponse<Void> deleteFault(@PathVariable Long id) {
        aiManagementService.deleteFaultReport(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/defect-reports")
    public ApiResponse<PageResponse<DefectReport>> defectPage(@Valid @ModelAttribute AiPageQuery query) {
        return ApiResponse.ok(aiManagementService.pageDefectReports(query));
    }

    @GetMapping("/defect-reports/{id}")
    public ApiResponse<DefectReport> defectDetail(@PathVariable Long id) {
        return ApiResponse.ok(aiManagementService.defectReportDetail(id));
    }

    @GetMapping("/defect-reports/export")
    public ResponseEntity<byte[]> exportDefect(@org.springframework.web.bind.annotation.RequestParam List<Long> reportIds) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("缺陷检测报表.csv", StandardCharsets.UTF_8).build().toString())
            .body(aiManagementService.exportDefectReports(reportIds));
    }

    @PostMapping("/defect-reports")
    public ApiResponse<DefectReport> createDefect(@RequestBody DefectReport request) {
        return ApiResponse.ok(aiManagementService.createDefectReport(request));
    }

    @PutMapping("/defect-reports/{id}")
    public ApiResponse<DefectReport> updateDefect(@PathVariable Long id, @RequestBody DefectReport request) {
        return ApiResponse.ok(aiManagementService.updateDefectReport(id, request));
    }

    @PatchMapping("/defect-reports/{id}/handle")
    public ApiResponse<Void> handleDefect(@PathVariable Long id, @Valid @RequestBody ResultHandleRequest request) {
        aiManagementService.handleDefectReport(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/defect-reports/{id}")
    public ApiResponse<Void> deleteDefect(@PathVariable Long id) {
        aiManagementService.deleteDefectReport(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/optimization-reports")
    public ApiResponse<PageResponse<OptimizationReport>> optimizationPage(@Valid @ModelAttribute AiPageQuery query) {
        return ApiResponse.ok(aiManagementService.pageOptimizationReports(query));
    }

    @GetMapping("/optimization-reports/{id}")
    public ApiResponse<OptimizationReport> optimizationDetail(@PathVariable Long id) {
        return ApiResponse.ok(aiManagementService.optimizationReportDetail(id));
    }

    @GetMapping("/optimization-reports/export")
    public ResponseEntity<byte[]> exportOptimization(@org.springframework.web.bind.annotation.RequestParam List<Long> reportIds) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("性能优化建议.csv", StandardCharsets.UTF_8).build().toString())
            .body(aiManagementService.exportOptimizationReports(reportIds));
    }

    @PostMapping("/optimization-reports")
    public ApiResponse<OptimizationReport> createOptimization(@RequestBody OptimizationReport request) {
        return ApiResponse.ok(aiManagementService.createOptimizationReport(request));
    }

    @PutMapping("/optimization-reports/{id}")
    public ApiResponse<OptimizationReport> updateOptimization(@PathVariable Long id, @RequestBody OptimizationReport request) {
        return ApiResponse.ok(aiManagementService.updateOptimizationReport(id, request));
    }

    @PatchMapping("/optimization-reports/{id}/handle")
    public ApiResponse<Void> handleOptimization(@PathVariable Long id, @Valid @RequestBody ResultHandleRequest request) {
        aiManagementService.handleOptimizationReport(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/optimization-reports/{id}")
    public ApiResponse<Void> deleteOptimization(@PathVariable Long id) {
        aiManagementService.deleteOptimizationReport(id);
        return ApiResponse.ok(null);
    }
}
