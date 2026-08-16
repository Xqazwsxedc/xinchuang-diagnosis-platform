package com.xinchuang.diagnosis.system.controller;

import com.xinchuang.diagnosis.common.api.ApiResponse;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.system.dto.DictItemPageQuery;
import com.xinchuang.diagnosis.system.dto.DictItemSaveRequest;
import com.xinchuang.diagnosis.system.dto.DictTypePageQuery;
import com.xinchuang.diagnosis.system.dto.DictTypeSaveRequest;
import com.xinchuang.diagnosis.system.dto.SimpleStatusRequest;
import com.xinchuang.diagnosis.system.service.SysDictService;
import com.xinchuang.diagnosis.system.vo.DictItemVO;
import com.xinchuang.diagnosis.system.vo.DictTypeVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/dict")
public class SysDictController {
    private final SysDictService sysDictService;

    public SysDictController(SysDictService sysDictService) {
        this.sysDictService = sysDictService;
    }

    @GetMapping("/types")
    public ApiResponse<PageResponse<DictTypeVO>> typePage(@Valid @ModelAttribute DictTypePageQuery query) {
        return ApiResponse.ok(sysDictService.pageTypes(query));
    }

    @GetMapping("/types/{id}")
    public ApiResponse<DictTypeVO> typeDetail(@PathVariable Long id) {
        return ApiResponse.ok(sysDictService.typeDetail(id));
    }

    @PostMapping("/types")
    public ApiResponse<DictTypeVO> createType(@Valid @RequestBody DictTypeSaveRequest request) {
        return ApiResponse.ok(sysDictService.createType(request));
    }

    @PutMapping("/types/{id}")
    public ApiResponse<DictTypeVO> updateType(@PathVariable Long id, @Valid @RequestBody DictTypeSaveRequest request) {
        return ApiResponse.ok(sysDictService.updateType(id, request));
    }

    @PatchMapping("/types/{id}/status")
    public ApiResponse<Void> updateTypeStatus(@PathVariable Long id, @Valid @RequestBody SimpleStatusRequest request) {
        sysDictService.updateTypeStatus(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/types/{id}")
    public ApiResponse<Void> deleteType(@PathVariable Long id) {
        sysDictService.deleteType(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/items")
    public ApiResponse<PageResponse<DictItemVO>> itemPage(@Valid @ModelAttribute DictItemPageQuery query) {
        return ApiResponse.ok(sysDictService.pageItems(query));
    }

    @GetMapping("/items/enabled/{dictCode}")
    public ApiResponse<List<DictItemVO>> enabledItems(@PathVariable String dictCode) {
        return ApiResponse.ok(sysDictService.enabledItems(dictCode));
    }

    @GetMapping("/items/{id}")
    public ApiResponse<DictItemVO> itemDetail(@PathVariable Long id) {
        return ApiResponse.ok(sysDictService.itemDetail(id));
    }

    @PostMapping("/items")
    public ApiResponse<DictItemVO> createItem(@Valid @RequestBody DictItemSaveRequest request) {
        return ApiResponse.ok(sysDictService.createItem(request));
    }

    @PutMapping("/items/{id}")
    public ApiResponse<DictItemVO> updateItem(@PathVariable Long id, @Valid @RequestBody DictItemSaveRequest request) {
        return ApiResponse.ok(sysDictService.updateItem(id, request));
    }

    @PatchMapping("/items/{id}/status")
    public ApiResponse<Void> updateItemStatus(@PathVariable Long id, @Valid @RequestBody SimpleStatusRequest request) {
        sysDictService.updateItemStatus(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        sysDictService.deleteItem(id);
        return ApiResponse.ok(null);
    }
}
