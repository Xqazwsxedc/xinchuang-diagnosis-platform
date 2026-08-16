package com.xinchuang.diagnosis.ai.vo;

import com.xinchuang.diagnosis.ai.entity.ResultStatusLog;
import com.xinchuang.diagnosis.ai.entity.FaultReport;
import java.util.List;

public class FaultAlertDetailVO extends FaultAlertVO {
    private List<FaultAlertHistoryVO> histories;
    private String handledByName;

    public static FaultAlertDetailVO from(FaultReport report, List<ResultStatusLog> logs) {
        FaultAlertDetailVO vo = new FaultAlertDetailVO();
        vo.populate(report);
        vo.histories = logs.stream().map(log -> {
            FaultAlertHistoryVO item = new FaultAlertHistoryVO();
            item.setBeforeStatus(log.getBeforeStatus());
            item.setAfterStatus(log.getAfterStatus());
            item.setOperationRemark(log.getOperationRemark());
            item.setOperatorName(log.getOperatorNameSnapshot());
            item.setCreatedAt(log.getCreatedAt());
            return item;
        }).toList();
        vo.handledByName = logs.stream().map(ResultStatusLog::getOperatorNameSnapshot)
            .filter(name -> name != null && !name.isBlank()).findFirst().orElse(null);
        return vo;
    }

    public List<FaultAlertHistoryVO> getHistories() { return histories; }
    public String getHandledByName() { return handledByName; }
}
