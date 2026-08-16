package com.xinchuang.diagnosis.ledger.service;

import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.ledger.dto.BusinessSystemSaveRequest;
import com.xinchuang.diagnosis.ledger.dto.LedgerPageQuery;
import com.xinchuang.diagnosis.ledger.vo.BusinessSystemVO;

public interface BusinessSystemService {
    PageResponse<BusinessSystemVO> page(LedgerPageQuery query);

    BusinessSystemVO detail(Long id);

    BusinessSystemVO create(BusinessSystemSaveRequest request);

    BusinessSystemVO update(Long id, BusinessSystemSaveRequest request);

    void delete(Long id);
}

