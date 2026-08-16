package com.xinchuang.diagnosis.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.system.dto.DictItemPageQuery;
import com.xinchuang.diagnosis.system.dto.DictItemSaveRequest;
import com.xinchuang.diagnosis.system.dto.DictTypePageQuery;
import com.xinchuang.diagnosis.system.dto.DictTypeSaveRequest;
import com.xinchuang.diagnosis.system.dto.SimpleStatusRequest;
import com.xinchuang.diagnosis.system.entity.SysDictItem;
import com.xinchuang.diagnosis.system.entity.SysDictType;
import com.xinchuang.diagnosis.system.mapper.SysDictItemMapper;
import com.xinchuang.diagnosis.system.mapper.SysDictTypeMapper;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import com.xinchuang.diagnosis.system.service.SysDictService;
import com.xinchuang.diagnosis.system.vo.DictItemVO;
import com.xinchuang.diagnosis.system.vo.DictTypeVO;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SysDictServiceImpl implements SysDictService {
    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictItemMapper dictItemMapper;
    private final AuditLogService auditLogService;

    public SysDictServiceImpl(SysDictTypeMapper dictTypeMapper,
                              SysDictItemMapper dictItemMapper,
                              AuditLogService auditLogService) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictItemMapper = dictItemMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    public PageResponse<DictTypeVO> pageTypes(DictTypePageQuery query) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysDictType::getDictCode, query.getKeyword())
                .or()
                .like(SysDictType::getDictName, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getDictCode())) {
            wrapper.like(SysDictType::getDictCode, query.getDictCode());
        }
        if (StringUtils.hasText(query.getDictName())) {
            wrapper.like(SysDictType::getDictName, query.getDictName());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(SysDictType::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysDictType::getUpdatedAt);
        Page<SysDictType> page = dictTypeMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(page.getRecords().stream().map(this::toTypeVO).toList(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public DictTypeVO typeDetail(Long id) {
        return toTypeVO(getTypeById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictTypeVO createType(DictTypeSaveRequest request) {
        ensureTypeCodeUnique(request.getDictCode(), null);
        SysDictType type = new SysDictType();
        fillType(type, request);
        dictTypeMapper.insert(type);
        auditLogService.recordSuccess("字典管理", "CREATE", "SYS_DICT_TYPE", type.getId(), "新增字典类型：" + type.getDictCode());
        return typeDetail(type.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictTypeVO updateType(Long id, DictTypeSaveRequest request) {
        SysDictType type = getTypeById(id);
        ensureTypeCodeUnique(request.getDictCode(), id);
        String oldCode = type.getDictCode();
        fillType(type, request);
        dictTypeMapper.updateById(type);
        if (!Objects.equals(oldCode, type.getDictCode())) {
            updateItemDictCode(oldCode, type.getDictCode());
        }
        auditLogService.recordSuccess("字典管理", "UPDATE", "SYS_DICT_TYPE", id, "编辑字典类型：" + type.getDictCode());
        return typeDetail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTypeStatus(Long id, SimpleStatusRequest request) {
        SysDictType type = getTypeById(id);
        type.setStatus(request.getStatus());
        dictTypeMapper.updateById(type);
        auditLogService.recordSuccess("字典管理", "STATUS", "SYS_DICT_TYPE", id, "更新字典类型状态：" + type.getDictCode() + " -> " + request.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteType(Long id) {
        SysDictType type = getTypeById(id);
        Long count = dictItemMapper.selectCount(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictCode, type.getDictCode()));
        if (count != null && count > 0) {
            throw new BusinessException("该字典类型存在字典项，不能删除");
        }
        dictTypeMapper.deleteById(id);
        auditLogService.recordSuccess("字典管理", "DELETE", "SYS_DICT_TYPE", id, "删除字典类型：" + type.getDictCode());
    }

    @Override
    public PageResponse<DictItemVO> pageItems(DictItemPageQuery query) {
        LambdaQueryWrapper<SysDictItem> wrapper = buildItemQuery(query);
        wrapper.orderByAsc(SysDictItem::getSortOrder).orderByDesc(SysDictItem::getUpdatedAt);
        Page<SysDictItem> page = dictItemMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(page.getRecords().stream().map(this::toItemVO).toList(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public List<DictItemVO> enabledItems(String dictCode) {
        ensureTypeExists(dictCode);
        return dictItemMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
            .eq(SysDictItem::getDictCode, dictCode)
            .eq(SysDictItem::getStatus, "ENABLED")
            .orderByAsc(SysDictItem::getSortOrder)
            .orderByAsc(SysDictItem::getItemValue)
        ).stream().map(this::toItemVO).toList();
    }

    @Override
    public DictItemVO itemDetail(Long id) {
        return toItemVO(getItemById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictItemVO createItem(DictItemSaveRequest request) {
        ensureTypeExists(request.getDictCode());
        ensureItemValueUnique(request.getDictCode(), request.getItemValue(), null);
        SysDictItem item = new SysDictItem();
        fillItem(item, request);
        dictItemMapper.insert(item);
        auditLogService.recordSuccess("字典管理", "CREATE", "SYS_DICT_ITEM", item.getId(), "新增字典项：" + item.getDictCode() + "/" + item.getItemValue());
        return itemDetail(item.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictItemVO updateItem(Long id, DictItemSaveRequest request) {
        SysDictItem item = getItemById(id);
        ensureTypeExists(request.getDictCode());
        ensureItemValueUnique(request.getDictCode(), request.getItemValue(), id);
        fillItem(item, request);
        dictItemMapper.updateById(item);
        auditLogService.recordSuccess("字典管理", "UPDATE", "SYS_DICT_ITEM", id, "编辑字典项：" + item.getDictCode() + "/" + item.getItemValue());
        return itemDetail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItemStatus(Long id, SimpleStatusRequest request) {
        SysDictItem item = getItemById(id);
        item.setStatus(request.getStatus());
        dictItemMapper.updateById(item);
        auditLogService.recordSuccess("字典管理", "STATUS", "SYS_DICT_ITEM", id, "更新字典项状态：" + item.getDictCode() + "/" + item.getItemValue() + " -> " + request.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long id) {
        SysDictItem item = getItemById(id);
        dictItemMapper.deleteById(id);
        auditLogService.recordSuccess("字典管理", "DELETE", "SYS_DICT_ITEM", id, "删除字典项：" + item.getDictCode() + "/" + item.getItemValue());
    }

    private LambdaQueryWrapper<SysDictItem> buildItemQuery(DictItemPageQuery query) {
        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysDictItem::getItemValue, query.getKeyword())
                .or()
                .like(SysDictItem::getItemLabel, query.getKeyword())
                .or()
                .like(SysDictItem::getDictCode, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getDictCode())) {
            wrapper.eq(SysDictItem::getDictCode, query.getDictCode());
        }
        if (StringUtils.hasText(query.getItemValue())) {
            wrapper.like(SysDictItem::getItemValue, query.getItemValue());
        }
        if (StringUtils.hasText(query.getItemLabel())) {
            wrapper.like(SysDictItem::getItemLabel, query.getItemLabel());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(SysDictItem::getStatus, query.getStatus());
        }
        return wrapper;
    }

    private SysDictType getTypeById(Long id) {
        SysDictType type = dictTypeMapper.selectById(id);
        if (type == null) {
            throw new BusinessException("字典类型不存在");
        }
        return type;
    }

    private SysDictItem getItemById(Long id) {
        SysDictItem item = dictItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException("字典项不存在");
        }
        return item;
    }

    private void ensureTypeExists(String dictCode) {
        Long count = dictTypeMapper.selectCount(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getDictCode, dictCode));
        if (count == null || count == 0) {
            throw new BusinessException("字典类型不存在");
        }
    }

    private void ensureTypeCodeUnique(String dictCode, Long currentId) {
        SysDictType existing = dictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getDictCode, dictCode));
        if (existing != null && !Objects.equals(existing.getId(), currentId)) {
            throw new BusinessException("字典编码已存在");
        }
    }

    private void ensureItemValueUnique(String dictCode, String itemValue, Long currentId) {
        SysDictItem existing = dictItemMapper.selectOne(new LambdaQueryWrapper<SysDictItem>()
            .eq(SysDictItem::getDictCode, dictCode)
            .eq(SysDictItem::getItemValue, itemValue));
        if (existing != null && !Objects.equals(existing.getId(), currentId)) {
            throw new BusinessException("字典项值已存在");
        }
    }

    private void updateItemDictCode(String oldCode, String newCode) {
        dictItemMapper.update(new LambdaUpdateWrapper<SysDictItem>()
            .set(SysDictItem::getDictCode, newCode)
            .eq(SysDictItem::getDictCode, oldCode));
    }

    private void fillType(SysDictType type, DictTypeSaveRequest request) {
        type.setDictCode(request.getDictCode());
        type.setDictName(request.getDictName());
        type.setStatus(request.getStatus());
        type.setRemark(request.getRemark());
    }

    private void fillItem(SysDictItem item, DictItemSaveRequest request) {
        item.setDictCode(request.getDictCode());
        item.setItemValue(request.getItemValue());
        item.setItemLabel(request.getItemLabel());
        item.setTagType(request.getTagType());
        item.setSortOrder(request.getSortOrder());
        item.setStatus(request.getStatus());
        item.setRemark(request.getRemark());
    }

    private DictTypeVO toTypeVO(SysDictType type) {
        DictTypeVO vo = new DictTypeVO();
        vo.setId(type.getId());
        vo.setDictCode(type.getDictCode());
        vo.setDictName(type.getDictName());
        vo.setStatus(type.getStatus());
        vo.setRemark(type.getRemark());
        vo.setCreatedAt(type.getCreatedAt());
        vo.setUpdatedAt(type.getUpdatedAt());
        return vo;
    }

    private DictItemVO toItemVO(SysDictItem item) {
        DictItemVO vo = new DictItemVO();
        vo.setId(item.getId());
        vo.setDictCode(item.getDictCode());
        vo.setItemValue(item.getItemValue());
        vo.setItemLabel(item.getItemLabel());
        vo.setTagType(item.getTagType());
        vo.setSortOrder(item.getSortOrder());
        vo.setStatus(item.getStatus());
        vo.setRemark(item.getRemark());
        vo.setCreatedAt(item.getCreatedAt());
        vo.setUpdatedAt(item.getUpdatedAt());
        return vo;
    }
}
