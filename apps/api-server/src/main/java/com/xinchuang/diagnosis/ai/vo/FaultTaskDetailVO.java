package com.xinchuang.diagnosis.ai.vo;

import com.xinchuang.diagnosis.ai.entity.AiTask;
import com.xinchuang.diagnosis.ai.entity.TaskExecRecord;
import com.xinchuang.diagnosis.ai.entity.FaultReport;
import java.util.List;

public class FaultTaskDetailVO extends FaultTaskVO {
    private List<TaskExecRecord> executionRecords;
    private List<FaultReport> faultReports;

    public static FaultTaskDetailVO from(AiTask task, List<TaskExecRecord> executionRecords, List<FaultReport> faultReports) {
        FaultTaskDetailVO vo = new FaultTaskDetailVO();
        vo.populate(task); vo.executionRecords = executionRecords; vo.faultReports = faultReports; return vo;
    }
    public List<TaskExecRecord> getExecutionRecords() { return executionRecords; }
    public List<FaultReport> getFaultReports() { return faultReports; }
}
