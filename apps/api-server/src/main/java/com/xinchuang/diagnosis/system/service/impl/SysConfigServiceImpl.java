package com.xinchuang.diagnosis.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.system.dto.ConfigPageQuery;
import com.xinchuang.diagnosis.system.dto.ConfigSaveRequest;
import com.xinchuang.diagnosis.system.dto.SimpleStatusRequest;
import com.xinchuang.diagnosis.system.entity.SysConfig;
import com.xinchuang.diagnosis.system.mapper.SysConfigMapper;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import com.xinchuang.diagnosis.system.service.SysConfigService;
import com.xinchuang.diagnosis.system.vo.ConfigVO;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SysConfigServiceImpl implements SysConfigService {
    private final SysConfigMapper sysConfigMapper;
    private final AuditLogService auditLogService;

    public SysConfigServiceImpl(SysConfigMapper sysConfigMapper, AuditLogService auditLogService) {
        this.sysConfigMapper = sysConfigMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    public PageResponse<ConfigVO> page(ConfigPageQuery query) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysConfig::getConfigKey, query.getKeyword())
                .or()
                .like(SysConfig::getConfigName, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getConfigKey())) {
            wrapper.like(SysConfig::getConfigKey, query.getConfigKey());
        }
        if (StringUtils.hasText(query.getConfigName())) {
            wrapper.like(SysConfig::getConfigName, query.getConfigName());
        }
        if (StringUtils.hasText(query.getConfigType())) {
            wrapper.eq(SysConfig::getConfigType, query.getConfigType());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(SysConfig::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysConfig::getUpdatedAt);
        Page<SysConfig> page = sysConfigMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(page.getRecords().stream().map(this::toVO).toList(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public ConfigVO detail(Long id) {
        return toVO(getById(id));
    }

    @Override
    public ConfigVO getByKey(String configKey) {
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, configKey));
        if (config == null) {
            throw new BusinessException("系统配置不存在");
        }
        return toVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigVO create(ConfigSaveRequest request) {
        ensureConfigKeyUnique(request.getConfigKey(), null);
        SysConfig config = new SysConfig();
        fill(config, request);
        sysConfigMapper.insert(config);
        auditLogService.recordSuccess("系统配置", "CREATE", "SYS_CONFIG", config.getId(), "新增系统配置：" + config.getConfigKey());
        return detail(config.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigVO update(Long id, ConfigSaveRequest request) {
        SysConfig config = getById(id);
        ensureConfigKeyUnique(request.getConfigKey(), id);
        fill(config, request);
        sysConfigMapper.updateById(config);
        auditLogService.recordSuccess("系统配置", "UPDATE", "SYS_CONFIG", id, "编辑系统配置：" + config.getConfigKey());
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, SimpleStatusRequest request) {
        SysConfig config = getById(id);
        config.setStatus(request.getStatus());
        sysConfigMapper.updateById(config);
        auditLogService.recordSuccess("系统配置", "STATUS", "SYS_CONFIG", id, "更新系统配置状态：" + config.getConfigKey() + " -> " + request.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysConfig config = getById(id);
        sysConfigMapper.deleteById(id);
        auditLogService.recordSuccess("系统配置", "DELETE", "SYS_CONFIG", id, "删除系统配置：" + config.getConfigKey());
    }

    private SysConfig getById(Long id) {
        SysConfig config = sysConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("系统配置不存在");
        }
        return config;
    }

    private void ensureConfigKeyUnique(String configKey, Long currentId) {
        SysConfig existing = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, configKey));
        if (existing != null && !Objects.equals(existing.getId(), currentId)) {
            throw new BusinessException("配置键已存在");
        }
    }

    private void fill(SysConfig config, ConfigSaveRequest request) {
        config.setConfigKey(request.getConfigKey());
        config.setConfigName(request.getConfigName());
        config.setConfigValue(request.getConfigValue());
        config.setConfigType(request.getConfigType());
        config.setStatus(request.getStatus());
        config.setRemark(request.getRemark());
    }

    private ConfigVO toVO(SysConfig config) {
        ConfigVO vo = new ConfigVO();
        vo.setId(config.getId());
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigName(config.getConfigName());
        vo.setConfigValue(config.getConfigValue());
        vo.setConfigType(config.getConfigType());
        vo.setStatus(config.getStatus());
        vo.setRemark(config.getRemark());
        vo.setCreatedAt(config.getCreatedAt());
        vo.setUpdatedAt(config.getUpdatedAt());
        return vo;
    }
}
