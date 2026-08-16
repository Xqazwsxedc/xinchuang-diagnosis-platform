package com.xinchuang.diagnosis.ledger.controller;

import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.ledger.dto.DbInstanceLogPageQuery;
import com.xinchuang.diagnosis.ledger.dto.DbMetricRangeQuery;
import com.xinchuang.diagnosis.ledger.service.LedgerDetailService;
import com.xinchuang.diagnosis.ledger.vo.BusinessDataRecordVO;
import com.xinchuang.diagnosis.ledger.vo.DbInstanceLogVO;
import com.xinchuang.diagnosis.ledger.vo.DbMetricsOverviewVO;
import com.xinchuang.diagnosis.ledger.vo.LedgerDownload;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/ledger")
public class LedgerDetailController {
    private final LedgerDetailService ledgerDetailService;

    public LedgerDetailController(LedgerDetailService ledgerDetailService) {
        this.ledgerDetailService = ledgerDetailService;
    }

    @GetMapping("/business-systems/{id}/data-records")
    public ApiResponse<List<BusinessDataRecordVO>> businessDataRecords(@PathVariable Long id) {
        return ApiResponse.ok(ledgerDetailService.businessDataRecords(id));
    }

    @GetMapping("/business-systems/{id}/data-records/download")
    public ResponseEntity<byte[]> downloadBusinessData(@PathVariable Long id,
                                                        @RequestParam(required = false) String recordIds) {
        return csvResponse(ledgerDetailService.downloadBusinessData(id, parseSelectedIds(recordIds)));
    }

    @GetMapping("/business-systems/{id}/data-records/{recordId}/download")
    public ResponseEntity<byte[]> downloadBusinessDataRecord(@PathVariable Long id, @PathVariable Long recordId) {
        return csvResponse(ledgerDetailService.downloadBusinessData(id, List.of(recordId)));
    }

    @GetMapping("/db-resources/{id}/metrics")
    public ApiResponse<DbMetricsOverviewVO> dbMetrics(@PathVariable Long id, @Valid @ModelAttribute DbMetricRangeQuery query) {
        return ApiResponse.ok(ledgerDetailService.dbMetrics(id, query));
    }

    @GetMapping("/db-resources/{id}/metrics/download")
    public ResponseEntity<byte[]> downloadDbMetrics(@PathVariable Long id, @Valid @ModelAttribute DbMetricRangeQuery query) {
        return csvResponse(ledgerDetailService.downloadDbMetrics(id, query));
    }

    @GetMapping("/db-resources/{id}/logs")
    public ApiResponse<PageResponse<DbInstanceLogVO>> dbLogs(@PathVariable Long id, @Valid @ModelAttribute DbInstanceLogPageQuery query) {
        return ApiResponse.ok(ledgerDetailService.dbLogs(id, query));
    }

    @GetMapping("/db-resources/{id}/logs/download")
    public ResponseEntity<byte[]> downloadDbLogs(@PathVariable Long id,
                                                  @Valid @ModelAttribute DbInstanceLogPageQuery query,
                                                  @RequestParam(required = false) String logIds) {
        return csvResponse(ledgerDetailService.downloadDbLogs(id, query, parseSelectedIds(logIds)));
    }

    private ResponseEntity<byte[]> csvResponse(LedgerDownload download) {
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename(download.fileName(), StandardCharsets.UTF_8)
                .build()
                .toString())
            .body(download.content());
    }

    private List<Long> parseSelectedIds(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        String[] parts = value.split(",");
        if (parts.length > 100) {
            throw new BusinessException("单次最多下载100条记录");
        }
        List<Long> ids = new ArrayList<>(parts.length);
        try {
            for (String part : parts) {
                Long id = Long.valueOf(part.trim());
                if (id <= 0) {
                    throw new NumberFormatException("ID must be positive");
                }
                if (!ids.contains(id)) {
                    ids.add(id);
                }
            }
        } catch (NumberFormatException exception) {
            throw new BusinessException("下载记录ID格式不正确");
        }
        return ids;
    }
}
