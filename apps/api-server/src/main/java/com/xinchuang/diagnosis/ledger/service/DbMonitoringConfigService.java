package com.xinchuang.diagnosis.ledger.service;

import com.xinchuang.diagnosis.ledger.dto.DbMonitoringConfigRequest;
import com.xinchuang.diagnosis.ledger.vo.DbMonitoringConfigVO;

public interface DbMonitoringConfigService {
    DbMonitoringConfigVO get(Long resourceId);
    DbMonitoringConfigVO save(Long resourceId, DbMonitoringConfigRequest request);
    DbMonitoringConfigVO test(Long resourceId);
}
