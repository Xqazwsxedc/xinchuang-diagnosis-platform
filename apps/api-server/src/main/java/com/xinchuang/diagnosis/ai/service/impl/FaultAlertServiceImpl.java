package com.xinchuang.diagnosis.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinchuang.diagnosis.ai.dto.AiPageQuery;
import com.xinchuang.diagnosis.ai.dto.FaultAlertPageQuery;
import com.xinchuang.diagnosis.ai.dto.ResultHandleRequest;
import com.xinchuang.diagnosis.ai.entity.FaultReport;
import com.xinchuang.diagnosis.ai.entity.ResultStatusLog;
import com.xinchuang.diagnosis.ai.mapper.ResultStatusLogMapper;
import com.xinchuang.diagnosis.ai.service.AiManagementService;
import com.xinchuang.diagnosis.ai.service.FaultAlertService;
import com.xinchuang.diagnosis.ai.vo.FaultAlertDetailVO;
import com.xinchuang.diagnosis.ai.vo.FaultAlertVO;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class FaultAlertServiceImpl implements FaultAlertService {
    private final AiManagementService aiManagementService;
    private final ResultStatusLogMapper resultStatusLogMapper;

    public FaultAlertServiceImpl(AiManagementService aiManagementService, ResultStatusLogMapper resultStatusLogMapper) {
        this.aiManagementService = aiManagementService;
        this.resultStatusLogMapper = resultStatusLogMapper;
    }

    @Override
    public PageResponse<FaultAlertVO> page(FaultAlertPageQuery query) {
        AiPageQuery legacyQuery = new AiPageQuery();
        legacyQuery.setPageNo(query.getPageNo());
        legacyQuery.setPageSize(query.getPageSize());
        legacyQuery.setKeyword(query.getKeyword());
        legacyQuery.setProcessStatus(query.getProcessStatus());
        legacyQuery.setLevel(query.getRiskLevel());
        legacyQuery.setTargetType(query.getTargetType());
        PageResponse<FaultReport> page = aiManagementService.pageFaultReports(legacyQuery);
        return PageResponse.of(page.getRecords().stream().map(FaultAlertVO::from).toList(), page.getTotal(), page.getPageNo(), page.getPageSize());
    }

    @Override
    public FaultAlertDetailVO detail(Long id) {
        FaultReport report = aiManagementService.faultReportDetail(id);
        Page<ResultStatusLog> logs = resultStatusLogMapper.selectPage(new Page<>(1, 50), new LambdaQueryWrapper<ResultStatusLog>()
            .eq(ResultStatusLog::getResultType, "FAULT")
            .eq(ResultStatusLog::getResultId, id)
            .orderByDesc(ResultStatusLog::getCreatedAt));
        return FaultAlertDetailVO.from(report, logs.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FaultAlertDetailVO handle(Long id, ResultHandleRequest request) {
        if (!StringUtils.hasText(request.getRemark())) {
            throw new BusinessException("处理备注不能为空");
        }
        aiManagementService.handleFaultReport(id, request);
        return detail(id);
    }

    @Override
    public void delete(Long id) {
        aiManagementService.deleteFaultReport(id);
    }
}
