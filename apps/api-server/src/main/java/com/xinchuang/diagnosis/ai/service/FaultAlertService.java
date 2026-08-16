package com.xinchuang.diagnosis.ai.service;

import com.xinchuang.diagnosis.ai.dto.FaultAlertPageQuery;
import com.xinchuang.diagnosis.ai.dto.ResultHandleRequest;
import com.xinchuang.diagnosis.ai.vo.FaultAlertDetailVO;
import com.xinchuang.diagnosis.ai.vo.FaultAlertVO;
import com.xinchuang.diagnosis.common.api.PageResponse;

public interface FaultAlertService {
    PageResponse<FaultAlertVO> page(FaultAlertPageQuery query);
    FaultAlertDetailVO detail(Long id);
    FaultAlertDetailVO handle(Long id, ResultHandleRequest request);
    void delete(Long id);
}
