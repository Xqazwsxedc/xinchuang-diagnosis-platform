package com.xinchuang.diagnosis.system.service;

import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.system.dto.NotificationCreateCommand;
import com.xinchuang.diagnosis.system.dto.NotificationPageQuery;
import com.xinchuang.diagnosis.system.vo.NotificationVO;

public interface NotificationService {
    PageResponse<NotificationVO> pageMine(NotificationPageQuery query);

    long unreadCount();

    NotificationVO detailMine(Long id);

    void markRead(Long id);

    void markAllRead();

    void deleteMine(Long id);

    void clearRead();

    void create(NotificationCreateCommand command);

    void notifyAdmins(String title, String content, String noticeType, String priority, String businessType, Long businessId);
}
