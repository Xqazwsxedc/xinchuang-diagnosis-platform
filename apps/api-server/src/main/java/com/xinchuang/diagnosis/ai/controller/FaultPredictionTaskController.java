package com.xinchuang.diagnosis.ai.controller;

import com.xinchuang.diagnosis.ai.dto.FaultTaskPageQuery;
import com.xinchuang.diagnosis.ai.dto.FaultTaskSaveRequest;
import com.xinchuang.diagnosis.ai.dto.TaskExecuteRequest;
import com.xinchuang.diagnosis.ai.entity.TaskExecRecord;
import com.xinchuang.diagnosis.ai.service.FaultPredictionTaskService;
import com.xinchuang.diagnosis.ai.vo.FaultTaskDetailVO;
import com.xinchuang.diagnosis.ai.vo.FaultTaskVO;
import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.common.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/fault-prediction/tasks")
public class FaultPredictionTaskController {
    private final FaultPredictionTaskService faultTaskService;
    public FaultPredictionTaskController(FaultPredictionTaskService faultTaskService) { this.faultTaskService = faultTaskService; }
    @GetMapping public ApiResponse<PageResponse<FaultTaskVO>> page(@Valid @ModelAttribute FaultTaskPageQuery query) { return ApiResponse.ok(faultTaskService.page(query)); }
    @GetMapping("/{id}") public ApiResponse<FaultTaskDetailVO> detail(@PathVariable Long id) { return ApiResponse.ok(faultTaskService.detail(id)); }
    @PostMapping public ApiResponse<FaultTaskDetailVO> create(@Valid @RequestBody FaultTaskSaveRequest request) { return ApiResponse.ok(faultTaskService.create(request)); }
    @PutMapping("/{id}") public ApiResponse<FaultTaskDetailVO> update(@PathVariable Long id, @Valid @RequestBody FaultTaskSaveRequest request) { return ApiResponse.ok(faultTaskService.update(id, request)); }
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable Long id) { faultTaskService.delete(id); return ApiResponse.ok(null); }
    @PostMapping("/{id}/execute") public ApiResponse<TaskExecRecord> execute(@PathVariable Long id, @Valid @RequestBody TaskExecuteRequest request) { return ApiResponse.ok(faultTaskService.execute(id, request)); }
}
