package com.xinchuang.diagnosis.ai.controller;

import com.xinchuang.diagnosis.ai.dto.AiPageQuery;
import com.xinchuang.diagnosis.ai.dto.TaskExecuteRequest;
import com.xinchuang.diagnosis.ai.entity.AiTask;
import com.xinchuang.diagnosis.ai.entity.TaskExecRecord;
import com.xinchuang.diagnosis.ai.service.AiManagementService;
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
@RequestMapping("/ai/tasks")
public class AiTaskController {
    private final AiManagementService aiManagementService;

    public AiTaskController(AiManagementService aiManagementService) {
        this.aiManagementService = aiManagementService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AiTask>> page(@Valid @ModelAttribute AiPageQuery query) {
        return ApiResponse.ok(aiManagementService.pageTasks(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<AiTask> detail(@PathVariable Long id) {
        return ApiResponse.ok(aiManagementService.taskDetail(id));
    }

    @PostMapping
    public ApiResponse<AiTask> create(@RequestBody AiTask request) {
        return ApiResponse.ok(aiManagementService.createTask(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AiTask> update(@PathVariable Long id, @RequestBody AiTask request) {
        return ApiResponse.ok(aiManagementService.updateTask(id, request));
    }

    @PostMapping("/{id}/execute")
    public ApiResponse<TaskExecRecord> execute(@PathVariable Long id, @Valid @RequestBody TaskExecuteRequest request) {
        return ApiResponse.ok(aiManagementService.executeTask(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        aiManagementService.deleteTask(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/executions")
    public ApiResponse<PageResponse<TaskExecRecord>> executions(@Valid @ModelAttribute AiPageQuery query) {
        return ApiResponse.ok(aiManagementService.pageExecutions(query));
    }
}
