package com.xinchuang.diagnosis.ledger.controller;

import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.ledger.dto.BusinessSystemSaveRequest;
import com.xinchuang.diagnosis.ledger.dto.LedgerPageQuery;
import com.xinchuang.diagnosis.ledger.service.BusinessSystemService;
import com.xinchuang.diagnosis.ledger.vo.BusinessSystemVO;
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
@RequestMapping("/ledger/business-systems")
public class BusinessSystemController {
    private final BusinessSystemService businessSystemService;

    public BusinessSystemController(BusinessSystemService businessSystemService) {
        this.businessSystemService = businessSystemService;
    }

    @GetMapping
    public ApiResponse<PageResponse<BusinessSystemVO>> page(@Valid @ModelAttribute LedgerPageQuery query) {
        return ApiResponse.ok(businessSystemService.page(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<BusinessSystemVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(businessSystemService.detail(id));
    }

    @PostMapping
    public ApiResponse<BusinessSystemVO> create(@Valid @RequestBody BusinessSystemSaveRequest request) {
        return ApiResponse.ok(businessSystemService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<BusinessSystemVO> update(@PathVariable Long id, @Valid @RequestBody BusinessSystemSaveRequest request) {
        return ApiResponse.ok(businessSystemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        businessSystemService.delete(id);
        return ApiResponse.ok(null);
    }
}

