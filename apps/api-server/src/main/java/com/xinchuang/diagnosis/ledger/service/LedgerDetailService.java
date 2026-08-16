package com.xinchuang.diagnosis.ledger.service;

import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.ledger.dto.DbInstanceLogPageQuery;
import com.xinchuang.diagnosis.ledger.dto.DbMetricRangeQuery;
import com.xinchuang.diagnosis.ledger.vo.BusinessDataRecordVO;
import com.xinchuang.diagnosis.ledger.vo.DbInstanceLogVO;
import com.xinchuang.diagnosis.ledger.vo.DbMetricsOverviewVO;
import com.xinchuang.diagnosis.ledger.vo.LedgerDownload;
import java.util.List;

public interface LedgerDetailService {
    List<BusinessDataRecordVO> businessDataRecords(Long businessSystemId);

    DbMetricsOverviewVO dbMetrics(Long dbResourceId, DbMetricRangeQuery query);

    PageResponse<DbInstanceLogVO> dbLogs(Long dbResourceId, DbInstanceLogPageQuery query);

    LedgerDownload downloadBusinessData(Long businessSystemId, List<Long> recordIds);

    LedgerDownload downloadDbMetrics(Long dbResourceId, DbMetricRangeQuery query);

    LedgerDownload downloadDbLogs(Long dbResourceId, DbInstanceLogPageQuery query, List<Long> logIds);
}
