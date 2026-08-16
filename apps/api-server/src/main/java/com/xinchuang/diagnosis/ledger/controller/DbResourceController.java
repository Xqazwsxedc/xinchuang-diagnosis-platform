package com.xinchuang.diagnosis.ledger.controller;

import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.ledger.dto.DbResourceSaveRequest;
import com.xinchuang.diagnosis.ledger.dto.LedgerPageQuery;
import com.xinchuang.diagnosis.ledger.service.DbResourceService;
import com.xinchuang.diagnosis.ledger.vo.DbResourceVO;
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
@RequestMapping("/ledger/db-resources")
public class DbResourceController {
    private final DbResourceService dbResourceService;

    public DbResourceController(DbResourceService dbResourceService) {
        this.dbResourceService = dbResourceService;
    }

    @GetMapping
    public ApiResponse<PageResponse<DbResourceVO>> page(@Valid @ModelAttribute LedgerPageQuery query) {
        return ApiResponse.ok(dbResourceService.page(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<DbResourceVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(dbResourceService.detail(id));
    }

    @PostMapping
    public ApiResponse<DbResourceVO> create(@Valid @RequestBody DbResourceSaveRequest request) {
        return ApiResponse.ok(dbResourceService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DbResourceVO> update(@PathVariable Long id, @Valid @RequestBody DbResourceSaveRequest request) {
        return ApiResponse.ok(dbResourceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dbResourceService.delete(id);
        return ApiResponse.ok(null);
    }
}

