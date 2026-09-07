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

/**
 * 数据台账详情模块的 REST 入口。
 *
 * <p>Controller 只负责路由、参数绑定和响应格式，数据库查询、Prometheus
 * 调用以及导出审计均下沉到 {@link LedgerDetailService}。</p>
 */
@RestController
@RequestMapping("/ledger")
public class LedgerDetailController {
    private final LedgerDetailService ledgerDetailService;

    public LedgerDetailController(LedgerDetailService ledgerDetailService) {
        this.ledgerDetailService = ledgerDetailService;
    }

    /** 查询业务系统下按收集时间倒序排列的数据记录。 */
    @GetMapping("/business-systems/{id}/data-records")
    public ApiResponse<List<BusinessDataRecordVO>> businessDataRecords(@PathVariable Long id) {
        return ApiResponse.ok(ledgerDetailService.businessDataRecords(id));
    }

    /** 下载业务系统的全部数据，或通过 recordIds 下载选中的记录。 */
    @GetMapping("/business-systems/{id}/data-records/download")
    public ResponseEntity<byte[]> downloadBusinessData(@PathVariable Long id,
                                                        @RequestParam(required = false) String recordIds) {
        return csvResponse(ledgerDetailService.downloadBusinessData(id, parseSelectedIds(recordIds)));
    }

    /** 复用批量下载逻辑，下载单条业务数据记录。 */
    @GetMapping("/business-systems/{id}/data-records/{recordId}/download")
    public ResponseEntity<byte[]> downloadBusinessDataRecord(@PathVariable Long id, @PathVariable Long recordId) {
        return csvResponse(ledgerDetailService.downloadBusinessData(id, List.of(recordId)));
    }

    /** 查询数据库资源的实时指标和历史时间序列。 */
    @GetMapping("/db-resources/{id}/metrics")
    public ApiResponse<DbMetricsOverviewVO> dbMetrics(@PathVariable Long id, @Valid @ModelAttribute DbMetricRangeQuery query) {
        return ApiResponse.ok(ledgerDetailService.dbMetrics(id, query));
    }

    /** 按时间范围和指标选择导出数据库监控指标。 */
    @GetMapping("/db-resources/{id}/metrics/download")
    public ResponseEntity<byte[]> downloadDbMetrics(@PathVariable Long id, @Valid @ModelAttribute DbMetricRangeQuery query) {
        return csvResponse(ledgerDetailService.downloadDbMetrics(id, query));
    }

    /** 分页查询数据库资源的业务日志。 */
    @GetMapping("/db-resources/{id}/logs")
    public ApiResponse<PageResponse<DbInstanceLogVO>> dbLogs(@PathVariable Long id, @Valid @ModelAttribute DbInstanceLogPageQuery query) {
        return ApiResponse.ok(ledgerDetailService.dbLogs(id, query));
    }

    /** 按时间范围、日志级别和选中的日志 ID 导出日志。 */
    @GetMapping("/db-resources/{id}/logs/download")
    public ResponseEntity<byte[]> downloadDbLogs(@PathVariable Long id,
                                                  @Valid @ModelAttribute DbInstanceLogPageQuery query,
                                                  @RequestParam(required = false) String logIds) {
        return csvResponse(ledgerDetailService.downloadDbLogs(id, query, parseSelectedIds(logIds)));
    }

    /**
     * 将 Service 生成的下载结果统一转换成浏览器附件响应。
     * UTF-8 文件名和 CSV 媒体类型在这里集中处理，避免每个下载接口重复设置响应头。
     */
    private ResponseEntity<byte[]> csvResponse(LedgerDownload download) {
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename(download.fileName(), StandardCharsets.UTF_8)
                .build()
                .toString())
            .body(download.content());
    }

    /**
     * 解析前端传入的逗号分隔 ID，并在进入 Service 前完成基础格式校验。
     * 空值表示不限定记录，由 Service 按资源下载全部数据。
     */
    private List<Long> parseSelectedIds(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        String[] parts = value.split(",");
        // 限制单次选择数量，避免异常大的导出请求占用过多数据库和内存资源。
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
                // 前端重复勾选同一条记录时只导出一次。
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
