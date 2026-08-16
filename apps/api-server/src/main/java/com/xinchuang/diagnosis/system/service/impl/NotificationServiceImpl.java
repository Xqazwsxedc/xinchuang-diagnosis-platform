package com.xinchuang.diagnosis.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.security.LoginUser;
import com.xinchuang.diagnosis.security.UserContext;
import com.xinchuang.diagnosis.system.dto.NotificationCreateCommand;
import com.xinchuang.diagnosis.system.dto.NotificationPageQuery;
import com.xinchuang.diagnosis.system.entity.SysRole;
import com.xinchuang.diagnosis.system.entity.SysUser;
import com.xinchuang.diagnosis.system.entity.SysUserRole;
import com.xinchuang.diagnosis.system.entity.SystemNotification;
import com.xinchuang.diagnosis.system.mapper.SysRoleMapper;
import com.xinchuang.diagnosis.system.mapper.SysUserMapper;
import com.xinchuang.diagnosis.system.mapper.SysUserRoleMapper;
import com.xinchuang.diagnosis.system.mapper.SystemNotificationMapper;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import com.xinchuang.diagnosis.system.service.NotificationService;
import com.xinchuang.diagnosis.system.vo.NotificationVO;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final SystemNotificationMapper notificationMapper;
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final AuditLogService auditLogService;

    public NotificationServiceImpl(SystemNotificationMapper notificationMapper,
                                   SysUserMapper sysUserMapper,
                                   SysRoleMapper sysRoleMapper,
                                   SysUserRoleMapper sysUserRoleMapper,
                                   AuditLogService auditLogService) {
        this.notificationMapper = notificationMapper;
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    public PageResponse<NotificationVO> pageMine(NotificationPageQuery query) {
        Long userId = currentUserId();
        LambdaQueryWrapper<SystemNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotification::getReceiverId, userId);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SystemNotification::getTitle, query.getKeyword())
                .or()
                .like(SystemNotification::getContent, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getNoticeType())) {
            wrapper.eq(SystemNotification::getNoticeType, query.getNoticeType());
        }
        if (StringUtils.hasText(query.getPriority())) {
            wrapper.eq(SystemNotification::getPriority, query.getPriority());
        }
        if (StringUtils.hasText(query.getReadStatus())) {
            wrapper.eq(SystemNotification::getReadStatus, query.getReadStatus());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(SystemNotification::getCreatedAt, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(SystemNotification::getCreatedAt, query.getEndTime());
        }
        wrapper.orderByAsc(SystemNotification::getReadStatus).orderByDesc(SystemNotification::getCreatedAt);
        Page<SystemNotification> page = notificationMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(page.getRecords().stream().map(this::toVO).toList(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public long unreadCount() {
        Long count = notificationMapper.selectCount(new LambdaQueryWrapper<SystemNotification>()
            .eq(SystemNotification::getReceiverId, currentUserId())
            .eq(SystemNotification::getReadStatus, "UNREAD"));
        return count == null ? 0 : count;
    }

    @Override
    public NotificationVO detailMine(Long id) {
        return toVO(getMineById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long id) {
        SystemNotification notification = getMineById(id);
        if (!"READ".equals(notification.getReadStatus())) {
            notification.setReadStatus("READ");
            notification.setReadAt(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
        auditLogService.recordSuccess("通知中心", "READ", "SYSTEM_NOTIFICATION", id, "标记通知已读：" + notification.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead() {
        Long userId = currentUserId();
        Long unreadCount = notificationMapper.selectCount(new LambdaQueryWrapper<SystemNotification>()
            .eq(SystemNotification::getReceiverId, userId)
            .eq(SystemNotification::getReadStatus, "UNREAD"));
        LocalDateTime now = LocalDateTime.now();
        notificationMapper.update(new LambdaUpdateWrapper<SystemNotification>()
            .set(SystemNotification::getReadStatus, "READ")
            .set(SystemNotification::getReadAt, now)
            .eq(SystemNotification::getReceiverId, userId)
            .eq(SystemNotification::getReadStatus, "UNREAD"));
        auditLogService.recordSuccess("通知中心", "READ_ALL", "SYSTEM_NOTIFICATION", null,
            "全部通知标记已读，数量：" + (unreadCount == null ? 0 : unreadCount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMine(Long id) {
        SystemNotification notification = getMineById(id);
        notificationMapper.deleteById(id);
        auditLogService.recordSuccess("通知中心", "DELETE", "SYSTEM_NOTIFICATION", id, "删除通知：" + notification.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearRead() {
        Long userId = currentUserId();
        Long readCount = notificationMapper.selectCount(new LambdaQueryWrapper<SystemNotification>()
            .eq(SystemNotification::getReceiverId, userId)
            .eq(SystemNotification::getReadStatus, "READ"));
        notificationMapper.delete(new LambdaQueryWrapper<SystemNotification>()
            .eq(SystemNotification::getReceiverId, userId)
            .eq(SystemNotification::getReadStatus, "READ"));
        auditLogService.recordSuccess("通知中心", "CLEAR_READ", "SYSTEM_NOTIFICATION", null,
            "清空已读通知，数量：" + (readCount == null ? 0 : readCount));
    }

    @Override
    public void create(NotificationCreateCommand command) {
        if (command.getReceiverId() == null || !StringUtils.hasText(command.getTitle())) {
            return;
        }
        SysUser receiver = sysUserMapper.selectById(command.getReceiverId());
        if (receiver == null) {
            return;
        }
        SystemNotification notification = new SystemNotification();
        notification.setReceiverId(receiver.getId());
        notification.setReceiverNameSnapshot(receiver.getRealName());
        notification.setTitle(command.getTitle());
        notification.setContent(command.getContent());
        notification.setNoticeType(StringUtils.hasText(command.getNoticeType()) ? command.getNoticeType() : "SYSTEM");
        notification.setPriority(StringUtils.hasText(command.getPriority()) ? command.getPriority() : "NORMAL");
        notification.setReadStatus("UNREAD");
        notification.setBusinessType(command.getBusinessType());
        notification.setBusinessId(command.getBusinessId());
        notificationMapper.insert(notification);
    }

    @Override
    public void notifyAdmins(String title, String content, String noticeType, String priority, String businessType, Long businessId) {
        SysRole adminRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, "admin"));
        if (adminRole == null) {
            return;
        }
        List<Long> adminUserIds = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, adminRole.getId()))
            .stream()
            .map(SysUserRole::getUserId)
            .distinct()
            .toList();
        if (CollectionUtils.isEmpty(adminUserIds)) {
            return;
        }
        for (Long userId : adminUserIds) {
            NotificationCreateCommand command = new NotificationCreateCommand();
            command.setReceiverId(userId);
            command.setTitle(title);
            command.setContent(content);
            command.setNoticeType(noticeType);
            command.setPriority(priority);
            command.setBusinessType(businessType);
            command.setBusinessId(businessId);
            create(command);
        }
    }

    private SystemNotification getMineById(Long id) {
        SystemNotification notification = notificationMapper.selectOne(new LambdaQueryWrapper<SystemNotification>()
            .eq(SystemNotification::getId, id)
            .eq(SystemNotification::getReceiverId, currentUserId()));
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        return notification;
    }

    private Long currentUserId() {
        LoginUser loginUser = UserContext.get();
        if (loginUser == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return loginUser.getUserId();
    }

    private NotificationVO toVO(SystemNotification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setReceiverId(notification.getReceiverId());
        vo.setReceiverNameSnapshot(notification.getReceiverNameSnapshot());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setNoticeType(notification.getNoticeType());
        vo.setPriority(notification.getPriority());
        vo.setReadStatus(notification.getReadStatus());
        vo.setReadAt(notification.getReadAt());
        vo.setBusinessType(notification.getBusinessType());
        vo.setBusinessId(notification.getBusinessId());
        vo.setCreatedAt(notification.getCreatedAt());
        return vo;
    }
}
