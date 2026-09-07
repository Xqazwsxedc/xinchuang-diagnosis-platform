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

/**
 * 故障预测任务的 REST 接口。
 *
 * <p>接口只接收 DTO 并返回统一的 {@code ApiResponse}，任务校验、持久化和执行
 * 调度交给 {@link FaultPredictionTaskService}。</p>
 */
@RestController
@RequestMapping("/ai/fault-prediction/tasks")
public class FaultPredictionTaskController {
    private final FaultPredictionTaskService faultTaskService;

    public FaultPredictionTaskController(FaultPredictionTaskService faultTaskService) { this.faultTaskService = faultTaskService; }

    /** 分页查询故障预测任务，@ModelAttribute 将 query 参数绑定到 DTO。 */
    @GetMapping public ApiResponse<PageResponse<FaultTaskVO>> page(@Valid @ModelAttribute FaultTaskPageQuery query) { return ApiResponse.ok(faultTaskService.page(query)); }

    /** 查询任务详情、执行记录摘要和关联预警。 */
    @GetMapping("/{id}") public ApiResponse<FaultTaskDetailVO> detail(@PathVariable Long id) { return ApiResponse.ok(faultTaskService.detail(id)); }

    /** 新增任务，任务类型和任务编码由服务端控制。 */
    @PostMapping public ApiResponse<FaultTaskDetailVO> create(@Valid @RequestBody FaultTaskSaveRequest request) { return ApiResponse.ok(faultTaskService.create(request)); }

    /** 更新任务配置；服务层会保留已有任务编码。 */
    @PutMapping("/{id}") public ApiResponse<FaultTaskDetailVO> update(@PathVariable Long id, @Valid @RequestBody FaultTaskSaveRequest request) { return ApiResponse.ok(faultTaskService.update(id, request)); }

    /** 删除指定故障预测任务。 */
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable Long id) { faultTaskService.delete(id); return ApiResponse.ok(null); }

    /** 创建待处理执行记录，真正的模型调用由异步执行器完成。 */
    @PostMapping("/{id}/execute") public ApiResponse<TaskExecRecord> execute(@PathVariable Long id, @Valid @RequestBody TaskExecuteRequest request) { return ApiResponse.ok(faultTaskService.execute(id, request)); }
}
