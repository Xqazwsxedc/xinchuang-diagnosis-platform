package com.xinchuang.diagnosis.system.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.security.LoginUser;
import com.xinchuang.diagnosis.security.UserContext;
import com.xinchuang.diagnosis.system.dto.NotificationCreateCommand;
import com.xinchuang.diagnosis.system.entity.SysUser;
import com.xinchuang.diagnosis.system.entity.SystemNotification;
import com.xinchuang.diagnosis.system.mapper.SysRoleMapper;
import com.xinchuang.diagnosis.system.mapper.SysUserMapper;
import com.xinchuang.diagnosis.system.mapper.SysUserRoleMapper;
import com.xinchuang.diagnosis.system.mapper.SystemNotificationMapper;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Verifies notification ownership, no-op safety, defaults, and audit behavior. */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplV21Test {
    @Mock private SystemNotificationMapper notificationMapper;
    @Mock private SysUserMapper sysUserMapper;
    @Mock private SysRoleMapper sysRoleMapper;
    @Mock private SysUserRoleMapper sysUserRoleMapper;
    @Mock private AuditLogService auditLogService;
    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(notificationMapper, sysUserMapper, sysRoleMapper, sysUserRoleMapper, auditLogService);
    }

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    void unreadCountRejectsMissingUserContext() {
        BusinessException exception = assertThrows(BusinessException.class, service::unreadCount);

        assertEquals(401, exception.getCode());
        verify(notificationMapper, never()).selectCount(any());
    }

    @Test
    void createIgnoresCommandWithoutReceiverOrTitle() {
        service.create(new NotificationCreateCommand());

        verify(sysUserMapper, never()).selectById(any());
        verify(notificationMapper, never()).insert(any(SystemNotification.class));
    }

    @Test
    void createIgnoresUnknownReceiver() {
        NotificationCreateCommand command = command(9L, "待发送消息");
        when(sysUserMapper.selectById(9L)).thenReturn(null);

        service.create(command);

        verify(notificationMapper, never()).insert(any(SystemNotification.class));
    }

    @Test
    void createUsesSecureDefaultsAndReceiverSnapshot() {
        SysUser receiver = new SysUser();
        receiver.setId(9L);
        receiver.setRealName("测试接收人");
        when(sysUserMapper.selectById(9L)).thenReturn(receiver);

        service.create(command(9L, "待发送消息"));

        ArgumentCaptor<SystemNotification> captor = ArgumentCaptor.forClass(SystemNotification.class);
        verify(notificationMapper).insert(captor.capture());
        assertEquals("测试接收人", captor.getValue().getReceiverNameSnapshot());
        assertEquals("SYSTEM", captor.getValue().getNoticeType());
        assertEquals("NORMAL", captor.getValue().getPriority());
        assertEquals("UNREAD", captor.getValue().getReadStatus());
    }

    @Test
    void markReadAlreadyReadNotificationSkipsUpdateButAuditsAction() {
        setCurrentUser(1L);
        SystemNotification notification = new SystemNotification();
        notification.setId(7L);
        notification.setTitle("已读消息");
        notification.setReadStatus("READ");
        when(notificationMapper.selectOne(any())).thenReturn(notification);

        service.markRead(7L);

        verify(notificationMapper, never()).updateById(any(SystemNotification.class));
        verify(auditLogService).recordSuccess("通知中心", "READ", "SYSTEM_NOTIFICATION", 7L, "标记通知已读：已读消息");
    }

    private NotificationCreateCommand command(Long receiverId, String title) {
        NotificationCreateCommand command = new NotificationCreateCommand();
        command.setReceiverId(receiverId);
        command.setTitle(title);
        return command;
    }

    private void setCurrentUser(Long userId) {
        LoginUser user = new LoginUser();
        user.setUserId(userId);
        UserContext.set(user);
    }
}
