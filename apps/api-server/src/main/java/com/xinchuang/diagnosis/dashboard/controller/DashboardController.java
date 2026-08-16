package com.xinchuang.diagnosis.dashboard.controller;

import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.dashboard.service.DashboardService;
import com.xinchuang.diagnosis.dashboard.vo.DashboardOverviewVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public ApiResponse<DashboardOverviewVO> overview() {
        return ApiResponse.ok(dashboardService.overview());
    }
}
