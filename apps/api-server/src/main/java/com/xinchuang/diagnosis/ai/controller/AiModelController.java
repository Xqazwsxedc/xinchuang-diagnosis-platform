package com.xinchuang.diagnosis.ai.controller;

import com.xinchuang.diagnosis.ai.dto.AiPageQuery;
import com.xinchuang.diagnosis.ai.entity.AiModel;
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
@RequestMapping("/ai/models")
public class AiModelController {
    private final AiManagementService aiManagementService;

    public AiModelController(AiManagementService aiManagementService) {
        this.aiManagementService = aiManagementService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AiModel>> page(@Valid @ModelAttribute AiPageQuery query) {
        return ApiResponse.ok(aiManagementService.pageModels(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<AiModel> detail(@PathVariable Long id) {
        return ApiResponse.ok(aiManagementService.modelDetail(id));
    }

    @PostMapping
    public ApiResponse<AiModel> create(@RequestBody AiModel request) {
        return ApiResponse.ok(aiManagementService.createModel(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AiModel> update(@PathVariable Long id, @RequestBody AiModel request) {
        return ApiResponse.ok(aiManagementService.updateModel(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        aiManagementService.deleteModel(id);
        return ApiResponse.ok(null);
    }
}
