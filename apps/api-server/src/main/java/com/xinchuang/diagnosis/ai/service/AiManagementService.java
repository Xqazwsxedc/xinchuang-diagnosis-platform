package com.xinchuang.diagnosis.ai.service;

import com.xinchuang.diagnosis.ai.dto.AiPageQuery;
import com.xinchuang.diagnosis.ai.dto.ResultHandleRequest;
import com.xinchuang.diagnosis.ai.dto.TaskExecuteRequest;
import com.xinchuang.diagnosis.ai.entity.AiModel;
import com.xinchuang.diagnosis.ai.entity.AiTask;
import com.xinchuang.diagnosis.ai.entity.DefectReport;
import com.xinchuang.diagnosis.ai.entity.FaultReport;
import com.xinchuang.diagnosis.ai.entity.KnowledgeInstance;
import com.xinchuang.diagnosis.ai.entity.OptimizationReport;
import com.xinchuang.diagnosis.ai.entity.TaskExecRecord;
import com.xinchuang.diagnosis.common.api.PageResponse;
import java.util.List;

public interface AiManagementService {
    PageResponse<AiModel> pageModels(AiPageQuery query);
    AiModel modelDetail(Long id);
    AiModel createModel(AiModel request);
    AiModel updateModel(Long id, AiModel request);
    void deleteModel(Long id);

    PageResponse<KnowledgeInstance> pageKnowledge(AiPageQuery query);
    KnowledgeInstance knowledgeDetail(Long id);
    KnowledgeInstance createKnowledge(KnowledgeInstance request);
    KnowledgeInstance updateKnowledge(Long id, KnowledgeInstance request);
    void deleteKnowledge(Long id);

    PageResponse<AiTask> pageTasks(AiPageQuery query);
    AiTask taskDetail(Long id);
    AiTask createTask(AiTask request);
    AiTask updateTask(Long id, AiTask request);
    void deleteTask(Long id);
    TaskExecRecord executeTask(Long id, TaskExecuteRequest request);
    PageResponse<TaskExecRecord> pageExecutions(AiPageQuery query);

    PageResponse<FaultReport> pageFaultReports(AiPageQuery query);
    FaultReport faultReportDetail(Long id);
    FaultReport createFaultReport(FaultReport request);
    FaultReport updateFaultReport(Long id, FaultReport request);
    void handleFaultReport(Long id, ResultHandleRequest request);
    void deleteFaultReport(Long id);

    PageResponse<DefectReport> pageDefectReports(AiPageQuery query);
    DefectReport defectReportDetail(Long id);
    DefectReport createDefectReport(DefectReport request);
    DefectReport updateDefectReport(Long id, DefectReport request);
    void handleDefectReport(Long id, ResultHandleRequest request);
    void deleteDefectReport(Long id);
    byte[] exportDefectReports(List<Long> reportIds);

    PageResponse<OptimizationReport> pageOptimizationReports(AiPageQuery query);
    OptimizationReport optimizationReportDetail(Long id);
    byte[] exportOptimizationReports(List<Long> reportIds);
    OptimizationReport createOptimizationReport(OptimizationReport request);
    OptimizationReport updateOptimizationReport(Long id, OptimizationReport request);
    void handleOptimizationReport(Long id, ResultHandleRequest request);
    void deleteOptimizationReport(Long id);
}
