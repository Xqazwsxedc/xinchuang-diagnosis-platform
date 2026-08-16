package com.xinchuang.diagnosis.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.ledger.dto.BusinessSystemSaveRequest;
import com.xinchuang.diagnosis.ledger.dto.LedgerPageQuery;
import com.xinchuang.diagnosis.ledger.entity.BusinessSystem;
import com.xinchuang.diagnosis.ledger.entity.BusinessSystemDb;
import com.xinchuang.diagnosis.ledger.entity.DbResource;
import com.xinchuang.diagnosis.ledger.mapper.BusinessSystemDbMapper;
import com.xinchuang.diagnosis.ledger.mapper.BusinessSystemMapper;
import com.xinchuang.diagnosis.ledger.mapper.DbResourceMapper;
import com.xinchuang.diagnosis.ledger.service.BusinessSystemService;
import com.xinchuang.diagnosis.ledger.vo.BusinessSystemVO;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import com.xinchuang.diagnosis.system.service.NotificationService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BusinessSystemServiceImpl implements BusinessSystemService {
    private final BusinessSystemMapper businessSystemMapper;
    private final BusinessSystemDbMapper businessSystemDbMapper;
    private final DbResourceMapper dbResourceMapper;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public BusinessSystemServiceImpl(BusinessSystemMapper businessSystemMapper,
                                     BusinessSystemDbMapper businessSystemDbMapper,
                                     DbResourceMapper dbResourceMapper,
                                     AuditLogService auditLogService,
                                     NotificationService notificationService) {
        this.businessSystemMapper = businessSystemMapper;
        this.businessSystemDbMapper = businessSystemDbMapper;
        this.dbResourceMapper = dbResourceMapper;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Override
    public PageResponse<BusinessSystemVO> page(LedgerPageQuery query) {
        LambdaQueryWrapper<BusinessSystem> wrapper = buildQuery(query);
        wrapper.orderByDesc(BusinessSystem::getUpdatedAt);
        Page<BusinessSystem> page = businessSystemMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        Map<Long, List<Long>> relationMap = loadDatabaseIdsByBusinessSystemIds(page.getRecords());
        Map<Long, DbResource> dbResourceMap = loadDbResourceMap(page.getRecords(), relationMap);
        List<BusinessSystemVO> records = page.getRecords().stream()
            .map(entity -> toVO(entity, relationMap.getOrDefault(entity.getId(), List.of()), dbResourceMap))
            .toList();
        return PageResponse.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public BusinessSystemVO detail(Long id) {
        BusinessSystem entity = businessSystemMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("业务系统不存在");
        }
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BusinessSystemVO create(BusinessSystemSaveRequest request) {
        List<Long> databaseIds = normalizeDatabaseIds(request);
        ensureDatabasesExist(databaseIds);
        BusinessSystem entity = new BusinessSystem();
        fill(entity, request, databaseIds);
        businessSystemMapper.insert(entity);
        saveDatabaseRelations(entity.getId(), databaseIds);
        auditLogService.recordSuccess("数据台账", "CREATE", "BUSINESS_SYSTEM", entity.getId(), "新增业务系统：" + entity.getName() + "，关联数据库：" + databaseIds.size() + " 个");
        notifyIfAbnormal(entity);
        return detail(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BusinessSystemVO update(Long id, BusinessSystemSaveRequest request) {
        BusinessSystem entity = getById(id);
        List<Long> databaseIds = normalizeDatabaseIds(request);
        ensureDatabasesExist(databaseIds);
        fill(entity, request, databaseIds);
        businessSystemMapper.updateById(entity);
        saveDatabaseRelations(id, databaseIds);
        auditLogService.recordSuccess("数据台账", "UPDATE", "BUSINESS_SYSTEM", id, "编辑业务系统：" + entity.getName() + "，关联数据库：" + databaseIds.size() + " 个");
        notifyIfAbnormal(entity);
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        BusinessSystem entity = getById(id);
        businessSystemDbMapper.deleteByBusinessSystemIdPhysically(id);
        businessSystemMapper.deleteById(id);
        auditLogService.recordSuccess("数据台账", "DELETE", "BUSINESS_SYSTEM", id, "删除业务系统：" + entity.getName());
    }

    private LambdaQueryWrapper<BusinessSystem> buildQuery(LedgerPageQuery query) {
        LambdaQueryWrapper<BusinessSystem> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(BusinessSystem::getName, query.getKeyword())
                .or()
                .like(BusinessSystem::getOwnerName, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(BusinessSystem::getName, query.getName());
        }
        if (StringUtils.hasText(query.getOwnerName())) {
            wrapper.like(BusinessSystem::getOwnerName, query.getOwnerName());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(BusinessSystem::getStatus, query.getStatus());
        }
        return wrapper;
    }

    private BusinessSystem getById(Long id) {
        BusinessSystem entity = businessSystemMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("业务系统不存在");
        }
        return entity;
    }

    private void fill(BusinessSystem entity, BusinessSystemSaveRequest request, List<Long> databaseIds) {
        entity.setName(request.getName());
        entity.setDatabaseId(databaseIds.get(0));
        entity.setOwnerName(request.getOwnerName());
        entity.setStatus(request.getStatus());
        entity.setAccessUrl(request.getAccessUrl());
        entity.setRemark(request.getRemark());
    }

    private List<Long> normalizeDatabaseIds(BusinessSystemSaveRequest request) {
        Set<Long> ids = new LinkedHashSet<>();
        if (request.getDatabaseIds() != null) {
            request.getDatabaseIds().stream()
                .filter(id -> id != null && id > 0)
                .forEach(ids::add);
        }
        if (ids.isEmpty() && request.getDatabaseId() != null && request.getDatabaseId() > 0) {
            ids.add(request.getDatabaseId());
        }
        if (ids.isEmpty()) {
            throw new BusinessException("请选择至少一个关联分布式数据库");
        }
        return new ArrayList<>(ids);
    }

    private void ensureDatabasesExist(List<Long> databaseIds) {
        List<DbResource> dbResources = dbResourceMapper.selectBatchIds(databaseIds);
        if (dbResources.size() != databaseIds.size()) {
            throw new BusinessException("关联的分布式数据库不存在");
        }
    }

    private void saveDatabaseRelations(Long businessSystemId, List<Long> databaseIds) {
        businessSystemDbMapper.deleteByBusinessSystemIdPhysically(businessSystemId);
        for (int i = 0; i < databaseIds.size(); i++) {
            BusinessSystemDb relation = new BusinessSystemDb();
            relation.setBusinessSystemId(businessSystemId);
            relation.setDbResourceId(databaseIds.get(i));
            relation.setRelationType(i == 0 ? "PRIMARY" : "STANDBY");
            businessSystemDbMapper.insert(relation);
        }
    }

    private void notifyIfAbnormal(BusinessSystem entity) {
        if (!List.of("WARNING", "DANGER", "OFFLINE").contains(entity.getStatus())) {
            return;
        }
        notificationService.notifyAdmins(
            "业务系统状态异常：" + entity.getName(),
            "业务系统「" + entity.getName() + "」当前状态为 " + entity.getStatus() + "，请及时关注。",
            "ALERT",
            "DANGER".equals(entity.getStatus()) || "OFFLINE".equals(entity.getStatus()) ? "HIGH" : "NORMAL",
            "BUSINESS_SYSTEM",
            entity.getId()
        );
    }

    private BusinessSystemVO toVO(BusinessSystem entity) {
        List<Long> databaseIds = businessSystemDbMapper.selectList(
                new LambdaQueryWrapper<BusinessSystemDb>()
                    .eq(BusinessSystemDb::getBusinessSystemId, entity.getId())
                    .orderByAsc(BusinessSystemDb::getId)
            ).stream()
            .map(BusinessSystemDb::getDbResourceId)
            .distinct()
            .toList();
        Map<Long, DbResource> dbResourceMap = databaseIds.isEmpty() && entity.getDatabaseId() == null
            ? Map.of()
            : dbResourceMapper.selectBatchIds(databaseIds.isEmpty() ? List.of(entity.getDatabaseId()) : databaseIds).stream()
                .collect(Collectors.toMap(DbResource::getId, Function.identity(), (left, right) -> left));
        return toVO(entity, databaseIds, dbResourceMap);
    }

    private BusinessSystemVO toVO(BusinessSystem entity, List<Long> relationDatabaseIds, Map<Long, DbResource> dbResourceMap) {
        BusinessSystemVO vo = new BusinessSystemVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDatabaseId(entity.getDatabaseId());
        vo.setOwnerName(entity.getOwnerName());
        vo.setStatus(entity.getStatus());
        vo.setAccessUrl(entity.getAccessUrl());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());

        List<Long> databaseIds = relationDatabaseIds.stream().distinct().toList();
        if (databaseIds.isEmpty() && entity.getDatabaseId() != null) {
            databaseIds = List.of(entity.getDatabaseId());
        }

        if (!databaseIds.isEmpty()) {
            List<String> databaseNames = databaseIds.stream()
                .map(dbResourceMap::get)
                .map(dbResource -> dbResource == null ? null : dbResource.getName())
                .filter(StringUtils::hasText)
                .toList();
            vo.setDatabaseIds(databaseIds);
            vo.setDatabaseNames(databaseNames);
            vo.setDatabaseId(databaseIds.get(0));
            DbResource firstDbResource = dbResourceMap.get(databaseIds.get(0));
            vo.setDatabaseName(firstDbResource == null ? null : firstDbResource.getName());
        } else {
            vo.setDatabaseIds(List.of());
            vo.setDatabaseNames(List.of());
        }
        return vo;
    }

    private Map<Long, List<Long>> loadDatabaseIdsByBusinessSystemIds(List<BusinessSystem> records) {
        List<Long> businessSystemIds = records.stream()
            .map(BusinessSystem::getId)
            .toList();
        if (businessSystemIds.isEmpty()) {
            return Map.of();
        }
        return businessSystemDbMapper.selectList(
                new LambdaQueryWrapper<BusinessSystemDb>()
                    .in(BusinessSystemDb::getBusinessSystemId, businessSystemIds)
                    .orderByAsc(BusinessSystemDb::getId)
            ).stream()
            .collect(Collectors.groupingBy(
                BusinessSystemDb::getBusinessSystemId,
                Collectors.mapping(BusinessSystemDb::getDbResourceId, Collectors.toList())
            ));
    }

    private Map<Long, DbResource> loadDbResourceMap(List<BusinessSystem> records, Map<Long, List<Long>> relationMap) {
        Set<Long> databaseIds = new LinkedHashSet<>();
        relationMap.values().forEach(databaseIds::addAll);
        records.stream()
            .map(BusinessSystem::getDatabaseId)
            .filter(id -> id != null && id > 0)
            .forEach(databaseIds::add);
        if (databaseIds.isEmpty()) {
            return Map.of();
        }
        return dbResourceMapper.selectBatchIds(databaseIds).stream()
            .collect(Collectors.toMap(DbResource::getId, Function.identity(), (left, right) -> left));
    }
}
