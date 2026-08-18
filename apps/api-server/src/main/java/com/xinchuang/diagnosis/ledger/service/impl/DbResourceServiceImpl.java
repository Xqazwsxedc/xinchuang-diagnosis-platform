package com.xinchuang.diagnosis.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.ledger.dto.DbResourceSaveRequest;
import com.xinchuang.diagnosis.ledger.dto.LedgerPageQuery;
import com.xinchuang.diagnosis.ledger.entity.BusinessSystem;
import com.xinchuang.diagnosis.ledger.entity.BusinessSystemDb;
import com.xinchuang.diagnosis.ledger.entity.DbResource;
import com.xinchuang.diagnosis.ledger.entity.DbMonitoringConfig;
import com.xinchuang.diagnosis.ledger.mapper.BusinessSystemDbMapper;
import com.xinchuang.diagnosis.ledger.mapper.BusinessSystemMapper;
import com.xinchuang.diagnosis.ledger.mapper.DbResourceMapper;
import com.xinchuang.diagnosis.ledger.mapper.DbMonitoringConfigMapper;
import com.xinchuang.diagnosis.ledger.service.DbResourceService;
import com.xinchuang.diagnosis.ledger.vo.DbResourceVO;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import com.xinchuang.diagnosis.system.service.NotificationService;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DbResourceServiceImpl implements DbResourceService {
    private final DbResourceMapper dbResourceMapper;
    private final DbMonitoringConfigMapper dbMonitoringConfigMapper;
    private final BusinessSystemMapper businessSystemMapper;
    private final BusinessSystemDbMapper businessSystemDbMapper;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public DbResourceServiceImpl(DbResourceMapper dbResourceMapper,
                                 DbMonitoringConfigMapper dbMonitoringConfigMapper,
                                 BusinessSystemMapper businessSystemMapper,
                                 BusinessSystemDbMapper businessSystemDbMapper,
                                 AuditLogService auditLogService,
                                 NotificationService notificationService) {
        this.dbResourceMapper = dbResourceMapper;
        this.dbMonitoringConfigMapper = dbMonitoringConfigMapper;
        this.businessSystemMapper = businessSystemMapper;
        this.businessSystemDbMapper = businessSystemDbMapper;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Override
    public PageResponse<DbResourceVO> page(LedgerPageQuery query) {
        LambdaQueryWrapper<DbResource> wrapper = buildQuery(query);
        wrapper.orderByDesc(DbResource::getUpdatedAt);
        Page<DbResource> page = dbResourceMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        List<DbResource> resources = page.getRecords();
        Map<Long, DbMonitoringConfig> monitoringByResource = monitoringByResource(resources);
        List<DbResourceVO> records = resources.stream()
            .map(resource -> toVO(resource, monitoringByResource.get(resource.getId())))
            .toList();
        return PageResponse.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public DbResourceVO detail(Long id) {
        DbResource entity = dbResourceMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("分布式数据库不存在");
        }
        return toVO(entity, dbMonitoringConfigMapper.selectOne(
            new LambdaQueryWrapper<DbMonitoringConfig>().eq(DbMonitoringConfig::getResourceId, id)
        ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DbResourceVO create(DbResourceSaveRequest request) {
        DbResource entity = new DbResource();
        fill(entity, request);
        dbResourceMapper.insert(entity);
        auditLogService.recordSuccess("数据台账", "CREATE", "DB_RESOURCE", entity.getId(), "新增分布式数据库：" + entity.getName());
        notifyIfAbnormal(entity);
        return detail(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DbResourceVO update(Long id, DbResourceSaveRequest request) {
        DbResource entity = getById(id);
        fill(entity, request);
        dbResourceMapper.updateById(entity);
        auditLogService.recordSuccess("数据台账", "UPDATE", "DB_RESOURCE", id, "编辑分布式数据库：" + entity.getName());
        notifyIfAbnormal(entity);
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DbResource entity = getById(id);
        Long count = businessSystemMapper.selectCount(
            new LambdaQueryWrapper<BusinessSystem>().eq(BusinessSystem::getDatabaseId, id)
        );
        Long relationCount = businessSystemDbMapper.selectCount(
            new LambdaQueryWrapper<BusinessSystemDb>().eq(BusinessSystemDb::getDbResourceId, id)
        );
        if ((count != null && count > 0) || (relationCount != null && relationCount > 0)) {
            throw new BusinessException("该分布式数据库已被业务系统关联，不能删除");
        }
        dbResourceMapper.deleteById(id);
        auditLogService.recordSuccess("数据台账", "DELETE", "DB_RESOURCE", id, "删除分布式数据库：" + entity.getName());
    }

    private LambdaQueryWrapper<DbResource> buildQuery(LedgerPageQuery query) {
        LambdaQueryWrapper<DbResource> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(DbResource::getName, query.getKeyword())
                .or()
                .like(DbResource::getOwnerName, query.getKeyword())
                .or()
                .like(DbResource::getPublicIp, query.getKeyword())
                .or()
                .like(DbResource::getPrivateIp, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(DbResource::getName, query.getName());
        }
        if (StringUtils.hasText(query.getOwnerName())) {
            wrapper.like(DbResource::getOwnerName, query.getOwnerName());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(DbResource::getStatus, query.getStatus());
        }
        return wrapper;
    }

    private DbResource getById(Long id) {
        DbResource entity = dbResourceMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("分布式数据库不存在");
        }
        return entity;
    }

    private void fill(DbResource entity, DbResourceSaveRequest request) {
        entity.setName(request.getName());
        entity.setPublicIp(request.getPublicIp());
        entity.setPrivateIp(request.getPrivateIp());
        entity.setOwnerName(request.getOwnerName());
        entity.setStatus(request.getStatus());
    }

    private void notifyIfAbnormal(DbResource entity) {
        if (!List.of("WARNING", "DANGER", "OFFLINE").contains(entity.getStatus())) {
            return;
        }
        notificationService.notifyAdmins(
            "分布式数据库状态异常：" + entity.getName(),
            "分布式数据库「" + entity.getName() + "」当前状态为 " + entity.getStatus() + "，请及时关注。",
            "ALERT",
            "DANGER".equals(entity.getStatus()) || "OFFLINE".equals(entity.getStatus()) ? "HIGH" : "NORMAL",
            "DB_RESOURCE",
            entity.getId()
        );
    }

    private Map<Long, DbMonitoringConfig> monitoringByResource(List<DbResource> resources) {
        if (resources.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = resources.stream().map(DbResource::getId).toList();
        return dbMonitoringConfigMapper.selectList(
            new LambdaQueryWrapper<DbMonitoringConfig>().in(DbMonitoringConfig::getResourceId, ids)
        ).stream().collect(Collectors.toMap(DbMonitoringConfig::getResourceId, Function.identity(), (first, ignored) -> first));
    }

    private DbResourceVO toVO(DbResource entity, DbMonitoringConfig monitoringConfig) {
        DbResourceVO vo = new DbResourceVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setPublicIp(entity.getPublicIp());
        vo.setPrivateIp(entity.getPrivateIp());
        vo.setOwnerName(entity.getOwnerName());
        vo.setStatus(entity.getStatus());
        vo.setMonitoringStatus(monitoringConfig != null && "HEALTHY".equals(monitoringConfig.getStatus())
            ? "CONNECTED" : "NOT_CONNECTED");
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
