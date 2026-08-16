package com.xinchuang.diagnosis.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.ledger.dto.DbInstanceLogPageQuery;
import com.xinchuang.diagnosis.ledger.dto.DbMetricRangeQuery;
import com.xinchuang.diagnosis.ledger.entity.BusinessDataRecord;
import com.xinchuang.diagnosis.ledger.entity.BusinessSystem;
import com.xinchuang.diagnosis.ledger.entity.DbInstanceLog;
import com.xinchuang.diagnosis.ledger.entity.DbResource;
import com.xinchuang.diagnosis.ledger.entity.ReportExportLog;
import com.xinchuang.diagnosis.ledger.mapper.BusinessDataRecordMapper;
import com.xinchuang.diagnosis.ledger.mapper.BusinessSystemMapper;
import com.xinchuang.diagnosis.ledger.mapper.DbInstanceLogMapper;
import com.xinchuang.diagnosis.ledger.mapper.DbResourceMapper;
import com.xinchuang.diagnosis.ledger.mapper.ReportExportLogMapper;
import com.xinchuang.diagnosis.ledger.service.LedgerDetailService;
import com.xinchuang.diagnosis.ledger.vo.BusinessDataRecordVO;
import com.xinchuang.diagnosis.ledger.vo.DbInstanceLogVO;
import com.xinchuang.diagnosis.ledger.vo.DbMetricsOverviewVO;
import com.xinchuang.diagnosis.ledger.vo.LedgerDownload;
import com.xinchuang.diagnosis.monitoring.prometheus.DbPrometheusMetricsService;
import com.xinchuang.diagnosis.monitoring.prometheus.PrometheusException;
import com.xinchuang.diagnosis.security.LoginUser;
import com.xinchuang.diagnosis.security.UserContext;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class LedgerDetailServiceImpl implements LedgerDetailService {
    private final BusinessSystemMapper businessSystemMapper;
    private final DbResourceMapper dbResourceMapper;
    private final BusinessDataRecordMapper businessDataRecordMapper;
    private final DbInstanceLogMapper dbInstanceLogMapper;
    private final ReportExportLogMapper reportExportLogMapper;
    private final AuditLogService auditLogService;
    private final DbPrometheusMetricsService prometheusMetricsService;

    public LedgerDetailServiceImpl(BusinessSystemMapper businessSystemMapper,
                                   DbResourceMapper dbResourceMapper,
                                   BusinessDataRecordMapper businessDataRecordMapper,
                                   DbInstanceLogMapper dbInstanceLogMapper,
                                   ReportExportLogMapper reportExportLogMapper,
                                   AuditLogService auditLogService,
                                   DbPrometheusMetricsService prometheusMetricsService) {
        this.businessSystemMapper = businessSystemMapper;
        this.dbResourceMapper = dbResourceMapper;
        this.businessDataRecordMapper = businessDataRecordMapper;
        this.dbInstanceLogMapper = dbInstanceLogMapper;
        this.reportExportLogMapper = reportExportLogMapper;
        this.auditLogService = auditLogService;
        this.prometheusMetricsService = prometheusMetricsService;
    }

    @Override
    public List<BusinessDataRecordVO> businessDataRecords(Long businessSystemId) {
        requireBusinessSystem(businessSystemId);
        return businessDataRecordMapper.selectList(new LambdaQueryWrapper<BusinessDataRecord>()
                .eq(BusinessDataRecord::getSystemId, businessSystemId)
                .orderByDesc(BusinessDataRecord::getCollectedAt))
            .stream()
            .map(this::toBusinessDataRecordVO)
            .toList();
    }

    @Override
    public DbMetricsOverviewVO dbMetrics(Long dbResourceId, DbMetricRangeQuery query) {
        requireDbResource(dbResourceId);
        return loadPrometheusMetrics(query.getRange());
    }

    @Override
    public PageResponse<DbInstanceLogVO> dbLogs(Long dbResourceId, DbInstanceLogPageQuery query) {
        requireDbResource(dbResourceId);
        Page<DbInstanceLog> page = dbInstanceLogMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()),
            new LambdaQueryWrapper<DbInstanceLog>()
                .eq(DbInstanceLog::getDbInstanceId, dbResourceId)
                .ge(DbInstanceLog::getLoggedAt, rangeStart(query.getRange()))
                .eq(StringUtils.hasText(query.getLogLevel()), DbInstanceLog::getLogLevel, query.getLogLevel())
                .orderByDesc(DbInstanceLog::getLoggedAt));
        return PageResponse.of(page.getRecords().stream().map(this::toDbInstanceLogVO).toList(),
            page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LedgerDownload downloadBusinessData(Long businessSystemId, List<Long> recordIds) {
        BusinessSystem businessSystem = requireBusinessSystem(businessSystemId);
        List<BusinessDataRecord> records = recordIds == null || recordIds.isEmpty()
            ? findBusinessDataRecords(businessSystemId)
            : findBusinessDataRecords(businessSystemId, recordIds);
        if (records.isEmpty()) {
            throw new BusinessException("暂无可下载的业务数据");
        }
        String fileName = records.size() == 1
            ? records.get(0).getFileName()
            : "business-data-" + businessSystemId + ".csv";
        StringBuilder csv = new StringBuilder("收集日期,收集时间,总大小(MB),收集状态,文件名,收集摘要\n");
        for (BusinessDataRecord record : records) {
            csv.append(record.getCollectedAt().toLocalDate()).append(',')
                .append(record.getCollectedAt().toLocalTime().withNano(0)).append(',')
                .append(String.format("%.1f", record.getTotalSizeBytes() / 1024D / 1024D)).append(',')
                .append(record.getCollectStatus()).append(',')
                .append(csvValue(record.getFileName())).append(',')
                .append(csvValue(record.getRecordSummary())).append('\n');
        }
        recordExport("BUSINESS_DATA", businessSystemId, fileName,
            "下载业务系统数据：" + businessSystem.getName());
        return csvDownload(fileName, csv);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LedgerDownload downloadDbMetrics(Long dbResourceId, DbMetricRangeQuery query) {
        DbResource resource = requireDbResource(dbResourceId);
        DbMetricsOverviewVO overview = loadPrometheusMetrics(query.getRange());
        if (overview.getPoints().isEmpty()) {
            throw new BusinessException("当前时间范围暂无指标数据");
        }
        String fileName = "db-metrics-" + dbResourceId + "-" + query.getRange() + ".csv";
        StringBuilder csv = new StringBuilder("采样时间,CPU利用率(%),内存使用占比(%),磁盘IOPS,当前连接数,缓存命中率(%)\n");
        for (var point : overview.getPoints()) {
            csv.append(point.getSampledAt()).append(',')
                .append(value(point.getCpuUsage())).append(',')
                .append(value(point.getMemoryUsage())).append(',')
                .append(value(point.getDiskIops())).append(',')
                .append(value(point.getCurrentConnections())).append(',')
                .append(value(point.getCacheHitRate())).append('\n');
        }
        recordExport("DB_METRICS", dbResourceId, fileName, "下载数据库指标：" + resource.getName());
        return csvDownload(fileName, csv);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LedgerDownload downloadDbLogs(Long dbResourceId, DbInstanceLogPageQuery query, List<Long> logIds) {
        DbResource resource = requireDbResource(dbResourceId);
        LambdaQueryWrapper<DbInstanceLog> wrapper = new LambdaQueryWrapper<DbInstanceLog>()
            .eq(DbInstanceLog::getDbInstanceId, dbResourceId)
            .ge(DbInstanceLog::getLoggedAt, rangeStart(query.getRange()))
            .eq(StringUtils.hasText(query.getLogLevel()), DbInstanceLog::getLogLevel, query.getLogLevel())
            .orderByDesc(DbInstanceLog::getLoggedAt);
        if (logIds != null && !logIds.isEmpty()) {
            wrapper.in(DbInstanceLog::getId, logIds);
        }
        List<DbInstanceLog> logs = dbInstanceLogMapper.selectList(wrapper);
        if (logs.isEmpty()) {
            throw new BusinessException("当前时间范围暂无服务器日志");
        }
        if (logIds != null && !logIds.isEmpty() && logs.size() != logIds.size()) {
            throw new BusinessException("存在无效或不可下载的服务器日志");
        }
        String fileName = "db-logs-" + dbResourceId + "-" + query.getRange() + ".csv";
        StringBuilder csv = new StringBuilder("日志时间,日志级别,日志内容\n");
        for (DbInstanceLog log : logs) {
            csv.append(log.getLoggedAt()).append(',')
                .append(log.getLogLevel()).append(',')
                .append(csvValue(log.getLogContent())).append('\n');
        }
        recordExport("DB_LOG", dbResourceId, fileName, "下载数据库日志：" + resource.getName());
        return csvDownload(fileName, csv);
    }

    private List<BusinessDataRecord> findBusinessDataRecords(Long businessSystemId) {
        return businessDataRecordMapper.selectList(new LambdaQueryWrapper<BusinessDataRecord>()
            .eq(BusinessDataRecord::getSystemId, businessSystemId)
            .orderByDesc(BusinessDataRecord::getCollectedAt));
    }

    private List<BusinessDataRecord> findBusinessDataRecords(Long businessSystemId, List<Long> recordIds) {
        List<BusinessDataRecord> records = businessDataRecordMapper.selectList(new LambdaQueryWrapper<BusinessDataRecord>()
            .eq(BusinessDataRecord::getSystemId, businessSystemId)
            .in(BusinessDataRecord::getId, recordIds)
            .orderByDesc(BusinessDataRecord::getCollectedAt));
        if (records.size() != recordIds.size()) {
            throw new BusinessException("存在无效或不可下载的业务数据记录");
        }
        return records;
    }

    private DbMetricsOverviewVO loadPrometheusMetrics(String range) {
        try {
            return prometheusMetricsService.load(range);
        } catch (PrometheusException exception) {
            throw new BusinessException(503, exception.getMessage());
        }
    }

    private BusinessSystem requireBusinessSystem(Long id) {
        BusinessSystem businessSystem = businessSystemMapper.selectById(id);
        if (businessSystem == null) {
            throw new BusinessException("业务系统不存在");
        }
        return businessSystem;
    }

    private DbResource requireDbResource(Long id) {
        DbResource resource = dbResourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException("分布式数据库不存在");
        }
        return resource;
    }

    private LocalDateTime rangeStart(String range) {
        return switch (range) {
            case "1H" -> LocalDateTime.now().minusHours(1);
            case "3H" -> LocalDateTime.now().minusHours(3);
            case "6H" -> LocalDateTime.now().minusHours(6);
            case "12H" -> LocalDateTime.now().minusHours(12);
            case "3D" -> LocalDateTime.now().minusDays(3);
            case "7D" -> LocalDateTime.now().minusDays(7);
            case "14D" -> LocalDateTime.now().minusDays(14);
            default -> LocalDateTime.now().minusDays(1);
        };
    }

    private void recordExport(String reportType, Long reportId, String fileName, String content) {
        LoginUser user = UserContext.get();
        if (user == null || user.getUserId() == null) {
            throw new BusinessException(401, "登录已失效，请重新登录");
        }
        ReportExportLog exportLog = new ReportExportLog();
        exportLog.setReportType(reportType);
        exportLog.setReportId(reportId);
        exportLog.setFileName(fileName);
        exportLog.setExportStatus("SUCCESS");
        exportLog.setExportedBy(user.getUserId());
        exportLog.setExportedAt(LocalDateTime.now());
        reportExportLogMapper.insert(exportLog);
        auditLogService.recordSuccess("数据台账", "EXPORT", reportType, reportId, content);
    }

    private LedgerDownload csvDownload(String fileName, StringBuilder csv) {
        return new LedgerDownload(fileName, ("\ufeff" + csv).getBytes(StandardCharsets.UTF_8));
    }

    private String csvValue(String value) {
        if (value == null) {
            return "";
        }
        return '"' + value.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + '"';
    }

    private String value(Object value) { return value == null ? "" : value.toString(); }

    private BusinessDataRecordVO toBusinessDataRecordVO(BusinessDataRecord record) {
        BusinessDataRecordVO vo = new BusinessDataRecordVO();
        vo.setId(record.getId());
        vo.setCollectedAt(record.getCollectedAt());
        vo.setTotalSizeBytes(record.getTotalSizeBytes());
        vo.setCollectStatus(record.getCollectStatus());
        vo.setFileName(record.getFileName());
        vo.setRecordSummary(record.getRecordSummary());
        return vo;
    }

    private DbInstanceLogVO toDbInstanceLogVO(DbInstanceLog log) {
        DbInstanceLogVO vo = new DbInstanceLogVO();
        vo.setId(log.getId());
        vo.setLoggedAt(log.getLoggedAt());
        vo.setLogLevel(log.getLogLevel());
        vo.setLogContent(log.getLogContent());
        return vo;
    }
}
