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
import com.xinchuang.diagnosis.ledger.entity.DbMonitoringConfig;
import com.xinchuang.diagnosis.ledger.entity.ReportExportLog;
import com.xinchuang.diagnosis.ledger.mapper.BusinessDataRecordMapper;
import com.xinchuang.diagnosis.ledger.mapper.BusinessSystemMapper;
import com.xinchuang.diagnosis.ledger.mapper.DbInstanceLogMapper;
import com.xinchuang.diagnosis.ledger.mapper.DbResourceMapper;
import com.xinchuang.diagnosis.ledger.mapper.DbMonitoringConfigMapper;
import com.xinchuang.diagnosis.ledger.mapper.ReportExportLogMapper;
import com.xinchuang.diagnosis.ledger.service.LedgerDetailService;
import com.xinchuang.diagnosis.ledger.vo.BusinessDataRecordVO;
import com.xinchuang.diagnosis.ledger.vo.DbInstanceLogVO;
import com.xinchuang.diagnosis.ledger.vo.DbMetricsOverviewVO;
import com.xinchuang.diagnosis.ledger.vo.LedgerDownload;
import com.xinchuang.diagnosis.monitoring.prometheus.DbPrometheusMetricsService;
import com.xinchuang.diagnosis.monitoring.prometheus.PrometheusException;
import com.xinchuang.diagnosis.monitoring.prometheus.PrometheusRange;
import com.xinchuang.diagnosis.monitoring.prometheus.PrometheusRangeResolver;
import com.xinchuang.diagnosis.security.LoginUser;
import com.xinchuang.diagnosis.security.UserContext;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 数据台账详情业务编排层。
 *
 * <p>这里串联资源存在性、监控配置健康状态、数据库查询、Prometheus 查询、
 * CSV 生成和导出审计；Controller 不直接接触这些细节。</p>
 */
@Service
public class LedgerDetailServiceImpl implements LedgerDetailService {
    private final BusinessSystemMapper businessSystemMapper;
    private final DbResourceMapper dbResourceMapper;
    private final DbMonitoringConfigMapper monitoringConfigMapper;
    private final ObjectMapper objectMapper;
    private final BusinessDataRecordMapper businessDataRecordMapper;
    private final DbInstanceLogMapper dbInstanceLogMapper;
    private final ReportExportLogMapper reportExportLogMapper;
    private final AuditLogService auditLogService;
    private final DbPrometheusMetricsService prometheusMetricsService;
    private final PrometheusRangeResolver prometheusRangeResolver;

    public LedgerDetailServiceImpl(BusinessSystemMapper businessSystemMapper,
                                   DbResourceMapper dbResourceMapper,
                                   DbMonitoringConfigMapper monitoringConfigMapper,
                                   ObjectMapper objectMapper,
                                   BusinessDataRecordMapper businessDataRecordMapper,
                                   DbInstanceLogMapper dbInstanceLogMapper,
                                   ReportExportLogMapper reportExportLogMapper,
                                   AuditLogService auditLogService,
                                   DbPrometheusMetricsService prometheusMetricsService,
                                   PrometheusRangeResolver prometheusRangeResolver) {
        this.businessSystemMapper = businessSystemMapper;
        this.dbResourceMapper = dbResourceMapper;
        this.monitoringConfigMapper = monitoringConfigMapper;
        this.objectMapper = objectMapper;
        this.businessDataRecordMapper = businessDataRecordMapper;
        this.dbInstanceLogMapper = dbInstanceLogMapper;
        this.reportExportLogMapper = reportExportLogMapper;
        this.auditLogService = auditLogService;
        this.prometheusMetricsService = prometheusMetricsService;
        this.prometheusRangeResolver = prometheusRangeResolver;
    }

    @Override
    public List<BusinessDataRecordVO> businessDataRecords(Long businessSystemId) {
        // 先校验父级业务系统，避免把不存在资源误显示成空列表。
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
        DbResource resource = requireDbResource(dbResourceId);
        DbMonitoringConfig config = requireHealthyMonitoring(resource);
        // 详情页默认一次加载五类指标，资源级 URL 和 selector 由监控配置提供。
        return loadPrometheusMetrics(config, query.getRange(), Set.of("cpuUsage", "memoryUsage", "diskIops", "currentConnections", "cacheHitRate"));
    }

    @Override
    public PageResponse<DbInstanceLogVO> dbLogs(Long dbResourceId, DbInstanceLogPageQuery query) {
        requireDbResource(dbResourceId);
        // 业务日志仍来自平台自己的 db_instance_log 表，不经过 Prometheus。
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
        // 空 ID 列表表示下载全部；指定 ID 时由查询条件保证记录属于当前业务系统。
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
        DbMonitoringConfig config = requireHealthyMonitoring(resource);
        Set<String> metricCodes = parseMetricCodes(query.getMetrics());
        // 自定义日期优先于页面快捷范围，否则按 range 计算最近一段时间。
        PrometheusRange selectedRange = resolveDownloadRange(query);
        if (selectedRange == null) {
            selectedRange = prometheusRangeResolver.resolve(query.getRange(), Instant.now());
        }
        DbMetricsOverviewVO overview = loadPrometheusMetrics(config, selectedRange, metricCodes);
        if (overview.getPoints().isEmpty()) {
            throw new BusinessException("当前时间范围暂无指标数据");
        }
        String fileName = "db-metrics-" + dbResourceId + "-" + (query.getStartDate() != null ? query.getStartDate() : query.getRange()) + ".csv";
        StringBuilder csv = new StringBuilder("采样时间");
        if (metricCodes.contains("cpuUsage")) csv.append(",CPU利用率(%)");
        if (metricCodes.contains("memoryUsage")) csv.append(",内存使用占比(%)");
        if (metricCodes.contains("diskIops")) csv.append(",磁盘IOPS");
        if (metricCodes.contains("currentConnections")) csv.append(",当前连接数");
        if (metricCodes.contains("cacheHitRate")) csv.append(",缓存命中率(%)");
        csv.append('\n');
        for (var point : overview.getPoints()) {
            csv.append(point.getSampledAt());
            if (metricCodes.contains("cpuUsage")) csv.append(',').append(value(point.getCpuUsage()));
            if (metricCodes.contains("memoryUsage")) csv.append(',').append(value(point.getMemoryUsage()));
            if (metricCodes.contains("diskIops")) csv.append(',').append(value(point.getDiskIops()));
            if (metricCodes.contains("currentConnections")) csv.append(',').append(value(point.getCurrentConnections()));
            if (metricCodes.contains("cacheHitRate")) csv.append(',').append(value(point.getCacheHitRate()));
            csv.append('\n');
        }
        // 导出成功和审计记录属于同一事务，审计失败时整个导出操作回滚。
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
            // 先按资源、时间和级别过滤，再叠加选中 ID，防止跨资源下载日志。
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

    private DbMetricsOverviewVO loadPrometheusMetrics(PrometheusRange range, Set<String> metricCodes) {
        try {
            return prometheusMetricsService.load(range, metricCodes);
        } catch (PrometheusException exception) {
            throw new BusinessException(503, exception.getMessage());
        }
    }

    private DbMetricsOverviewVO loadPrometheusMetrics(DbMonitoringConfig config, String range, Set<String> metricCodes) {
        try {
            // 资源级配置允许不同数据库资源使用不同 Prometheus 地址和目标 selector。
            return prometheusMetricsService.load(prometheusRangeResolver.resolve(range, Instant.now()), metricCodes, config.getPrometheusBaseUrl(), config.getPrometheusAuthRef(),
                selector(config.getNodeTargetSelector()), selector(config.getDatabaseTargetSelector()));
        } catch (PrometheusException exception) {
            throw new BusinessException(503, exception.getMessage());
        }
    }

    private DbMetricsOverviewVO loadPrometheusMetrics(DbMonitoringConfig config, PrometheusRange range, Set<String> metricCodes) {
        try {
            return prometheusMetricsService.load(range, metricCodes, config.getPrometheusBaseUrl(), config.getPrometheusAuthRef(),
                selector(config.getNodeTargetSelector()), selector(config.getDatabaseTargetSelector()));
        } catch (PrometheusException exception) {
            throw new BusinessException(503, exception.getMessage());
        }
    }

    private Map<String, String> selector(String value) {
        try { return objectMapper.readValue(value, new TypeReference<Map<String, String>>() { }); }
        catch (Exception ex) { throw new BusinessException(503, "监控配置损坏，请重新保存"); }
    }

    private PrometheusRange resolveDownloadRange(DbMetricRangeQuery query) {
        if ((query.getStartDate() == null) != (query.getEndDate() == null)) {
            throw new BusinessException("开始日期和结束日期必须同时填写");
        }
        if (query.getStartDate() != null) {
            return prometheusRangeResolver.resolve(query.getStartDate(), query.getEndDate());
        }
        return null;
    }

    private Set<String> parseMetricCodes(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException("至少选择一项指标");
        }
        // 导出接口只接受白名单指标，避免把任意字符串带入查询或 CSV 表头。
        Set<String> allowed = Set.of("cpuUsage", "memoryUsage", "cacheHitRate");
        Set<String> selected = new LinkedHashSet<>(Arrays.stream(value.split(","))
            .map(String::trim).filter(StringUtils::hasText).toList());
        if (selected.isEmpty() || !allowed.containsAll(selected)) {
            throw new BusinessException("指标选择不合法");
        }
        return selected;
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

    private DbMonitoringConfig requireHealthyMonitoring(DbResource resource) {
        // 只有连接测试通过且资源状态正常时，才允许读取真实监控数据。
        DbMonitoringConfig config = monitoringConfigMapper.selectOne(new LambdaQueryWrapper<DbMonitoringConfig>()
            .eq(DbMonitoringConfig::getResourceId, resource.getId()));
        if (config == null || !"HEALTHY".equals(config.getStatus()) || !"NORMAL".equals(resource.getMonitorStatus())) {
            throw new BusinessException(409, "当前数据库监控尚未通过连接测试，暂无真实指标数据");
        }
        return config;
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
        // 导出操作必须关联当前登录人，同时写入导出日志和统一审计日志。
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
        // UTF-8 BOM 让 Excel 等客户端正确识别中文 CSV 表头。
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
