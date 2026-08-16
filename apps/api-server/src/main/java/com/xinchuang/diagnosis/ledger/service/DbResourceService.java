package com.xinchuang.diagnosis.ledger.service;

import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.ledger.dto.DbResourceSaveRequest;
import com.xinchuang.diagnosis.ledger.dto.LedgerPageQuery;
import com.xinchuang.diagnosis.ledger.vo.DbResourceVO;

public interface DbResourceService {
    PageResponse<DbResourceVO> page(LedgerPageQuery query);

    DbResourceVO detail(Long id);

    DbResourceVO create(DbResourceSaveRequest request);

    DbResourceVO update(Long id, DbResourceSaveRequest request);

    void delete(Long id);
}

