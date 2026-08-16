package com.xinchuang.diagnosis.system.controller;

import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.system.dto.NotificationPageQuery;
import com.xinchuang.diagnosis.system.service.NotificationService;
import com.xinchuang.diagnosis.system.vo.NotificationVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<NotificationVO>> page(@Valid @ModelAttribute NotificationPageQuery query) {
        return ApiResponse.ok(notificationService.pageMine(query));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount() {
        return ApiResponse.ok(notificationService.unreadCount());
    }

    @GetMapping("/{id}")
    public ApiResponse<NotificationVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(notificationService.detailMine(id));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        notificationService.markAllRead();
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        notificationService.deleteMine(id);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/read")
    public ApiResponse<Void> clearRead() {
        notificationService.clearRead();
        return ApiResponse.ok(null);
    }
}
